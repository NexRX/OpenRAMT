package Controller.Socket.Task;


import Model.General.MonitoringData;
import Model.Misc.HardDiskItem;
import Model.Misc.SoftDiskItem;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OSFileStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class MonitoringTask implements Runnable {
    // Atomic Monitoring Data.
    private int cpuUsage = 0;
    private long ramUsage = 0;
    private final ArrayList<SoftDiskItem> softDiskItems = new ArrayList<>();
    private final ArrayList<HardDiskItem> hardDiskItems = new ArrayList<>();
    private int cpuTemp = 0;
    private int systemTemp = 0;

    private final AtomicReference<MonitoringData> output = new AtomicReference<>();

    // Hardware Abstractions
    private final SystemInfo sysInfo = new SystemInfo();
    private final HardwareAbstractionLayer hal = sysInfo.getHardware();
    private final CentralProcessor cpu = hal.getProcessor();
    private final List<OSFileStore> disks;
    private final GlobalMemory memory;
    private final List<PowerSource> systemPower;

    // One rrs.
    private final int diskCount;
    private final long ramCapacity;

    // Misc
    long[] preCPUTick = new long[8];
    private AtomicInteger pollingRate = new AtomicInteger(); // Assign automatically from settings

    public MonitoringData getMonitoringData() {
        return output.get();
    }

    public MonitoringTask() {
        // Disks
        this.disks = sysInfo.getOperatingSystem().getFileSystem().getFileStores(true);
        this.diskCount = disks.size();

        // RAM
        this.memory = hal.getMemory();
        this.ramCapacity = memory.getTotal();

        // Other
        systemPower = hal.getPowerSources();

        preCPUTick = cpu.getSystemCpuLoadTicks();
    }

    @Override
    public void run() {
        // CPU Usage & IO

        long[] preIOTick = new long[diskCount]; //disk stores probably different from os file stores.
        for (int i = 0; i < diskCount; i++) {
            preIOTick[i] = sysInfo.getHardware().getDiskStores().get(i).getReads() +
                    sysInfo.getHardware().getDiskStores().get(i).getWrites();

            //System.out.println(sysInfo.getHardware().getDiskStores().get(i).getName() + " | " + sysInfo.getHardware().getDiskStores().get(i).getModel());
        }

        try {
            TimeUnit.MILLISECONDS.sleep(150);
        } catch (InterruptedException e) {
            System.out.println("Thread failed to wait 0.15 seconds. Getting tick data early");
        }

        cpuUsage = (int)(cpu.getSystemCpuLoadBetweenTicks(preCPUTick) * 100);
        preCPUTick = cpu.getSystemCpuLoadTicks(); // for next run

        for (int i = 0; i < diskCount; i++) { // Disk space & final Disk IO
            SoftDiskItem softDisk = new SoftDiskItem(
                    disks.get(i).getLabel(),
                    disks.get(i).getFreeSpace(),
                    disks.get(i).getTotalSpace()
            );

            HardDiskItem hardDisk = new HardDiskItem(sysInfo.getHardware().getDiskStores().get(i).getModel(),
                    (sysInfo.getHardware().getDiskStores().get(i).getReads() +
                            sysInfo.getHardware().getDiskStores().get(i).getWrites()) - preIOTick[i]);

            try {
                softDiskItems.set(i, softDisk);
            } catch (IndexOutOfBoundsException e) {
                softDiskItems.add(i, softDisk);
            }

            try {
                hardDiskItems.set(i, hardDisk);
            } catch (IndexOutOfBoundsException e) {
                hardDiskItems.add(i, hardDisk);
            }
        }

        // RAM Usage
        ramUsage = memory.getAvailable();

        // Temps
        cpuTemp = (int) sysInfo.getHardware().getSensors().getCpuTemperature();

        int systemAvgTemp = 0;
        int tempCount = 0;
        for (PowerSource ps : systemPower) {
            systemAvgTemp += ps.getTemperature();
            tempCount++;
        }
        systemAvgTemp += cpuTemp;
        tempCount++;

        systemTemp = systemAvgTemp / tempCount;

        // Alerts all bindings to output of new data.
        output.set(parseMonitoringData());
    }

    private SoftDiskItem[] getSoftDiskPrimativeArray() {
        SoftDiskItem[] primative = new SoftDiskItem[softDiskItems.size()];
        for (int i=0; i < primative.length; i++)
        {
            primative[i] = softDiskItems.get(i);
        }
        return primative;
    }

    private HardDiskItem[] getHardDiskPrimativeArray() {
        HardDiskItem[] primative = new HardDiskItem[hardDiskItems.size()];
        for (int i=0; i < primative.length; i++)
        {
            primative[i] = hardDiskItems.get(i);
        }
        return primative;
    }

    private MonitoringData parseMonitoringData() {
        return new MonitoringData(
                cpuUsage,
                ramUsage,
                getSoftDiskPrimativeArray(),
                getHardDiskPrimativeArray(),
                cpuTemp,
                systemTemp,
                diskCount,
                ramCapacity);
    }
}

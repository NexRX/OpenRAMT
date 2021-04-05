package Controller.Socket.Task;


import Model.General.MonitoringData;
import Model.Misc.DiskItem;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OSFileStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class MonitoringTask implements Runnable {
    // Atomic Monitoring Data.
    private final AtomicInteger cpuUsage = new AtomicInteger();
    private final AtomicLong ramUsage = new AtomicLong();
    private ArrayList<DiskItem> diskItems = new ArrayList<>();
    private final AtomicInteger cpuTemp = new AtomicInteger();
    private final AtomicInteger systemTemp = new AtomicInteger();

    public final AtomicReference<MonitoringData> output = new AtomicReference<>();

    // OSHI / System Information
    private final SystemInfo sysInfo = new SystemInfo();
    private final HardwareAbstractionLayer hal = sysInfo.getHardware();
    private final CentralProcessor cpu = hal.getProcessor();
    private final List<OSFileStore> disks;
    private final GlobalMemory memory;
    private final PowerSource systemPower;

    // One rrs.
    private final int diskCount;
    private final long ramCapacity;

    // Misc
    private AtomicInteger pollingRate = new AtomicInteger(); // Assign automatically from settings

    // TODO implement this!!! sysInfo.getOperatingSystem().isElevated()
    public MonitoringTask() {
        // Disks
        this.disks = sysInfo.getOperatingSystem().getFileSystem().getFileStores(true);
        this.diskCount = disks.size();

        // RAM
        this.memory = hal.getMemory();
        this.ramCapacity = memory.getTotal();

        // Other
        this.systemPower = hal.getPowerSources().get(0);
    }



    private MonitoringData parseMonitoringData() {

        return new MonitoringData(
                cpuUsage.get(),
                ramUsage.get(),
                getDiskItemsPrimativeArray(),
                cpuTemp.get(),
                systemTemp.get(), diskCount, ramCapacity);
    }

    @Override
    public void run() {
        // CPU Usage & IO
        long[] preCPUTick = cpu.getSystemCpuLoadTicks();

        long[] preIOTick = new long[diskCount]; //disk stores probably different from os file stores.
        for (int i = 0; i < diskCount; i++) { //TODO Redo IO So HardWare Drive matches SoftwareDrive (by confirming hwdrives name = softdrive name) 1.
            preIOTick[i] = sysInfo.getHardware().getDiskStores().get(i).getReads() +
                    sysInfo.getHardware().getDiskStores().get(i).getWrites();

        }

        try {
            System.out.println("sleeping");
            TimeUnit.MILLISECONDS.sleep(100); //TODO configure this with settings
            System.out.println("not sleeping");
        } catch (InterruptedException e) {
            System.out.println("Thread failed to wait 0.1 seconds so getting CPU tick early");
        }

        cpuUsage.set((int)(cpu.getSystemCpuLoadBetweenTicks(preCPUTick) * 100));

        for (int i = 0; i < diskCount; i++) { // Disk space & final Disk IO
            DiskItem thisDisk = new DiskItem(
                    disks.get(i).getLabel(),
                    disks.get(i).getFreeSpace(),
                    disks.get(i).getTotalSpace(),
                    (sysInfo.getHardware().getDiskStores().get(i).getReads() +   //TODO Redo IO So HardWare Drive matches SoftwareDrive (by confirming hwdrives name = softdrive name) 2.
                            sysInfo.getHardware().getDiskStores().get(i).getWrites()) - preIOTick[i]
            );

            try {
                diskItems.set(i, thisDisk);
            } catch (IndexOutOfBoundsException e) {
                diskItems.add(i, thisDisk);
            }
        }

        // RAM Usage
        ramUsage.set(memory.getAvailable());

        // Temps
        cpuTemp.set((int) sysInfo.getHardware().getSensors().getCpuTemperature());
        systemTemp.set((int) systemPower.getTemperature()); // Actually battery temp, so 0 on desktop.

        // Alerts all bindings to output of new data.
        output.set(parseMonitoringData());
    }

    private DiskItem[] getDiskItemsPrimativeArray() {
        DiskItem[] primative = new DiskItem[diskItems.size()];
        for (int i=0; i < primative.length; i++)
        {
            primative[i] = diskItems.get(i);
        }
        return primative;
    }
}

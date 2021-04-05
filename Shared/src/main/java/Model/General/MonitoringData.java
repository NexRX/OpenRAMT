package Model.General;

import Model.Misc.DiskItem;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MonitoringData implements Serializable {
    private final int cpuUsage;
    private final long ramUsage;
    private final DiskItem[] disks;
    private final int cpuTemp;
    private final int systemTemp;

    private final int diskCount;
    private final long ramCapacity;

    private final String timestamp;

    /**
     * Wrapper class from the data that the OpenRAMT monitoring service provides for serialisation. A timestamp is
     * automatically provided as a SimpleDateFormat when constructing a instance of this class.
     * @param cpuUsage The CPU usage of the server.
     * @param ramUsage The RAM usage of the server.
     * @param disk An arraylist of DiskItem objects that refer to the state of a disk's label/name, space, capacity & IO. //TODO fix this documentation
     * @param cpuTemp The CPU temperature of the server.
     * @param systemTemp The system temperature of the server.
     * @param diskCount The amount of physical disks on the system.
     * @param ramCapacity The total capacity of physical memory.
     */
    public MonitoringData(int cpuUsage, long ramUsage, DiskItem[] disks, int cpuTemp, int systemTemp, int diskCount, long ramCapacity) {
        this.cpuUsage = cpuUsage;
        this.ramUsage = ramUsage;
        this.disks = disks;
        this.cpuTemp = cpuTemp;
        this.systemTemp = systemTemp;
        this.diskCount = diskCount;
        this.ramCapacity = ramCapacity;
        this.timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public int getCPUUsage() { return cpuUsage; }

    public long getRAMUsage() { return ramUsage; }

    public DiskItem getDisk(int index) { return disks[index]; }

    public DiskItem[] getDisks() { return disks; }

    public int getCPUTemp() { return cpuTemp; }

    public int getSystemTemp() { return systemTemp; }

    public String getTimestamp() { return timestamp; }

    public int getDiskCount() { return diskCount; }

    public long getRamCapacity() { return ramCapacity; }
}

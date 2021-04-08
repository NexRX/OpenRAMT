package Model.Misc;

public class ProcessItem {
    private final String name;
    private final String id;
    private final String status;
    private final String cpu;
    private final String mem;


    public ProcessItem(String name, String id, String status, String cpu, String mem) {
        this.name = name;
        this.id = id;
        this.status = status;
        this.cpu = cpu;
        this.mem = mem;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getCpu() {
        return cpu;
    }

    public String getMem() {
        return mem;
    }
}

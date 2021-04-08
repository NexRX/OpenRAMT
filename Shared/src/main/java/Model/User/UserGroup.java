package Model.User;

import java.io.Serializable;

public class UserGroup implements Serializable {
    private String name;
    private String admin;
    private String general;
    private String process;
    private String monitoring;
    private String power;

    public UserGroup(String name, boolean admin, boolean general, boolean process, boolean monitoring, boolean power) {
        this.name = name;
        this.admin = admin ? "True" : "False";
        this.general = general ? "True" : "False";
        this.process = process ? "True" : "False";
        this.monitoring = monitoring ? "True" : "False";
        this.power = power ? "True" : "False";
    }

    // Normal
    public Boolean isName() { return Boolean.parseBoolean(name); }
    public Boolean isAdmin() { return Boolean.parseBoolean(admin); }
    public Boolean isGeneral() { return Boolean.parseBoolean(general); }
    public Boolean isProcess() { return Boolean.parseBoolean(process); }
    public Boolean isMonitoring() { return Boolean.parseBoolean(monitoring); }
    public Boolean isPower() { return Boolean.parseBoolean(power); }

    // For printing to a table without parsing and then formatting etc.
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAdmin() {
        return admin;
    }
    public void setAdmin(String admin) {
        this.admin = admin;
    }
    public String getGeneral() {
        return general;
    }
    public void setGeneral(String general) {
        this.general = general;
    }
    public String getProcess() {
        return process;
    }
    public void setProcess(String process) {
        this.process = process;
    }
    public String getMonitoring() {
        return monitoring;
    }
    public void setMonitoring(String monitoring) {
        this.monitoring = monitoring;
    }
    public String getPower() {
        return power;
    }
    public void setPower(String power) {
        this.power = power;
    }
}

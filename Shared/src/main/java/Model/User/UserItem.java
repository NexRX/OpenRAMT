package Model.User;

import java.util.HashMap;

public class UserItem {
    private String username;

    private String group;
    private String admin;
    private String general;
    private String process;
    private String monitoring;
    private String power;

    private String suspended;

    private UserData userData;
    private UserGroup userGroup;

    /**
     * Assigns the values of user group and data together to be represented in a TableView without having to use a pair
     * of generics.
     * @param user The UserData holding the name and group values of the user.
     * @param group The UserGroup which hold the permission values.
     */
    public UserItem(UserData user, UserGroup group) {
        this.username = user.getUsername();
        this.group = user.getGroup();

        this.admin = group.getAdmin();
        this.general = group.getGeneral();
        this.process = group.getProcess();
        this.monitoring = group.getMonitoring();
        this.power = group.getPower();

        this.suspended = user.isSuspended().toString();
    }

    /**
     * Automatically searches and assigns the text value of groups to this item object representing Users.
     * @param user The UserData holding the name and group values of the user.
     * @param groups The UserGroup ArrayList which hold the permission values of all groups to search through.
     */
    public UserItem(UserData user, HashMap<String, UserGroup> groups) {
        this.username = user.getUsername();
        this.group = user.getGroup();

        UserGroup group = groups.get(user.getGroup());

        this.admin = group.getAdmin();
        this.general = group.getGeneral();
        this.process = group.getProcess();
        this.monitoring = group.getMonitoring();
        this.power = group.getPower();

        this.suspended = user.isSuspended().toString();
    }

    public UserData getUserObj() {
        return userData;
    }

    public UserGroup getGroupObj() {
        return userGroup;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
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

    public String getSuspended() {
        return suspended;
    }

    public void setSuspended(String suspended) {
        this.suspended = suspended;
    }

    public UserData getUserData() {
        return userData;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

    public void setUserData(UserGroup userGroup) {
        this.userGroup = userGroup;
    }
}

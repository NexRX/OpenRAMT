package Model.Task;

import Model.User.UserData;

import java.io.Serializable;
import java.util.UUID;

public class TaskRequest implements Serializable {
    private final String requestID = UUID.randomUUID().toString();
    private final Task task;
    private final UserData user;

    public TaskRequest(Task task, UserData user) {
        this.task = task;
        this.user = user;
    }

    public String getRequestID() {
        return requestID;
    }

    public Task getTask() {
        return task;
    }

    public UserData getUser() {
        return user;
    }
}

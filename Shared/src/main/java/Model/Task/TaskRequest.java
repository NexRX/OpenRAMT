package Model.Task;

import Model.User.UserData;
import java.io.Serializable;
import java.util.UUID;

public class TaskRequest<T> implements Serializable {
    private final String requestID = UUID.randomUUID().toString();
    private final Task task;
    private final UserData user;
    private final T parameter;

    public TaskRequest(Task task, UserData user) {
        this.task = task;
        this.user = user;
        this.parameter = null;
    }

    public TaskRequest(Task task, UserData user, T parameter) {
        this.task = task;
        this.user = user;
        this.parameter = parameter;
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

    public T getParameter() {
        return parameter;
    }
}

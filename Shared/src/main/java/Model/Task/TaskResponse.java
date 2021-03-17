package Model.Task;

import java.io.Serializable;

public class TaskResponse implements Serializable {
    private final TaskRequest request;
    private final Response response;
    private final int responseCode;

    public TaskResponse(TaskRequest request, Response response, Integer responseCode) {
        this.request = request;
        this.response = response;
        this. responseCode = responseCode;
    }

    public String getRequestID() {
        return request.getRequestID();
    }

    public Response getResponse() {
        return response;
    }

    public int getResponseCode() {
        return responseCode;
    }

}

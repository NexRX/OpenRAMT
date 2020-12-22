package Model;

public class TaskResponse {
    private final TaskRequest request;
    private final Response response;

    public TaskResponse(TaskRequest request, Response response) {
        this.request = request;
        this.response = response;
    }

    public String getRequestID() {
        return request.getRequestID();
    }

    public Response getResponse() {
        return response;
    }

}

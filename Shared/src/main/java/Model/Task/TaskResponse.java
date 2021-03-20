package Model.Task;

import java.io.Serializable;

public class TaskResponse<T> implements Serializable {
    private final TaskRequest request;
    private final Response response;
    private final int responseCode;
    private final T responseData;

    public TaskResponse(TaskRequest request, Response response, int responseCode) {
        this.request = request;
        this.response = response;
        this.responseCode = responseCode;

        this.responseData = null;
    }

    public TaskResponse(TaskRequest request, Response response, Integer responseCode, T responseData) {
        this.request = request;
        this.response = response;
        this.responseCode = responseCode;

        this.responseData = responseData;
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

    /**
     * Shorthand method to access if there is any data.
     * @return True for response data being anything other than null. False if it is null.
     */
    public boolean isData() {
        return responseData != null;
    }

    public T getResponseData() {
        return responseData;
    }

}

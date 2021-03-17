package Controller.Socket;

import Controller.Socket.Task.RAMTTaskLibrary;
import Model.Task.Response;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**

 */
public class ServerWorker implements Runnable{
    protected SSLSocket clientSocket = null;
    protected String serverText = null;

    public ServerWorker(SSLSocket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
    }

    public void run() {
        try {
            // create a DataInputStream so we can read data from it.
            ObjectInputStream socketInput = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream socketOutput = new ObjectOutputStream(clientSocket.getOutputStream());

            // Get the Task request from client then process.
            TaskRequest request = (TaskRequest) socketInput.readObject();
            System.out.println("Received [" + request.getRequestID() + " | " + request.getTask() +
                    ", by " + request.getUser() + "] in socket: " + clientSocket);

            int responseCode = processTask(request);
            System.out.println(responseCode);

            // Create response with processing results then send to user.

            Response responseState = switch (responseCode) {
                case 0 -> Response.SUCCESS;
                case 10, 11, 12, 19 -> Response.FAILEDAUTHENTICATION;
                default -> Response.OTHER;
            };

            TaskResponse response = new TaskResponse(request, responseState, responseCode);

            socketOutput.writeObject(response);

            // Finished, so printing this as such.
            System.out.println(request.getRequestID() + " closed without exception in socket: " + clientSocket);

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Socket " + clientSocket + " closing because of an exception, printing stack trace...");
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("A Socket closure completed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int processTask(TaskRequest request) {
        switch (request.getTask()) {
            case LOGIN:
                return RAMTTaskLibrary.login(request.getUser());
            case KILLPROCESS:
                break;
            case RESTARTPROCESS:
                break;
            case FETCHPROCESSES:
                break;
            case SHUTDOWN:
                break;
            case RESTART:
                break;
            case SLEEP:
                break;
            case ADDUSER:
                break;
            case EDITUSER:
                break;
            case DELETEUSER:
                break;
            case EDITSETTING:
                break;
            case GETSETTINGS:
                break;
            case STARTFTP:
                break;
            case STOPFTP:
                break;
            case RESTARTFTP:
                break;
            case CLEANDISK:
                break;
            case ENABLEWIFI:
                break;
            case DISABLEWIFI:
                break;
            case ENABLEBLUETOOTH:
                break;
            case DISABLEBLUETOOTH:
                break;
            case TESTING:
                break;
        }
        return 404; // Task not found.
    }
}
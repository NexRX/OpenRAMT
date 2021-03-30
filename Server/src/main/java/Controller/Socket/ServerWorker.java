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
            // create DataStreams so we can read/write data from it.
            ObjectInputStream socketInput = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream socketOutput = new ObjectOutputStream(clientSocket.getOutputStream());

            // Get the Task request from client then process.
            TaskRequest<?> request = (TaskRequest<?>) socketInput.readObject();

            System.out.println("Received [" + request.getRequestID() + " | " + request.getTask() +
                    ", by " + request.getUser().getUsername() + "] in socket: " + clientSocket);

            TaskResponse<?> response = processTask(request);
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

    private TaskResponse<?> processTask(TaskRequest<?> request) {
        switch (request.getTask()) {
            case LOGIN:
                return RAMTTaskLibrary.login((TaskRequest<Void>) request);
            case KILLPROCESS:
                return RAMTTaskLibrary.killProcess((TaskRequest<Integer>) request);
            case RESTARTPROCESS:
                return RAMTTaskLibrary.restartProcess((TaskRequest<Integer>) request);
            case FETCHPROCESSES:
                return RAMTTaskLibrary.fetchProcesses((TaskRequest<Void>) request);
            case SHUTDOWN:
                return RAMTTaskLibrary.shutdown((TaskRequest<Void>) request);
            case RESTART:
                return RAMTTaskLibrary.restart((TaskRequest<Void>) request);
            case SLEEP:
                return RAMTTaskLibrary.sleep((TaskRequest<Void>) request);
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
                return RAMTTaskLibrary.startFTP((TaskRequest<Void>) request);
            case STOPFTP:
                return RAMTTaskLibrary.stopFTP((TaskRequest<Void>) request);
            case RESTARTFTP:
                return RAMTTaskLibrary.restartFTP((TaskRequest<Void>) request);
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
        return new TaskResponse<Void>(request, Response.INTERRUPTED, 99);
    }
}
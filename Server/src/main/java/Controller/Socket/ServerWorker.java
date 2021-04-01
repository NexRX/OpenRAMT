package Controller.Socket;

import Controller.Socket.Task.RAMTTaskLibrary;
import Model.Task.Response;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import Model.User.UserData;
import Model.User.UserGroup;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static Controller.Socket.Task.RAMTTaskLibrary.*;

/**

 */
@SuppressWarnings("unchecked") // Safe when server & client respects request/response structure.
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
        return switch (request.getTask()) {
            case LOGIN -> login((TaskRequest<Void>) request);
            case KILLPROCESS -> killProcess((TaskRequest<Integer>) request);
            case RESTARTPROCESS -> restartProcess((TaskRequest<Integer>) request);
            case FETCHPROCESSES -> fetchProcesses((TaskRequest<Void>) request);
            case SHUTDOWN -> shutdown((TaskRequest<Void>) request);
            case RESTART -> restart((TaskRequest<Void>) request);
            case SLEEP -> sleep((TaskRequest<Void>) request);
            case ADDGROUP -> addGroup((TaskRequest<UserGroup>) request);
            case ADDUSER -> addUser((TaskRequest<UserData>) request);
            case UPDATEUSER -> updateUser((TaskRequest<String[]>) request);
            case DELETEGROUP -> deleteGroup((TaskRequest<String>) request);
            case DELETEUSER -> deleteUser((TaskRequest<String>) request);
            case DELETEUSERS -> deleteUsers((TaskRequest<String>) request);
            case EDITSETTING -> setSetting((TaskRequest<String[]>) request);
            case GETGROUP -> getGroup((TaskRequest<String>) request);
            case GETGROUPS -> getGroups((TaskRequest<Void>) request);
            case GETUSER -> getUser((TaskRequest<String>) request);
            case GETUSERS -> getUsers((TaskRequest<Void>) request);
            case GETSETTING -> getSetting((TaskRequest<String>) request);
            case GETSETTINGS -> getSettings((TaskRequest<Void>) request);
            case STARTFTP -> startFTP((TaskRequest<Void>) request);
            case STOPFTP -> stopFTP((TaskRequest<Void>) request);
            case RESTARTFTP -> restartFTP((TaskRequest<Void>) request);
            case CLEANDISK -> cleanDisk((TaskRequest<Integer>) request);
            case ENABLEWIFI -> enableWifi((TaskRequest<Integer>) request);
            case DISABLEWIFI -> disableWifi((TaskRequest<Integer>) request);
            case ENABLEBLUETOOTH -> enableBluetooth((TaskRequest<Integer>) request);
            case DISABLEBLUETOOTH -> disableBluetooth((TaskRequest<Integer>) request);
            case SUSPENDUSER -> suspendUser((TaskRequest<String>) request);
            case SUSPENDUSERS -> suspendUsers((TaskRequest<String>) request);
            case UPDATEGROUP -> updateGroup((TaskRequest<String[]>) request);
            case WOL -> wakeOnLAN((TaskRequest<String[]>) request);
            case TESTING -> new TaskResponse<>(request, Response.OTHER, 0);
            default -> new TaskResponse<Void>(request, Response.INTERRUPTED, 99); // Lil' future proofing.
        };
    }
}
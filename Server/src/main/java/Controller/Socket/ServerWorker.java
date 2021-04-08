package Controller.Socket;

import Model.Task.Response;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import Model.User.UserData;
import Model.User.UserGroup;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

import static Controller.Socket.Task.RAMTTaskLibrary.*;

/**

 */
@SuppressWarnings("unchecked") // Safe when server & client respects request/response structure.
public class ServerWorker implements Runnable{
    protected Socket socket;

    public ServerWorker(Socket clientSocket) {
        this.socket     = clientSocket;
    }

    public void run() {
        try {
            // create DataStreams so we can read/write data from it.
            ObjectInputStream socketInput = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream socketOutput = new ObjectOutputStream(socket.getOutputStream());

            // Get the Task request from client then process.
            TaskRequest<?> request = (TaskRequest<?>) socketInput.readObject();

            System.out.println("Received [" + request.getRequestID() + " | " + request.getTask() +
                    ", by " + request.getUser().getUsername() + "] in socket: " + socket);

            TaskResponse<?> response = processTask(request);
            socketOutput.writeObject(response);

            // Finished, so printing this as such.
            System.out.println(request.getRequestID() + " closed without exception in socket: " + socket);

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Socket " + socket + " closing because of an exception," +
                    " could be early wrong security/early disconnect from user. Printing Stack Trace...");
            e.printStackTrace();
        } finally {
            try {
                socket.close();
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
            case FACTORYRESET -> factoryReset((TaskRequest<Void>) request);
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
            case EDITSETTINGS -> setSettings((TaskRequest<HashMap<String, String>>) request);
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
            default -> new TaskResponse<Void>(request, Response.INTERRUPTED, 99); // Lil' future proofing.
        };
    }
}
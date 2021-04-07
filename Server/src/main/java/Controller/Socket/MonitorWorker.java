package Controller.Socket;

import Controller.Database.DBManager;
import Controller.Socket.Task.MonitoringTask;
import Controller.Socket.Task.RAMTTaskLibrary;
import Model.Task.Task;
import Model.Task.TaskRequest;
import Model.User.UserData;
import Model.User.UserGroup;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

//@SuppressWarnings("unchecked") // Safe when server & client respects request/response structure.
public class MonitorWorker implements Runnable {
    Socket socket;
    MonitoringTask monitor;
    //private volatile ReadOnlyObjectProperty<MonitoringData> monitoringData;

    private final int pollingRate;

    public MonitorWorker(Socket socket, MonitoringTask monitor) {
        this.socket = socket;
        this.monitor = monitor;

        int pollingRateBuf;
        try {
            pollingRateBuf = Integer.parseInt(DBManager.getSetting("Monitoring Polling Rate"));
        } catch (SQLException e) {
            pollingRateBuf = 3000; // fallback
        }
        pollingRate = pollingRateBuf;
    }

    @Override
    public void run() {
        try {
            // create DataStreams so we can read/write data from it.
            ObjectOutputStream socketOutput = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream socketInput = new ObjectInputStream(socket.getInputStream());

            // Get the Task request from client then process.
            UserData requester = (UserData) socketInput.readObject();

            System.out.println("Monitor Request [" + requester.getUsername() + " | " + requester.getHost() + "] in socket: "
                    + socket);

            UserGroup group = RAMTTaskLibrary.getGroup(new TaskRequest<String>(Task.GETGROUP, requester, requester.getGroup())).getResponseData();

            if (group == null || !group.isMonitoring()) {
                socketOutput.writeInt(0);
            } else {
                socketOutput.writeInt(pollingRate);

                boolean isNotTimedOut = true;
                socket.setSoTimeout(10 * 1000); //10s

                while (isNotTimedOut) {
                    try {
                        try {
                            TimeUnit.MILLISECONDS.sleep(pollingRate * 1000L); // supports decimal seconds
                        } catch (InterruptedException e) {
                            System.out.println("Polled Early...");
                        }

                        socketOutput.writeUnshared(monitor.getMonitoringData()); // java.net.SocketException: Connection reset by peer could be used to handle early disconnect
                    } catch (SocketTimeoutException e) {
                        isNotTimedOut = false;
                    }
                }
            }
            // Finished, so printing this as such.
            System.out.println(requester.getUsername() + "'s monitor request finished in: " + socket);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Socket (" + socket + ") closing because of an exception," +
                    " could be wrong security/early disconnect from user. Printing Stack Trace...");
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Failed to close socket, printing...");
            e.printStackTrace();
        }
    }
}

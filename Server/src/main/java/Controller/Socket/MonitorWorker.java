package Controller.Socket;

import Controller.Socket.Task.MonitoringTask;
import Model.General.MonitoringData;
import Model.Misc.DiskItem;
import Model.User.UserData;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

//@SuppressWarnings("unchecked") // Safe when server & client respects request/response structure.
public class MonitorWorker implements Runnable {
    Socket socket;
    MonitoringTask monitor;
    //private volatile ReadOnlyObjectProperty<MonitoringData> monitoringData;

    public MonitorWorker(Socket socket, MonitoringTask monitor) {
        this.socket = socket;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try {
            System.out.println("Work started 2");
            // create DataStreams so we can read/write data from it.
            ObjectOutputStream socketOutput = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Work started 2.5");
            ObjectInputStream socketInput = new ObjectInputStream(socket.getInputStream());

            System.out.println("Work started 3");

            // Get the Task request from client then process.
            UserData requester = (UserData) socketInput.readObject();

            System.out.println("Monitor Request [" + requester.getUsername() + " | " + requester.getHost() + "] in socket: "
                    + socket);

            socketOutput.writeObject(true); //todo auth it

            boolean isNotTimedOut = true;

            socket.setSoTimeout(10 * 1000);

            while (isNotTimedOut) {
                try {
                    try {
                        System.out.println("sleeping");
                        TimeUnit.MILLISECONDS.sleep(3000); //TODO configure this with settings
                        System.out.println("not sleeping");       //TODO BUT BIGGER. ISN'T SLEPT OR WAITED SOMEHOW IT WILL FUCK OVER MONITOR AND ITS FRESHNESS OF DATA.
                    } catch (InterruptedException e) {
                        System.out.println("Thread failed to wait 0.2 seconds so getting CPU tick early");
                    }

                    for (DiskItem disk : monitor.output.get().getDisks()) {
                        System.out.println("Disk " + disk.getDiskIdentifier() +": "+ disk.getDiskIO());
                    }

                    socketOutput.writeUnshared(monitor.output.get()); // java.net.SocketException: Connection reset by peer could be used to handle early disconnect
                } catch (SocketTimeoutException e) {
                    isNotTimedOut = false;
                }
            }

            // Finished, so printing this as such.
            System.out.println(requester.getUsername() + "'s monitor request handle in: " + socket);
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
}

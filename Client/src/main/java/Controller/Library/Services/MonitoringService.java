package Controller.Library.Services;

import Controller.Content.MonitorController;
import Controller.Library.Socket.ClientMonitorWorker;
import Model.General.SocketStreamSession;
import Model.User.UserData;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.chart.XYChart;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.FutureTask;

public class MonitoringService extends Service<Void> {
    private UserData user;
    private ClientMonitorWorker monitoringWorker;
    private final MonitorController monitoringController;

    public MonitoringService(UserData user, MonitorController monitoringController) {
        super();
        this.user = user;
        this.monitoringController = monitoringController;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() {
                try {
                    System.out.println("Setting up monitoring connection.");
                    Socket socket = new Socket(user.getHost(), 3068); //TODO get from server settings
                    socket.setSoTimeout(10 * 1000); //10s

                    ObjectOutputStream socketOutput = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream socketInput = new ObjectInputStream(socket.getInputStream());

                    // Asking Server to allow monitoring.
                    socketOutput.writeObject(user);

                    // Server permits?
                    if (!((boolean) socketInput.readObject())) {
                        System.out.println("Denied Access to monitoring.");
                    } else {
                        // AFTER THIS DONT USE SOCKET
                        System.out.println("Initialising M-Worker");
                        monitoringWorker = new ClientMonitorWorker(socketInput, monitoringController);
                        FutureTask<Void> futureTask = new FutureTask<>(monitoringWorker);
                        Thread thread = new Thread(futureTask);
                        thread.start();
                        System.out.println("Thread for M-Worker started");
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    public void updateUser(UserData user) {
        this.user = user;
    }

    public boolean isMonitoring() {
        return monitoringWorker.isMonitoring();
    }

    public ClientMonitorWorker getMonitoringWorker() {
        return monitoringWorker;
    }
}

package Controller.Library.Services;

import Controller.Content.MonitorController;
import Controller.Library.Socket.ClientMonitorWorker;
import Controller.Library.Socket.ClientWorker;
import Model.User.UserData;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
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
                    System.out.println("Setting up monitoring.");

                    Socket socket = user.isSecure() ? // Secure or plain socket?
                            ClientWorker.secureGeneration(user.getHost(), user.getMonitoringPort()) :
                            new Socket(user.getHost(), user.getMonitoringPort());

                    socket.setSoTimeout(10 * 1000); //10s

                    // Object stream setup
                    ObjectOutputStream socketOutput = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream socketInput = new ObjectInputStream(socket.getInputStream());

                    // Asking Server to allow monitoring.
                    socketOutput.writeObject(user);

                    // Server permits?
                    int pollingRate = socketInput.readInt();

                    if (pollingRate <= 0) {
                        System.out.println("Denied Access to monitoring.");
                    } else {
                        // AFTER THIS DONT USE SOCKET
                        System.out.println("Initialising M-Worker");
                        monitoringWorker = new ClientMonitorWorker(socketInput, monitoringController, pollingRate);
                        FutureTask<Void> futureTask = new FutureTask<>(monitoringWorker);
                        Thread thread = new Thread(futureTask);
                        thread.start();
                        System.out.println("Thread for M-Worker started");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
                    System.out.println("Failed to generate secure server.");
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

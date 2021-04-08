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

/**
 * The service extending class responsible for handling monitoring data.
 */
public class MonitoringService extends Service<Void> {
    private final UserData user;
    private final MonitorController monitoringController;
    private ClientMonitorWorker monitoringWorker;
    private Socket socket;

    /**
     * This service is used to monitor the server data given relating to its various states, such as disk spaces and CPU
     *  usage. If a timeout occurs or a socket is closed for some reason, the service will need to be restarted again.
     * @param user The logged in user data contians the information to connect to the host and authorise a monitoring
     *             session.
     * @param monitoringController The monitoring controller in which to update with our stream monitoring data.
     */
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

                    socket = user.isSecure() ? // Secure or plain socket?
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

    /**
     * Stops the service workers, closes the socket and sets it as null afterwards.
     */
    public void closeSockets() {
        monitoringWorker.stop();
        try {
            socket.close();
        } catch (IOException ignored) {}
        socket = null;
    }

    /**
     * Used to detect if the server is continuing to monitor indefinitely while the sockets are healthy.
     * @return The monitoring state. True for will continue to monitor indefinitely, False otherwise.
     */
    public boolean isMonitoring() {
        return monitoringWorker.isMonitoring();
    }
}

package Controller.Socket;

import Controller.RAMTAlert;
import Controller.Socket.Task.MonitoringTask;
import Model.User.UserData;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlainMonitoringServer implements Runnable {
    protected int port = 3068; // Default
    protected boolean stop = false;
    protected boolean isStopped = false;
    private boolean isInitialised = false;

    protected Thread runningThread = null;

    protected ServerSocket serverSocket;

    public PlainMonitoringServer() {
        try {
            initialisation();
        } catch (BindException e) {
            failedServerBind();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlainMonitoringServer(int port) {
        try {
            initialisation();
        } catch (BindException e) {
            failedServerBind();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * If the server successfully initialised once then the return of this will be true.
     * @return returns true for the state initialisation, false otherwise.
     */
    public boolean isInitialised() {
        return isInitialised;
    }

    /**
     * Runs a server that will start a new thread per incoming client request.
     */
    @Override
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }

        if (!isInitialised) {
            System.out.println("Server not initialised");
        }

        System.out.println("Monitoring server starting up");

        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        final MonitoringTask monitor = new MonitoringTask();
        ses.scheduleAtFixedRate(monitor, 0, 1, TimeUnit.SECONDS);

        while (!isStopped()) {
            Socket clientSocket;

            try {
                System.out.println("Waiting for monitor clients");
                clientSocket = this.serverSocket.accept();
                System.out.println(clientSocket.isConnected());
                new Thread(new MonitorWorker(clientSocket, monitor)).start();
            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("Server Stopped.");
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
        }

        ses.shutdown();
        System.out.println("Server Stopped.");
    }

    /**
     * This method gets the synchronised stopped state of the server.
     * @return the state of the server, true for stopped, false otherwise.
     */
    public synchronized boolean isStopped() {
        return this.isStopped;
    }

    /**
     * Fully stops the server. Causing a new one to be created if there are future internal calls to re-initialise.
     */
    public synchronized void stop() {
        this.isStopped = true;
        try {
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);

        }
    }

    /**
     * Creates our plain socket which therefore is unencrypted.
     *
     * @throws IOException      If the file doesn't exist then this exception will be thrown.
     * @throws BindException    If the port or server couldn't be opened when creating server.
     */
    private void initialisation() throws IOException {
        this.serverSocket = new ServerSocket(this.port);
        isInitialised = true;
    }

    private void failedServerBind() {
        new RAMTAlert(Alert.AlertType.ERROR,
                "OpenRAMT Startup Error.",
                "The port assigned to the server is already taken.",
                "This can be because two servers are running at once (on the same port) so please " +
                        "try closing the other program or wiping this application to choose a different port." +
                        "\n\n This program will now be exiting. Port: " + port).showAndWait();
        System.exit(-1);
        //TODO provide a way to change settings without a wipe in the future.
    }
}

package Controller.Library.Socket;

import Controller.Content.MonitorController;
import Model.General.MonitoringData;
import javafx.application.Platform;
import java.io.*;
import java.net.SocketTimeoutException;
import java.util.concurrent.*;

/**
 * Client monitor worker for its intended services.
 */
public class ClientMonitorWorker implements Callable<Void> {
    private final ObjectInputStream socketInput;
    private final MonitorController monitorController;
    private final int pollingRate;

    private boolean isMonitoring;
    private boolean stopSignal;

    /**
     * Does the monitoring work needed to get the monitoring data with the given pre-established socket and updates the
     * JavaFX thread where the monitorController will be to supply the information needed to update the charts.
     * @param socketInput The pre-established and authorised input socket with the server.
     * @param monitorController The monitoring controller to send the updated information too.
     * @param pollingRate the rate at which information is being generated on the server.
     */
    public ClientMonitorWorker(ObjectInputStream socketInput, MonitorController monitorController, int pollingRate) {
        this.socketInput = socketInput;
        this.monitorController = monitorController;
        this.pollingRate = pollingRate;
    }

    @Override
    public Void call() throws IOException, ClassNotFoundException {
        try {
            MonitoringData data = ((MonitoringData) socketInput.readUnshared());

            while (!stopSignal && data != null) {
                final MonitoringData loopData = data; // Final for JavaFX thread.
                isMonitoring = true;

                Platform.runLater(() -> { // Update JavaFX thread
                    monitorController.updateCPUUsage(loopData.getTimestamp(), loopData.getCPUUsage());
                    monitorController.updateRAMUsage(loopData.getRAMUsage(), loopData.getRamCapacity());
                    monitorController.updateDiskSpaces(loopData.getSoftDisks());
                    monitorController.updateDiskIOs(loopData.getHardDisks(), loopData.getTimestamp());
                    monitorController.updateCPUTemp(loopData.getTimestamp(), loopData.getCPUTemp());
                    monitorController.updateSystemTemp(loopData.getTimestamp(), loopData.getSystemTemp());
                });

                try {
                    // Sleep for polling rate.
                    TimeUnit.MILLISECONDS.sleep(pollingRate * 1000L);
                } catch (InterruptedException e) {
                    System.out.println("Thread failed to sleep, data retrieved early.");
                }

                data = (((MonitoringData) socketInput.readUnshared())); // Update data for next loop
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Monitoring socket timed out, quiting monitoring work.");
        } finally {
            isMonitoring= false;
        }

        socketInput.close();

        System.out.println("Finished Monitoring");
        return null;
    }

    /**
     * Also effective as a isStopped method since most of the work only occurs if isMonitoring is true. If it was true
     * and is false it cant be true again unless the worker has been manually restarted somehow.
     * @return True if the server will continue to monitor indefinitely, false otherwise.
     */
    public boolean isMonitoring() {
        return isMonitoring;
    }

    /**
     * Asks the worker to stop monitoring. This will cause it to exit its loop and therefore close the socket.
     * Use this for a more graceful exit other the server may have a (none-terminal) exception.
     */
    public void stop() {
        this.stopSignal = true;
    }
}
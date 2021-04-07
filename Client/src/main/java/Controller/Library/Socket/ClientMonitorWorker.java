package Controller.Library.Socket;

import Controller.Content.MonitorController;
import Model.General.MonitoringData;
import javafx.application.Platform;

import java.io.*;
import java.util.concurrent.*;

public class ClientMonitorWorker implements Callable<Void> {
    private final ObjectInputStream socketInput;
    private final MonitorController monitorController;
    private final int pollingRate;

    private boolean isMonitoring;
    private boolean stopSignal;

    public ClientMonitorWorker(ObjectInputStream socketInput, MonitorController monitorController, int pollingRate) {
        this.socketInput = socketInput;
        this.monitorController = monitorController;
        this.pollingRate = pollingRate;
    }

    @Override
    public Void call() throws IOException, ClassNotFoundException {
        MonitoringData data = ((MonitoringData) socketInput.readUnshared()); // At some point this held up monitoring. Maybe define a timeout?

        while (!stopSignal && data != null /*&& socketInput != null*/) { // Incase sockinput is nul
            MonitoringData loopData = data; // (final) Need to perform platform.runLater
            isMonitoring = true;

            Platform.runLater(() -> {
                // put random number with current time
                monitorController.updateCPUUsage(loopData.getTimestamp(), loopData.getCPUUsage());
                monitorController.updateRAMUsage(loopData.getRAMUsage(), loopData.getRamCapacity());
                monitorController.updateDiskSpaces(loopData.getSoftDisks());
                monitorController.updateDiskIOs(loopData.getHardDisks(), loopData.getTimestamp());
                monitorController.updateCPUTemp(loopData.getTimestamp(), loopData.getCPUUsage());
                monitorController.updateSystemTemp(loopData.getTimestamp(), loopData.getSystemTemp());
            });

            try {
                // Sleep for polling rate.
                TimeUnit.MILLISECONDS.sleep(pollingRate * 1000L);
            } catch (InterruptedException e) {
                System.out.println("Thread failed to sleep, data retrieved early.");
            }

            data = (((MonitoringData) socketInput.readUnshared()));
        }
        socketInput.close();
        isMonitoring = false;

        System.out.println("Finished Monitoring");
        return null;
    }

    /**
     * Also effective as a isStopped method since most of the work only occurs if isMonitoring is true. If it was true
     * and is false it cant be true again unless the worker has been manually restarted somehow.
     * @return
     */
    public boolean isMonitoring() {
        return isMonitoring;
    }

    /**
     * Asks the worker to stop monitoring.
     */
    public void stop() {
        this.stopSignal = true;
    }
}
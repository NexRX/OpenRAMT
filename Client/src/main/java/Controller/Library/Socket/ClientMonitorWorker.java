package Controller.Library.Socket;

import Controller.Content.MonitorController;
import Model.General.MonitoringData;
import Model.Misc.DiskItem;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import Model.User.UserData;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.chart.XYChart;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.*;

public class ClientMonitorWorker implements Callable<Void> {
    private final ObjectInputStream socketInput;
    private final MonitorController monitorController;

    private boolean isMonitoring;
    private boolean stopSignal;

    public ClientMonitorWorker(ObjectInputStream socketInput, MonitorController monitorController) {
        this.socketInput = socketInput;
        this.monitorController = monitorController;
    }

    @Override
    public Void call() throws IOException, ClassNotFoundException {
        MonitoringData data = ((MonitoringData) socketInput.readUnshared()); // At some point this held up monitoring. Maybe define a timeout?

        while (!stopSignal && data != null /*&& socketInput != null*/) { // Incase sockinput is nul
            MonitoringData loopData = data; // (final) Need to perform platform.runLater
            isMonitoring = true;

            for (DiskItem disk : loopData.getDisks()) {
                System.out.println("Disk " + disk.getDiskIdentifier() +": "+ disk.getDiskIO());
            }

            System.out.println(loopData.getTimestamp());

            Platform.runLater(() -> {
                // put random number with current time
                monitorController.updateCPUUsage(loopData.getTimestamp(), loopData.getCPUUsage());
                monitorController.updateRAMUsage(loopData.getRAMUsage(), loopData.getRamCapacity());
                monitorController.updateDiskSpaces(loopData.getDisks());
                monitorController.updateDiskIOs(loopData.getDisks(), loopData.getTimestamp());
                monitorController.updateCPUTemp(loopData.getTimestamp(), loopData.getCPUUsage());
                monitorController.updateSystemTemp(loopData.getTimestamp(), loopData.getSystemTemp());
            });

            try {
                // Sleep for polling rate.
                TimeUnit.MILLISECONDS.sleep(3000); //TODO configure this with settings
                System.out.println("Monitor Awakened");
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
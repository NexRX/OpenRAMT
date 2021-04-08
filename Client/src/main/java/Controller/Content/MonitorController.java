package Controller.Content;

import Controller.Library.Services.MonitoringService;
import Controller.RootController;
import Model.Misc.HardDiskItem;
import Model.Misc.SoftDiskItem;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The controller class for monitoring related work and display.
 */
public class MonitorController extends ScrollPane {
    // Series Chart Data //
    //CPU
    private static final XYChart.Series<String, Number> cpuUsageSeries = new XYChart.Series<>();

    // RAM
    private static final PieChart.Data[] ramData = new PieChart.Data[2];

    // Drives
    private static final XYChart.Series<Number, String> diskCapacitySeries = new XYChart.Series<>();
    private static final XYChart.Series<Number, String> diskSpaceSeries = new XYChart.Series<>();
    private static final ArrayList<XYChart.Series<String, Number>> ioSeries = new ArrayList<>(); //ArrayList might not be need and can be internalised in the chart itself.
    private static long maxDisk = 1;

    //Temps
    private static final XYChart.Series<String, Number> cpuTempSeries = new XYChart.Series<>();
    private static final XYChart.Series<String, Number> systemTempSeries = new XYChart.Series<>();

    private final MonitoringService monitoringService = new MonitoringService(RootController.getLoggedInUser(), this);

    // FXML
    @FXML FlowPane container;

    // Charts
    @FXML AreaChart<String, Number> acCPUUsage;
    @FXML PieChart pieRAMUsage;

    @FXML StackedBarChart<Number, String> barDiskSpace;
    @FXML NumberAxis axisDiskSize;

    @FXML AreaChart<String, Number> acDiskIO;

    @FXML AreaChart<String, Number> acCPUTemp;
    @FXML AreaChart<String, Number> acSystemTemp;

    // Extra
    @FXML VBox containerRestart;
    @FXML JFXButton btnRestart;


    /**
     * Controller for monitoring work and display.
     */
    public MonitorController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/Monitor.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        container.prefWidthProperty().bind(this.widthProperty());
        //container.prefHeightProperty().bind(this.heightProperty());

        // Defining a series to display data
        cpuUsageSeries.setName("CPU Usage");

        ramData[0] = new PieChart.Data("Used", 0);
        ramData[1] = new PieChart.Data("Remaining", 0);

        ramData[0].setName("Used");
        ramData[1].setName("Free");

        // Setup Series/Data
        acCPUUsage.getData().add(cpuUsageSeries);

        pieRAMUsage.getData().add(ramData[0]);
        pieRAMUsage.getData().add(ramData[1]);

        diskCapacitySeries.setName("Used");
        diskSpaceSeries.setName("Free (Total - Used)");
        barDiskSpace.getData().add(diskCapacitySeries); // An addAll potentially unsafe with
        barDiskSpace.getData().add(diskSpaceSeries);    // generics with varargs with warning.
        axisDiskSize.setAutoRanging(false);
        axisDiskSize.setLowerBound(0);
        axisDiskSize.setUpperBound(maxDisk);
        axisDiskSize.setTickUnit(1);

        acCPUTemp.getData().add(cpuTempSeries);

        acSystemTemp.getData().add(systemTempSeries);

        // Extra
        containerRestart.prefWidthProperty().bind(container.widthProperty());

        // Start Service
        applyEventHandlers();

        startMonitoringService();
    }

    private void applyEventHandlers() {
        btnRestart.setOnMouseClicked(e -> {
            stopMonitoringService();
            clearCurrentData();
            startMonitoringService();
        });
    }

    /**
     * Clears all the series data from the charts.
     */
    private void clearCurrentData() {
        cpuUsageSeries.getData().clear();
        diskCapacitySeries.getData().clear();
        diskSpaceSeries.getData().clear();
        for (XYChart.Series<String, Number> series :ioSeries) {
            series.getData().clear();
        }
        cpuTempSeries.getData().clear();
        systemTempSeries.getData().clear();
    }

    /**
     * Restarts the server.
     */
    public void startMonitoringService() {
        monitoringService.restart();
    }

    /**
     * Closes the sockets in use for monitoring and cancels the service's task.
     */
    public void stopMonitoringService() {
        monitoringService.closeSockets();
        monitoringService.cancel();
    }

    /**
     * Updates the CPU usage of the server.
     * @param time A timestamp from the server when the data was recorded.
     * @param value the CPU usage of the server from the timestamp.
     */
    public void updateCPUUsage(String time, Integer value) {
        cpuUsageSeries.getData().add(new XYChart.Data<>(time, value));
        if (cpuUsageSeries.getData().size() > 30)
            cpuUsageSeries.getData().remove(0);
    }

    /**
     * Updates the ram usage pie chart values.
     * @param used total physical memory used.
     * @param total total physical memory.
     */
    public void updateRAMUsage(long used, long total) {
        double usedPercent = (used * 1d / total /*+ 0.5 prevents truncating*/);
        double freePercent = 1d - usedPercent;
        ramData[0].setPieValue(freePercent);
        ramData[1].setPieValue(usedPercent);
    }

    /**
     * Updates all series of disk spaces. Expanding and contracting data if the servers information somehow changes.
     * @param disks The array of SoftDiskItem objects used to update the Series data.
     */
    public void updateDiskSpaces(SoftDiskItem[] disks) {
        int i = 0;
        for (SoftDiskItem disk : disks) {
            if (disk.getCapacity() > maxDisk) {
                maxDisk = disk.getCapacity();
                axisDiskSize.setUpperBound(maxDisk);
                axisDiskSize.setTickUnit(maxDisk / 3.9999); // upperbounds doesn't display correctly otherwise
            }
            try {
                diskSpaceSeries.getData().set(i, new XYChart.Data<>(disk.getSpace(), disk.getDiskIdentifier()));
                diskCapacitySeries.getData().set(i, new XYChart.Data<>(disk.getCapacity() - disk.getSpace(), disk.getDiskIdentifier()));
            } catch (IndexOutOfBoundsException e) {
                diskSpaceSeries.getData().add(i, new XYChart.Data<>(disk.getSpace(), disk.getDiskIdentifier()));
                diskCapacitySeries.getData().add(i, new XYChart.Data<>(disk.getCapacity() - disk.getSpace(), disk.getDiskIdentifier()));
            }

            if (diskSpaceSeries.getData().size() > 30)
                diskSpaceSeries.getData().remove(0);
            i++;
        }
    }

    /**
     * Updates all series of disk IO. Expanding and contracting data if the servers information somehow changes.
     * @param disks The array of HardDiskItem objects used to update the Series data.
     * @param timestamp A timestamp from the server when the data was recorded.
     */
    public void updateDiskIOs(HardDiskItem[] disks, String timestamp) {
        if (ioSeries.size() == 0) { // initialise if device is added later will need initialising  in else if maybe
            for (int i = 0; i < disks.length; i++) {
                XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
                newSeries.getData().add(new XYChart.Data<>(timestamp, 0));
                newSeries.setName(disks[i].getDiskIdentifier());
                ioSeries.add(i, newSeries);
                acDiskIO.getData().add(i, newSeries);
            }
        } else if (ioSeries.size() < disks.length) {
            for (int i = ioSeries.size()-1; i < disks.length; i++) {
                XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
                ioSeries.add(i, newSeries);
                acDiskIO.getData().add(i, newSeries);
            }
        } else if (ioSeries.size() > disks.length) {
            if (ioSeries.size() > disks.length + 1) {
                ioSeries.subList(disks.length + 1, ioSeries.size()).clear();
            }
        }

        int i = 0;
        for (HardDiskItem disk : disks) {
            ioSeries.get(i).getData().add(new XYChart.Data<>(timestamp, disk.getDiskIO()));
            if (ioSeries.get(i).getData().size() > 30)
                ioSeries.get(i).getData().remove(0);
            i++;
        }
    }

    /**
     * Updates the series with new data for CPU temp.
     * @param time The timestamp from when the data was taken.
     * @param value The temperature in Celsius of the CPU. As a whole/average, most expectedly it's packet temperature.
     */
    public void updateCPUTemp(String time, Integer value) {
        cpuTempSeries.getData().add(new XYChart.Data<>(time, value));
        if (cpuTempSeries.getData().size() > 30)
            cpuTempSeries.getData().remove(0);
    }

    /**
     * Updates the series with new data for system temp.
     * @param time The timestamp from when the data was taken.
     * @param value The system temperature for the server. Should be an average of all temperatures or motherboard's.
     */
    public void updateSystemTemp(String time, Integer value) {
        systemTempSeries.getData().add(new XYChart.Data<>(time, value));
        if (systemTempSeries.getData().size() > 30)
            systemTempSeries.getData().remove(0);
    }
}

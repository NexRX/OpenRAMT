package Controller.Content;

import Controller.Library.Services.MonitoringService;
import Controller.RootController;
import Model.Misc.HardDiskItem;
import Model.Misc.SoftDiskItem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.util.ArrayList;

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

    private final MonitoringService monitoringService = new MonitoringService(RootController.getLoggedInUser(), this);;

    // FXML ??
    @FXML FlowPane container;

    // Charts
    @FXML AreaChart<String, Number> acCPUUsage;
    @FXML PieChart pieRAMUsage;

    @FXML StackedBarChart<Number, String> barDiskSpace;
    @FXML NumberAxis axisDiskSize;

    @FXML AreaChart<String, Number> acDiskIO;

    @FXML AreaChart<String, Number> acCPUTemp;

    @FXML AreaChart<String, Number> acSystemTemp;


    /**
     * Constructs the VBox and loads its FXML file.
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

        // Start Service
        monitoringService.restart();
    }

    public void startMonitoringService() {
        monitoringService.restart();
    }

    public void stopMonitoringService() {
        monitoringService.cancel();
    }

    public void updateCPUUsage(String time, Integer value) {
        cpuUsageSeries.getData().add(new XYChart.Data<>(time, value));
    }

    public void updateRAMUsage(long used, long total) {
        double usedPercent = (used * 1d / total /*+ 0.5 prevents truncating*/);
        double freePercent = 1d - usedPercent;
        ramData[0].setPieValue(freePercent);
        ramData[1].setPieValue(usedPercent);
    }

    /*public void updateDisks(ArrayList<DiskItem> disks) {
        int i = 0;
        for (DiskItem disk : disks) {
            UPDATE BOTH SPACES AND IO HERE
        }
    }*/

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
            i++;
        }
    }

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
            for (int i = ioSeries.size()-1; i > disks.length; i--) {
                ioSeries.remove(i);
            }
        }

        int i = 0;
        for (HardDiskItem disk : disks) {
            ioSeries.get(i).getData().add(new XYChart.Data<>(timestamp, disk.getDiskIO()));
            i++;
        }
    }

    public void updateCPUTemp(String time, Integer value) {
        cpuTempSeries.getData().add(new XYChart.Data<>(time, value));
    }

    public void updateSystemTemp(String time, Integer value) {
        systemTempSeries.getData().add(new XYChart.Data<>(time, value));
    }
}

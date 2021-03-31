package Controller.Content;

import Controller.RAMTAlert;
import Controller.RootController;
import Model.Task.Task;
import Model.Task.TaskRequest;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;

import java.io.IOException;

public class GeneralController extends ScrollPane {

    @FXML JFXButton btnFTPStart;
    @FXML JFXButton btnFTPStop;
    @FXML JFXButton btnFTPRestart;

    @FXML JFXButton btnCleanDiskSystem;
    @FXML JFXButton btnCleanDiskExtra;
    @FXML JFXButton btnCleanDiskAll;
    @FXML JFXButton btnCleanDiskBin;

    @FXML JFXButton btnWiFiEnable;
    @FXML JFXButton btnWiFiDisable;
    @FXML JFXButton btnWiFiReenable;

    @FXML JFXButton btnBluetoothEnable;
    @FXML JFXButton btnBluetoothDisable;
    @FXML JFXButton btnBluetoothReenable;


    /**
     * Constructs the VBox and loads its FXML file.
     */
    public GeneralController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/General.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        applyEventHandlers();
    }

    private void applyEventHandlers() {
        // FTP Section
        btnFTPStart.setOnMouseClicked(event -> requestStart(new TaskRequest<>(Task.STARTFTP, RootController.getLoggedInUser())));
        btnFTPStop.setOnMouseClicked(event -> requestStart(new TaskRequest<>(Task.STOPFTP, RootController.getLoggedInUser())));
        btnFTPRestart.setOnMouseClicked(event -> requestStart(new TaskRequest<>(Task.RESTARTFTP, RootController.getLoggedInUser())));

        // General Section
        btnCleanDiskSystem.setOnMouseClicked(event -> requestStart(new TaskRequest<>(Task.CLEANDISK, RootController.getLoggedInUser(), 0)));
        btnCleanDiskExtra.setOnMouseClicked(event -> requestStart(new TaskRequest<>(Task.CLEANDISK, RootController.getLoggedInUser(), 1)));
        btnCleanDiskAll.setOnMouseClicked(event -> requestStart(new TaskRequest<>(Task.CLEANDISK, RootController.getLoggedInUser(), 2)));
        btnCleanDiskBin.setOnMouseClicked(event -> requestStart(new TaskRequest<>(Task.CLEANDISK, RootController.getLoggedInUser(), 3)));

        // WiFi Section
        btnWiFiEnable.setOnMouseClicked(event -> requestStart(new TaskRequest<>(Task.ENABLEWIFI, RootController.getLoggedInUser(), 0)));
        btnWiFiDisable.setOnMouseClicked(event -> requestStart(new TaskRequest<>(Task.DISABLEWIFI, RootController.getLoggedInUser())));
        btnWiFiReenable.setOnMouseClicked(event -> requestStart(new TaskRequest<>(Task.ENABLEWIFI, RootController.getLoggedInUser(), 1)));

        // Bluetooth Section
        btnBluetoothEnable.setOnMouseClicked(event -> requestStart(new TaskRequest<>(Task.ENABLEBLUETOOTH, RootController.getLoggedInUser(), 0)));
        btnBluetoothDisable.setOnMouseClicked(event -> requestStart(new TaskRequest<>(Task.DISABLEBLUETOOTH, RootController.getLoggedInUser())));
        btnBluetoothReenable.setOnMouseClicked(event -> requestStart(new TaskRequest<>(Task.ENABLEBLUETOOTH, RootController.getLoggedInUser(), 1)));
    }

    private void requestStart(TaskRequest<?> taskRequest) {
        if (RootController.getTaskService().isRunning()) {
            Alert alert = RAMTAlert.getAlertMidTask();
            alert.showAndWait();

            if (alert.getResult() == ButtonType.OK) {
                RootController.getTaskService().updateAndRestart(taskRequest);
            }
        } else {
            RootController.getTaskService().updateAndRestart(taskRequest);
        }
    }
}

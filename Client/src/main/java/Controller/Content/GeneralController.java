package Controller.Content;

import Controller.RAMTAlert;
import Controller.RootController;
import Model.Task.Task;
import Model.Task.TaskRequest;
import com.jfoenix.controls.JFXButton;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class GeneralController extends ScrollPane {

    @FXML JFXButton btnFTPStart;
    @FXML JFXButton btnFTPStop;
    @FXML JFXButton btnFTPRestart;

    @FXML JFXButton btnCleanDiskSystem;
    @FXML JFXButton btnCleanDiskExtra;
    @FXML JFXButton btnCleanDiskAll;
    @FXML JFXButton btnCleanDiskBin;

    @FXML JFXButton btnWIFIEnable;
    @FXML JFXButton btnWIFIDisable;
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
        btnFTPStart.setOnMouseClicked(requestStart(new TaskRequest<>(Task.STARTFTP, RootController.getLoggedInUser())));
        btnFTPStop.setOnMouseClicked(requestStart(new TaskRequest<>(Task.STOPFTP, RootController.getLoggedInUser())));
        btnFTPRestart.setOnMouseClicked(requestStart(new TaskRequest<>(Task.RESTARTFTP, RootController.getLoggedInUser())));

        // General Section
        btnCleanDiskSystem.setOnMouseClicked(requestStart(new TaskRequest<>(Task.CLEANDISK, RootController.getLoggedInUser(), 0)));
        btnCleanDiskExtra.setOnMouseClicked(requestStart(new TaskRequest<>(Task.CLEANDISK, RootController.getLoggedInUser(), 1)));
        btnCleanDiskAll.setOnMouseClicked(requestStart(new TaskRequest<>(Task.CLEANDISK, RootController.getLoggedInUser(), 2)));
        btnCleanDiskBin.setOnMouseClicked(requestStart(new TaskRequest<>(Task.CLEANDISK, RootController.getLoggedInUser(), 3)));

        // WIFI Section
        btnWIFIEnable.setOnMouseClicked(requestStart(new TaskRequest<>(Task.ENABLEWIFI, RootController.getLoggedInUser(), 0)));
        btnWIFIDisable.setOnMouseClicked(requestStart(new TaskRequest<>(Task.DISABLEWIFI, RootController.getLoggedInUser())));
        btnWiFiReenable.setOnMouseClicked(requestStart(new TaskRequest<>(Task.ENABLEWIFI, RootController.getLoggedInUser(), 1)));

        // Bluetooth Section
        btnBluetoothEnable.setOnMouseClicked(requestStart(new TaskRequest<>(Task.ENABLEBLUETOOTH, RootController.getLoggedInUser(), 0)));
        btnBluetoothDisable.setOnMouseClicked(requestStart(new TaskRequest<>(Task.DISABLEBLUETOOTH, RootController.getLoggedInUser())));
        btnBluetoothReenable.setOnMouseClicked(requestStart(new TaskRequest<>(Task.ENABLEBLUETOOTH, RootController.getLoggedInUser(), 1)));
    }

    private EventHandler<? super MouseEvent> requestStart(TaskRequest<?> taskRequest) {
        if (RootController.getTaskService().isRunning()) {
            Alert alert = RAMTAlert.getAlertMidTask();
            alert.showAndWait();

            if (alert.getResult() == ButtonType.OK) {
                RootController.getTaskService().updateAndRestart(taskRequest);

            }
        } else {
            RootController.getTaskService().updateAndRestart(taskRequest);
        }
        return null;
    }
}

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
        btnFTPStart.setOnMouseClicked(event -> {
            if (RootController.getTaskService().isRunning()) {
                Alert alert = RAMTAlert.getAlertMidTask();
                alert.showAndWait();

                if (alert.getResult() == ButtonType.OK) {
                    RootController.getTaskService().updateAndRestart(
                            new TaskRequest<>(Task.STARTFTP, RootController.getLoggedInUser()));

                }
            } else {
                RootController.getTaskService().updateAndRestart(
                        new TaskRequest<>(Task.STARTFTP, RootController.getLoggedInUser()));
            }
        });

        btnFTPStop.setOnMouseClicked(event -> {
            if (RootController.getTaskService().isRunning()) {
                Alert alert = RAMTAlert.getAlertMidTask();
                alert.showAndWait();

                if (alert.getResult() == ButtonType.OK) {
                    RootController.getTaskService().updateAndRestart(
                            new TaskRequest<>(Task.STOPFTP, RootController.getLoggedInUser()));

                }
            } else {
                RootController.getTaskService().updateAndRestart(
                        new TaskRequest<>(Task.STOPFTP, RootController.getLoggedInUser()));
            }
        });

        btnFTPRestart.setOnMouseClicked(event -> {
            if (RootController.getTaskService().isRunning()) {
                Alert alert = RAMTAlert.getAlertMidTask();
                alert.showAndWait();

                if (alert.getResult() == ButtonType.OK) {
                    RootController.getTaskService().updateAndRestart(
                            new TaskRequest<>(Task.RESTARTFTP, RootController.getLoggedInUser()));

                }
            } else {
                RootController.getTaskService().updateAndRestart(
                        new TaskRequest<>(Task.RESTARTFTP, RootController.getLoggedInUser()));
            }
        });
    }
}

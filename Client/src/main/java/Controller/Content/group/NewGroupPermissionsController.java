package Controller.Content.group;

import Controller.RAMTAlert;
import Controller.RootController;
import Model.Task.Task;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.IOException;

@SuppressWarnings("unchecked") // Safe when server & client respects request/response structure.
public class NewGroupPermissionsController extends AnchorPane {
    Stage stage;
    Stage callingStage;
    private double xOffset = 0;
    private double yOffset = 0;

    private String lastRequestID  = "";

    // Window Controls
    @FXML Pane topBar;
    @FXML JFXButton btnClose;

    @FXML Label lblGroupValue;

    //Group Toggles
    @FXML JFXToggleButton toggleBtnAdmin;
    @FXML JFXToggleButton toggleBtnGeneral;
    @FXML JFXToggleButton toggleBtnProcess;
    @FXML JFXToggleButton toggleBtnMonitor;
    @FXML JFXToggleButton toggleBtnPower;

    //Submit
    @FXML JFXButton btnSubmit;

    public NewGroupPermissionsController(Stage stage, Stage callingStage, String groupName) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/Group/Permissions.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.stage = stage;
        this.callingStage = callingStage;
        lblGroupValue.setText(groupName);

        applyEventHandlers();
    }

    private void applyEventHandlers() {
        topBar.setOnMousePressed(event -> { // These next two control window movement
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        topBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        btnClose.setOnMouseClicked(event -> returnToCaller());
        stage.setOnCloseRequest(event -> returnToCaller());
        btnSubmit.setOnMouseClicked(event -> lastRequestID = RootController.requestStart(new TaskRequest<>(
                Task.UPDATEGROUP,
                RootController.getLoggedInUser(),
                new String[]{"permissions", lblGroupValue.getText(),
                        String.valueOf(toggleBtnAdmin.isSelected()),
                        String.valueOf(toggleBtnGeneral.isSelected()),
                        String.valueOf(toggleBtnProcess.isSelected()),
                        String.valueOf(toggleBtnMonitor.isSelected()),
                        String.valueOf(toggleBtnPower.isSelected())})));

        RootController.getTaskService().addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            if (lastRequestID.equals(RootController.getTaskService().getRequest().getRequestID())) { // Our Request?
                Alert alert;
                Alert.AlertType type;
                String alertMessage;
                boolean exit = false;

                switch (((TaskResponse<Void>) event.getSource().getValue()).getResponseCode()) {
                    case 0 -> {
                        type = Alert.AlertType.INFORMATION;
                        alertMessage = "Operation completed successfully.";
                        exit = true;
                    }
                    case 1 -> {
                        type = Alert.AlertType.INFORMATION;
                        alertMessage = "One or more group names couldn't be identified";
                    }
                    case 2 -> {
                        type = Alert.AlertType.WARNING;
                        alertMessage = "One or more group names are invalid.";
                    }
                    case 3 -> {
                        type = Alert.AlertType.WARNING;
                        alertMessage = "That group is reserved by the application.";
                    }
                    default -> {
                        type = Alert.AlertType.ERROR;
                        alertMessage = "Couldn't handle the request. Please double check everything and try again.";
                    }
                }
                alert = new RAMTAlert(type, "Form Message", alertMessage, "Click a button to continue.");
                alert.showAndWait();

                if (exit) {
                    returnToCaller();
                }
            }
        });

    }

    private void returnToCaller() {
        callingStage.show();
        stage.close();
    }
}

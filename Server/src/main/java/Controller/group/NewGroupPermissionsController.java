package Controller.group;

import Controller.Database.DBManager;
import Controller.RAMTAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * This class is the controller class for updating/changing the permissions of a group.
 */
public class NewGroupPermissionsController extends AnchorPane {
    private final Stage stage;
    private final Stage callingStage;

    private double xOffset = 0;
    private double yOffset = 0;

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

    /**
     * The controller for updating/changing the permissions of a group.
     * @param stage A stage hosting this controller. It will be closed when requested by the user.
     * @param callingStage The stage to return to after this controller is done.
     * @param groupName The group name to modify.
     */
    public NewGroupPermissionsController(Stage stage, Stage callingStage, String groupName) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Group/Permissions.fxml"));
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

        btnSubmit.setOnMouseClicked(event -> {
            Alert alert = new RAMTAlert(Alert.AlertType.NONE);
            Alert.AlertType type;
            String alertMessage;
            boolean exit = false;

            try {
                switch (DBManager.updatePermissions(lblGroupValue.getText(),
                        toggleBtnAdmin.isSelected(),
                        toggleBtnGeneral.isSelected(),
                        toggleBtnProcess.isSelected(),
                        toggleBtnMonitor.isSelected(),
                        toggleBtnPower.isSelected())) {
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
                alert.setAlertType(type);
                alert.setTitle("Form Message");
                alert.setHeaderText(alertMessage);
                alert.setContentText("Click a button to continue.");
                alert.showAndWait();

                if (exit) {returnToCaller(); }
            } catch (SQLException e) {
                Alert alertError = new RAMTAlert(Alert.AlertType.ERROR,
                        "Application Error.",
                        "The application ran into a error with that request.",
                        "Please notify an admin. The error was an 'SQLException' if an admin asks.");
                alertError.showAndWait();

                e.printStackTrace();
                System.exit(1);
            }
        });

    }

    private void returnToCaller() {
        callingStage.show();
        stage.close();
    }
}

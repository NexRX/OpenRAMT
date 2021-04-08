package Controller.user;

import Controller.Database.DBManager;
import Controller.RAMTAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
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
 * Controller Class for updating usernames.
 */
public class NewUsernameController extends AnchorPane {
    Stage stage;
    Stage callingStage;

    private double xOffset = 0;
    private double yOffset = 0;

    //Window Controls
    @FXML JFXButton btnClose;
    @FXML Pane topBar;

    //Form Controls
    @FXML Label lblUserValue;

    @FXML JFXTextField txtName;

    //Submit
    @FXML JFXButton btnSubmit;

    /**
     * The controller for moving a user to a different pre-existing group.
     * @param stage A stage hosting this controller. It will be closed when requested by the user.
     * @param callingStage The stage to return to after this controller is done.
     * @param username The username of the user to update their name.
     */
    public NewUsernameController(Stage stage, Stage callingStage, String username) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/User/Username.fxml"));
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
        lblUserValue.setText(username);

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
                switch (DBManager.updateUsername(lblUserValue.getText(), txtName.getText())) {
                    case 0 -> {
                        type = Alert.AlertType.INFORMATION;
                        alertMessage = "Operation completed successfully.";
                        exit = true;
                    }
                    case 1 -> {
                        type = Alert.AlertType.INFORMATION;
                        alertMessage = "The user already exists or is reserved.";
                    }
                    case 2 -> {
                        type = Alert.AlertType.WARNING;
                        alertMessage = "One or more fields are invalid.";
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


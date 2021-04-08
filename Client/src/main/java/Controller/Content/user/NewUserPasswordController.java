package Controller.Content.user;

import Controller.CryptographyToolbox;
import Controller.RAMTAlert;
import Model.Task.Task;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static Controller.RootController.*;

/**
 * Controller class for updating passwords of a given user.
 */
@SuppressWarnings("unchecked") // Safe when server & client respects request/response structure.
public class NewUserPasswordController extends AnchorPane {
    Stage stage;
    Stage callingStage;
    private double xOffset = 0;
    private double yOffset = 0;

    private String lastRequestID  = "";

    //Window Controls
    @FXML JFXButton btnClose;
    @FXML Pane topBar;

    @FXML Label lblUserValue;

    //Form Controls
    @FXML JFXPasswordField txtPassword;

    //Submit
    @FXML JFXButton btnSubmit;

    /**
     * Controller for updating passwords of given users.
     * @param stage A stage hosting this controller. It will be closed when requested by the user.
     * @param callingStage The stage to return to after this controller is done.
     * @param username The username of the user to update their password.
     */
    public NewUserPasswordController(Stage stage, Stage callingStage, String username) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/User/Password.fxml"));
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
                    try {
                        lastRequestID = requestStart(new TaskRequest<>(
                                Task.UPDATEUSER,
                                getLoggedInUser(),
                                new String[]{"password", lblUserValue.getText(), CryptographyToolbox.generatePBKDF2WithHmacSHA512(txtPassword.getText())}));
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                        e.printStackTrace();
                    }
                }
        );

        getTaskService().addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            if (lastRequestID.equals(getTaskService().getRequest().getRequestID())) { // Our Request?
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

                alert = new RAMTAlert(type,"OpenRAMT Form Message", alertMessage,"Click a button to continue.");
                alert.showAndWait();

                if (exit) { returnToCaller(); }
            }
        });


    }

    private void returnToCaller() {
        callingStage.show();
        stage.close();
    }
}



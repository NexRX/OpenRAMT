package Controller;

import Controller.Library.Services.LoginProgressibleService;
import Model.UserData;
import application.Launcher;
import application.Launcher.MainStart;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
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

public class InitialController extends AnchorPane {
    private final Stage stage;

    @FXML JFXProgressBar jfxProgress;
    @FXML JFXButton btnClose;
    @FXML JFXTextField loginHost;
    @FXML JFXTextField loginUsername;
    @FXML JFXPasswordField loginPassword;
    @FXML JFXButton btnLogin;
    @FXML Label lblProgress;
    @FXML Pane topBar;

    private double xOffset = 0;
    private double yOffset = 0;

    private UserData user = new UserData(null, 0, null, null);
    private final LoginProgressibleService loginTask;


    public InitialController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Init.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        loginTask = new LoginProgressibleService(user);
        jfxProgress.progressProperty().bind(loginTask.getProgressProperty());

        this.stage = Launcher.MainStart.getStage();

        applyEventHandlers();
    }

    private void applyEventHandlers() {
        btnClose.setOnMouseClicked(event -> {
            Platform.exit();
            System.exit(0);
        });

        this.setOnMousePressed(event -> { // These next two control window movement
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        this.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        btnLogin.setOnMouseClicked(event -> {

            if (loginHost.getText().indexOf(':') > -1) { // <-- does it contain ":"?
                String[] arr = loginHost.getText().split(":");
                String host = arr[0];
                try {
                    int port = Integer.parseInt(arr[1]);
                    user = new UserData(host, port, loginUsername.getText(), loginPassword.getText()); //ToDo look into encrypting password to decrypt on other side (added security during traffic). (1)
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else {
                //Todo let user know port is no good and now trying default.

                user = new UserData(loginHost.getText(), loginUsername.getText(), loginPassword.getText()); //ToDo same here . (1)

            }

            loginTask.updateUser(user);
            loginTask.restart();
            /* loginTask.wait(); <- This MIGHT make it thread safe */ //Todo find out if this is true or is not needed.
        });

        loginTask.setOnSucceeded(event -> {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setAlertType(Alert.AlertType.ERROR);

            switch (loginTask.getValue()) {
                case SUCCESS -> {
                    MainStart.rootScene();
                    System.out.println("Switching");
                }
                case FAILED_CONNECTION -> {
                    a.setContentText("The connection to the host failed.");
                    a.show();
                }
                case FAILED_USERNAME -> {
                    a.setContentText("Authentication failed. Username not found.");
                    a.show();
                }
                case FAILED_PASSWORD -> {
                    a.setContentText("Authentication failed. Incorrect password given.");
                    a.show();
                }
                case FAILED_SUSPENDED -> {
                    a.setContentText("Authentication failed. The account is suspended.");
                    a.show();
                }
                default -> {
                    //Todo make this check redundant by producing only known errors.
                    a.setContentText("A error has occurred while logging in within the application.");
                    a.show();
                }
            }
        });
    }
}
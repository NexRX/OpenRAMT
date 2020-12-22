package Controller;

import Controller.Services.LoginProgressibleService;
import Model.UserData;
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

import java.io.IOException;

public class InitialController extends AnchorPane {
    @FXML JFXProgressBar jfxProgress;
    @FXML JFXButton btnClose;
    @FXML JFXTextField loginHost;
    @FXML JFXTextField loginUsername;
    @FXML JFXPasswordField loginPassword;
    @FXML JFXButton btnLogin;
    @FXML Label lblProgress;
    private UserData user = new UserData(null, 0, null,null);
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

        applyEventHandlers();
    }

    private void applyEventHandlers() {
        btnClose.setOnMouseClicked(event -> {
            Platform.exit();
            System.exit(0);
        });

        btnLogin.setOnMouseClicked(event -> {
            if (loginHost.getText().indexOf(':') > -1) { // <-- does it contain ":"?
                String[] arr = loginHost.getText().split(":");
                String host = arr[0];
                try {
                    int port = Integer.parseInt(arr[1]);
                    user = new UserData(host, port, loginUsername.getText(), loginPassword.getText());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else {
                user = new UserData(loginHost.getText(), loginUsername.getText(), loginPassword.getText());
            }
            loginTask.updateUser(user);
            loginTask.restart();
        });

        loginTask.setOnSucceeded(event -> {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setAlertType(Alert.AlertType.ERROR);
            switch (loginTask.getValue()) {
                case SUCCESS:
                    MainStart.rootScene();
                    System.out.println("Switching");
                    break;
                case FAILED_CONNECTION:
                    a.setContentText("The connection to the host failed.");
                    a.show();
                    break;
                case FAILED_AUTHENTICATION:
                    a.setContentText("Authentication failed. Please check user details.");
                    a.show();
                    break;
            }
        });
    }
}
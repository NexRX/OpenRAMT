package Controller;

import Controller.Library.Services.LoginProgressiveService;
import Model.User.UserData;
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

public class InitialController extends AnchorPane {
    private final Stage stage;

    @FXML JFXProgressBar jfxProgress;
    @FXML JFXButton btnClose;
    @FXML JFXTextField loginHost;
    @FXML JFXTextField loginUsername;
    @FXML JFXPasswordField loginPassword;
    @FXML JFXButton btnLogin;
    @FXML Label lblProgress;
    @FXML Label lblSecure;
    @FXML Pane topBar;

    private double xOffset = 0;
    private double yOffset = 0;

    private boolean secure = true;

    private UserData user = new UserData(null, 0, null, null, true);
    private final LoginProgressiveService loginTask;


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

        loginTask = new LoginProgressiveService(user);
        jfxProgress.progressProperty().bind(loginTask.getProgressProperty());

        this.stage = Launcher.MainStart.getStage();

        applyEventHandlers();
    }

    private void applyEventHandlers() {
        btnClose.setOnMouseClicked(event -> {
            Platform.exit();
            System.exit(0);
        });

        topBar.setOnMousePressed(event -> { // These next two control window movement
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        topBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        lblSecure.setOnMouseClicked(event -> {
            if (secure) {
                lblSecure.setStyle("-fx-text-fill:red;");
                lblSecure.setText("\uD83D\uDD13");
            } else {
                lblSecure.setStyle("-fx-text-fill:lime;");
                lblSecure.setText("\uD83D\uDD12");
            }

            secure = !secure;
            loginTask.setSecure(secure);
        });

        btnLogin.setOnMouseClicked(event -> {

            if (loginHost.getText().indexOf(':') > -1) { // <-- does it contain ":"?
                System.out.println("Port found in " + loginHost.getText());
                String[] arr = loginHost.getText().split(":");
                String host = arr[0];
                try {
                    int port = Integer.parseInt(arr[1]);
                    System.out.print(port);
                    user = new UserData(host, port, loginUsername.getText(), loginPassword.getText(), secure);
                } catch (NumberFormatException e) {
                    Alert alert = new RAMTAlert(Alert.AlertType.WARNING,
                            "Settings Warning",
                            "Incorrect port given",
                            "Invalid port was given so default was attempted instead.\n" +
                                    "The port should follow the ip address with semi-colon i.e. 127.0.0.1:1234");
                    alert.showAndWait();
                }
            } else {
                user = new UserData(loginHost.getText(), loginUsername.getText(), loginPassword.getText(), secure);
            }

            loginTask.updateUser(user);
            loginTask.restart();
            /* loginTask.wait(); <- This MIGHT make it thread safe if not already */
        });

        loginTask.setOnSucceeded(event -> {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setAlertType(Alert.AlertType.ERROR);

            switch (loginTask.getValue()) {
                case SUCCESS -> {
                    MainStart.rootScene(user);
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
                    a.setContentText("A error has occurred while logging in within the application.");
                    a.show();
                }
            }
        });
    }
}
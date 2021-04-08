package Controller;

import Controller.Library.Services.LoginProgressiveService;
import Model.Task.TaskResponse;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Starting point for the client class.
 */
public class InitialController extends AnchorPane {
    private final Stage stage;

    private double xOffset = 0;
    private double yOffset = 0;

    private final FileChooser fileChooser = new FileChooser();

    private boolean secure = true;

    private UserData user = new UserData(null, 0, null, null, true);
    private final LoginProgressiveService loginTask;

    @FXML JFXProgressBar jfxProgress;
    @FXML JFXButton btnClose;
    @FXML JFXButton btnCert;
    @FXML JFXTextField loginHost;
    @FXML JFXTextField loginUsername;
    @FXML JFXPasswordField loginPassword;
    @FXML JFXButton btnLogin;

    //@FXML Label lblProgress;
    @FXML Label lblSecure;
    @FXML Pane topBar;

    /**
     * The constructor for starting an instance where the user will be able to attempt to login to the server.
     * After the user is logged in it will proceed to call the related methods to move to the RootController.
     */
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

        btnCert.setOnMouseClicked(e -> {
            new RAMTAlert(Alert.AlertType.INFORMATION,
                    "OpenRAMT Information",
                    "Import information, please read the following!",
                    "Only use this setting if your importing a servers keystore file. This will prevent any" +
                            " secure connections from working unless the server has a identical copy of this file.\n\n" +
                            "Please note that you will need to go into the installation directory of this application " +
                            "and delete the keystore.jks file to go back to the default one provided.").showAndWait();

            File srcFile = fileChooser.showOpenDialog(stage);

            if (srcFile != null) {
                try {
                    Files.copy(srcFile.toPath(),
                            (new File(new File("data/").toPath() + "keystore.jks")).toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ioException) {
                    new RAMTAlert(Alert.AlertType.ERROR,
                            "OpenRAMT Copy Error",
                            "That file couldn't be copied!",
                            "Please ensure the file is still there and isn't being used by another program.\n\n" +
                                    "You can also do this manually after setup by renaming the file to 'keystore.jks' and" +
                                    "copying it to the data folder where this application is installed.\n" +
                                    "The file must be in keystore(JKS) format and password protected with the" +
                                    "password 'jknm43c23C1EW342we'. It is not recommended to do this manually unless this" +
                                    "setup fails consistently.").showAndWait();
                }
            }
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
            Alert a = new RAMTAlert(Alert.AlertType.ERROR);
            a.setAlertType(Alert.AlertType.ERROR);
            TaskResponse<UserData> loginResponse = loginTask.getValue();

            switch (loginResponse.getResponseCode()) {
                case 0 -> {
                    MainStart.rootScene(new UserData(
                                    user.getHost(),
                                    user.getPort(),
                                    loginResponse.getResponseData().getID(),
                                    user.getUsername(),
                                    user.getPassword(),
                                    loginResponse.getResponseData().getGroup(),
                                    loginResponse.getResponseData().isSuspended(),
                                    user.isSecure(),
                                    loginResponse.getResponseData().getObjGroup(),
                                    loginResponse.getResponseData().getMonitoringPort(),
                                    loginResponse.getResponseData().getMacAddress()
                            )
                    );
                    System.out.println("Logged in.");
                }
                case 10 -> {
                    a.setContentText("Authentication failed. Username not found.");
                    a.show();
                }
                case 11 -> {
                    a.setContentText("Authentication failed. Incorrect password given.");
                    a.show();
                }
                case 12 -> {
                    a.setContentText("Authentication failed. The account is suspended.");
                    a.show();
                }
                // Some error (probably not serious). needs timeouts set (for retries here) tho.
                default -> {
                    a.setContentText("A error has occurred while logging in within the application. " +
                            "Mostly a connection could not be made.");
                    a.show();
                } // essentially retry and add progress
            }
        });
    }
}
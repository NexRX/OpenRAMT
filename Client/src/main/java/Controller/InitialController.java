package Controller;

import Model.UserData;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class InitialController extends AnchorPane {
    BackgroundThreader threads = new BackgroundThreader();
    Stage stage;
    @FXML JFXProgressBar jfxProgress;
    @FXML JFXButton btnClose;
    @FXML JFXTextField loginHost;
    @FXML JFXTextField loginUsername;
    @FXML JFXPasswordField loginPassword;
    @FXML JFXButton btnLogin;
    @FXML Label lblProgress;
    private UserData user;
    private Thread initThread;

    public InitialController(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Initial.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.stage = stage;



        applyEventHandlers();
    }

    private void applyEventHandlers() {
        btnClose.setOnMouseClicked(event -> {
            Platform.exit();
            System.exit(0);
        });

        btnLogin.setOnMouseClicked(event -> {
            LoginService lss = new LoginService();
            jfxProgress.progressProperty().bind(lss.getProgressProperty());
            lss.execute("Something");
        });
    }
}
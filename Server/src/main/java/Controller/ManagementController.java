package Controller;

import Controller.Socket.SecureServer;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ManagementController extends AnchorPane {
    private Stage stage;

    @FXML JFXButton btnClose;
    @FXML JFXButton btnMin;

    private double xOffset = 0;
    private double yOffset = 0;

    SecureServer server;

    public ManagementController(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Management.fxml"));
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
        serverStart();
    }

    private void serverStart() throws IOException {
        server = new SecureServer(); // Pass port later and check it.
        new Thread(server).start();
    }

    private void stopServer() {
        server.stop();
        server = null;
    }

    private void applyEventHandlers() {
        this.setOnMousePressed(event -> { // These next two control window movement
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        this.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        btnMin.setOnMouseClicked(event -> stage.setIconified(true));

        btnClose.setOnMouseClicked(event -> {
            stopServer();
            Platform.exit();
            System.exit(0);
        });
    }
}
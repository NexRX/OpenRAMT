package Controller;

import application.Launcher.MainStart;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SetupController extends AnchorPane {
    private Stage stage;
    @FXML JFXButton btnClose;
    @FXML JFXButton btnSetup;

    public SetupController(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Setup.fxml"));
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
        btnSetup.setOnMouseClicked(event -> {
            try {
                MainStart.mainScene();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        btnClose.setOnMouseClicked(event -> {
            Platform.exit();
            System.exit(0);
        });
    }
}
package Controller;

import Model.UserData;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
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
            jfxProgress.setProgress(0f);

            int initIndex = threads.addThread(initRun);
            threads.startThread(initIndex);

        });
    }

    Runnable initRun = new Runnable() { // http://tutorials.jenkov.com/javafx/concurrency.html
        @Override
        public void run() {
            double progress = 0;
            for(int i=0; i<10; i++){

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                progress += 0.1;
                final double reportedProgress = progress;

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        jfxProgress
                                .setProgress(reportedProgress);
                    }
                });
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    lblProgress.setText("Section Complete.");
                }

            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    stage.getScene().setRoot(new LauncherController(stage));
                    stage.setMinWidth(800);
                    stage.setMinHeight(450);
                }
            });
        }
    };
}
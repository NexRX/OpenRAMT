package Controller;

import Model.UserData;
import application.Launcher;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;
import Controller.LauncherController;

public class InitialController extends AnchorPane {
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

    private void progress(ReadOnlyDoubleProperty taskProgress) {
        jfxProgress.progressProperty().bind(taskProgress);
    }

    private void applyEventHandlers() {
        btnClose.setOnMouseClicked(event -> {
            Platform.exit();
            System.exit(0);
        });

        btnLogin.setOnMouseClicked(event -> {
            jfxProgress.setProgress(0f);


            initThread = starterThread;
            initThread.setDaemon(false);
            initThread.start();

        });
    }

    Thread starterThread = new Thread(new Runnable() { // http://tutorials.jenkov.com/javafx/concurrency.html
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
    });
}
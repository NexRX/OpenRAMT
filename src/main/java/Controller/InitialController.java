package Controller;

import com.jfoenix.controls.JFXProgressBar;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class InitialController extends AnchorPane {
    Stage stage;
    @FXML JFXProgressBar jfxProgress;

    public InitialController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Initial.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(jfxProgress.progressProperty(), 0)),
                new KeyFrame(Duration.minutes(1), e-> {
                    // do anything you need here on completion...
                    System.out.println("Minute over");
                }, new KeyValue(jfxProgress.progressProperty(), 1))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
}

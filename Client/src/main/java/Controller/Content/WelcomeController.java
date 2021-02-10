package Controller.Content;

import Controller.Library.SideButton;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.ArrayList;

public class WelcomeController extends AnchorPane {
    /**
     * Constructs the VBox and loads its FXML file.
     */
    public WelcomeController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/Welcome.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}

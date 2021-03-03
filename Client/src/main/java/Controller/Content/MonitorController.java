package Controller.Content;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class MonitorController extends ScrollPane {
    /**
     * Constructs the VBox and loads its FXML file.
     */
    public MonitorController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/Monitor.fxml"));
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

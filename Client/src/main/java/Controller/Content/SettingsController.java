package Controller.Content;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;

import java.io.IOException;

public class SettingsController extends ScrollPane {
    /**
     * Constructs the VBox and loads its FXML file.
     */
    public SettingsController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/Settings.fxml"));
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

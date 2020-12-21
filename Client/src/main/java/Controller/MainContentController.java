package Controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.TabPane;

import java.io.IOException;

/**
 * The Stack Displays controller. Handles all tasks relating to the stack display.
 */
public class MainContentController extends TabPane {
    /**
     * Constructs the StackPane and loads its FXML file.
     */
    public MainContentController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/ContentRoot.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.getSelectionModel().select(0);
    }

}

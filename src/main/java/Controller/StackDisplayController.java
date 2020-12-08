package Controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import java.io.IOException;

/**
 * The Stack Displays controller. Handles all tasks relating to the stack display.
 */
public class StackDisplayController extends StackPane {
    /**
     * Constructs the StackPane and loads its FXML file.
     */
    public StackDisplayController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/StackDisplay.fxml"));
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

package Controller.Content;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class ProcessController extends AnchorPane {
    /**
     * Constructs the VBox and loads its FXML file.
     */
    public ProcessController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/Process.fxml"));
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

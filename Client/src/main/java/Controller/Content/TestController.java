package Controller.Content;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class TestController extends AnchorPane {
    /**
     * Constructs the VBox and loads its FXML file.
     */
    public TestController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/Test.fxml"));
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

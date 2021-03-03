package Controller.Content;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class AdminController extends ScrollPane {
    @FXML AnchorPane container;

    /**
     * Constructs the VBox and loads its FXML file.
     */
    public AdminController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/Admin.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        container.prefWidthProperty().bind(this.widthProperty());
        container.prefHeightProperty().bind(this.heightProperty());
    }
}

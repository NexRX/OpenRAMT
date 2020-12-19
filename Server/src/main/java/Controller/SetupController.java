package Controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SetupController extends AnchorPane {
    private Stage stage;

    public SetupController(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Setup.fxml"));
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

    private void applyEventHandlers() {
        /*btnClose.setOnMouseClicked(event -> {
            Platform.exit();
            System.exit(0);
        });*/
    }
}
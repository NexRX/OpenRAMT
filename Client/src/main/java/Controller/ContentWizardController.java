package Controller;

import Controller.Library.SideButton;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.ArrayList;

public class ContentWizardController extends WizardPane {

    /**
     * Constructs the VBox and loads its FXML file.
     */
    public ContentWizardController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content.fxml"));
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

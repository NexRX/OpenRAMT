package Controller;

import Controller.Library.SideButton;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The side bar controller. Handles all tasks relating to the sidebar.
 */
public class SideBarController extends VBox {
    @FXML
    JFXButton sbtnMain;
    private ArrayList<SideButton> sideButtons = new ArrayList<>();

    /**
     * Constructs the VBox and loads its FXML file.
     */
    public SideBarController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/SideBar.fxml"));
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



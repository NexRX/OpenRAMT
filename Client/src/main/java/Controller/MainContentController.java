package Controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

/**
 * The Stack Displays controller. Handles all tasks relating to the stack display.
 */
public class MainContentController extends WizardPane {
    /**
     * Constructs the StackPane and loads its FXML file.
     */
    public MainContentController(Node... children) {
        super(children);
        this.setMaxHeight(-1);
        this.setMaxWidth(-1);
        this.setMinHeight(-1);
        this.setMinWidth(-1);
        this.setPrefHeight(-1);
        this.setPrefWidth(-1);
        AnchorPane.setTopAnchor(this, 26.0);
        AnchorPane.setRightAnchor(this, 0.0);
        AnchorPane.setBottomAnchor(this, 0.0);
        AnchorPane.setLeftAnchor(this, 100.0);
        this.setStyle("-fx-background-color: purple");
    }

}

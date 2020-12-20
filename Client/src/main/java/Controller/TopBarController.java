package Controller;

import application.Launcher;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ButtonBar;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The Top bar controller. Handles all tasks relating to the Top bar.
 */
public class TopBarController extends ButtonBar {
    private final Stage stage;

    private double xOffset = 0;
    private double yOffset = 0;

    private Boolean isMaximised = false;
    private double prevWidth;
    private double prevHeight;
    private double prevX;
    private double prevY;

    /**
     * Constructs the ButtonBar and loads its FXML file. Does a lot of the undecorated
     * window processing.
     */
    public TopBarController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/TopBar.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.stage = Launcher.MainStart.getStage();

        applyEventHandlers();
    }

    /**
     * Applies this panes event handlers included those related to the stage.
     */
    public void applyEventHandlers() {
        this.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        this.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
            isMaximised = false;
        });

        this.getButtons().get(2).setOnMouseClicked(event -> {
            Platform.exit();
            System.exit(0);
        });

        this.getButtons().get(1).setOnMouseClicked(event -> {
            ObservableList<Screen> screens = Screen.getScreensForRectangle(new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));


            if (!isMaximised) {
                Rectangle2D bounds = screens.get(0).getVisualBounds();
                prevX = stage.getX();
                prevY = stage.getY();

                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());

                prevWidth = stage.getWidth();
                prevHeight = stage.getHeight();

                stage.setWidth(bounds.getWidth());
                stage.setHeight(bounds.getHeight());

                isMaximised = true;
            } else {
                stage.setWidth(prevWidth);
                stage.setHeight(prevHeight);

                stage.setX(prevX);
                stage.setY(prevY);

                isMaximised = false;
            }
        });

        this.getButtons().get(0).setOnMouseClicked(event -> stage.setIconified(true));
    }
}



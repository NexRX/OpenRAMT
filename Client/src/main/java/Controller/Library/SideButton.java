package Controller.Library;

import Model.General.AppPermission;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

/**
 * A side button objects for switching tab content.
 */
public class SideButton extends JFXButton {
    final String name;
    private final AppPermission permission;

    /**
     * An instance of a sidebutton.
     * @param name The name to display for this button.
     * @param permission The permission associated with the content related.
     */
    public SideButton(String name, AppPermission permission) {
        super();

        this.name = name;
        this.permission = permission;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/SideButton.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.setText(this.name);
    }

    /**
     * The name in use for this button.
     * @return the side buttons name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the enum app permission associated with the content related to this button.
     * @return The related app permission.
     */
    public AppPermission getAppPermission() {
        return permission;
    }
}

package Controller.Library;

import Model.AppPermission;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class SideButton extends JFXButton {
    final String name;
    private final AppPermission permission;

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

    public String getName() {
        return this.name;
    }

    public AppPermission getAppPermission() {
        return permission;
    }
}

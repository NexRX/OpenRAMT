package Controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

public class RAMTAlert extends Alert {

    public RAMTAlert(AlertType alertType) {
        super(alertType);
        this.initTheme();
    }

    public RAMTAlert(AlertType alertType,
                     String contentText,
                     ButtonType... buttons) {
        super(alertType, contentText, buttons);
        this.initTheme();
    }

    public RAMTAlert(AlertType alertType,
                     String title,
                     String header,
                     String contentText) {
        super(alertType);
        this.initTheme();

        this.setTitle(title);
        this.setHeaderText(header);
        this.setContentText(contentText);
    }

    public RAMTAlert(AlertType alertType,
                     String title,
                     String header,
                     String contentText,
                     ButtonType... buttons) {
        super(alertType, contentText, buttons);
        this.initTheme();

        this.setTitle(title);
        this.setHeaderText(header);
        this.setContentText(contentText);

    }

    public void initTheme() {
        DialogPane dialogPane = this.getDialogPane();
        dialogPane.getStylesheets().add( getClass().getResource("/CSS/Launcher.css").toExternalForm());
        dialogPane.getStyleClass().add("RAMTAlert");
    }

}
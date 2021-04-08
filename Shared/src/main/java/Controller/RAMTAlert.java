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

    // Preset of alerts.
    public static RAMTAlert getAlertMidTask() {
        return new RAMTAlert(Alert.AlertType.CONFIRMATION,
                "OpenRAMT Confirmation",
                "A task was already running, cancel that one and continue?",
                "If you select yes, the previous task will be canceled when possible and this one" +
                        "will take over.");
    }

}
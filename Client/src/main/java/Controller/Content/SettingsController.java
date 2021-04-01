package Controller.Content;


import Controller.RAMTAlert;
import Controller.RootController;
import Model.Task.Task;
import Model.Task.TaskRequest;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.HashMap;

import static Controller.RootController.*;

@SuppressWarnings("unchecked") // Safe when server & client respects request/response structure.
public class SettingsController extends ScrollPane {
    @FXML VBox container;

    String lastRequestID = "";
    HashMap<String, String> settings;

    // Form Controls
    @FXML JFXToggleButton toggleBtnSSL;
    @FXML JFXTextField txtNetworkPort;

    @FXML JFXTextField txtFTPAdminUsername;
    @FXML JFXTextField txtFTPAdminPassword;
    @FXML JFXTextField txtFTPGuestUsername;
    @FXML JFXTextField txtFTPGuestPassword;
    @FXML JFXToggleButton toggleBtnFTPGuest;

    @FXML JFXTextField txtMonitoringPollingRate;

    // Submit
    @FXML JFXButton btnSubmit;
    @FXML JFXButton btnRefresh;

    /**
     * Constructs the VBox and loads its FXML file.
     */
    public SettingsController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/Settings.fxml"));
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

        applyEventHandlers();

        lastRequestID = requestStart(new TaskRequest<>(Task.GETSETTINGS, getLoggedInUser()));
    }

    private void applyEventHandlers() {

        btnSubmit.setOnMouseClicked(event ->
            lastRequestID = requestStart(new TaskRequest<>(Task.EDITSETTINGS, getLoggedInUser(), getNewSettings())));

        btnRefresh.setOnMouseClicked(event ->
            lastRequestID = requestStart(new TaskRequest<>(Task.GETSETTINGS, getLoggedInUser(), getNewSettings())));

        getTaskService().addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            if (lastRequestID.equals(getTaskService().getRequest().getRequestID())) { // Our Request?

                if (getTaskService().getValue().getRequest().getTask() == Task.GETSETTINGS) {
                    settings = (HashMap<String, String>) getTaskService().getLastResponse().getResponseData();
                    setPlaceholdersPreValues();
                } else {
                    switch (getTaskService().getValue().getResponseCode()) {
                        case 0 -> new RAMTAlert(Alert.AlertType.INFORMATION,
                                "OpenRAMT Information",
                                "Settings updated successfully!",
                                "Refresh this settings page to reflect the new value when ready.\n\n" +
                                        "*Some* settings (like SSL/TLS) will need a server-side app restart AND" +
                                        "client-side restart there after to re-connect if needed.").show();
                        case 1 -> new RAMTAlert(Alert.AlertType.WARNING,
                                "OpenRAMT Warning",
                                "Settings couldn't be updated!",
                                "The server rejected those settings for a generic/unknown reason.").show();
                        case 2 -> new RAMTAlert(Alert.AlertType.WARNING,
                                "OpenRAMT Warning",
                                "Server said those settings aren't valid.",
                                "Please make sure that the value given are valid and/or expected for the setting").show();
                    }
                }

            }
        });
    }

    private void setPlaceholdersPreValues() {
        // Clear already text values if any
        txtNetworkPort.clear();
        txtFTPAdminUsername.clear();
        txtFTPAdminPassword.clear();
        txtFTPGuestUsername.clear();
        txtFTPGuestPassword.clear();
        txtMonitoringPollingRate.clear();

        // Set Pre values and placeholders
        toggleBtnSSL.selectedProperty().set(Boolean.parseBoolean(settings.get("Security")));
        txtNetworkPort.setPromptText(settings.get("Port"));

        txtFTPAdminUsername.setPromptText(settings.get("FTP Username"));
        txtFTPAdminPassword.setPromptText("*".repeat(settings.get("FTP Password").length()));
        txtFTPGuestUsername.setPromptText(settings.get("FTP Guest Username"));
        txtFTPGuestPassword.setPromptText("*".repeat(settings.get("FTP Guest Password").length()));
        toggleBtnFTPGuest.selectedProperty().set(Boolean.parseBoolean(settings.get("FTP Guest Enabled")));

        txtMonitoringPollingRate.setPromptText(settings.get("Monitoring Polling Rate"));

    }

    private HashMap<String, String> getNewSettings() {
        HashMap<String, String> results = new HashMap<>();

        // Security
        results.put("Security", String.valueOf(toggleBtnSSL.isSelected()));
        if (!txtNetworkPort.getText().isEmpty()) { results.put("Port", txtNetworkPort.getText()); }

        // Embedded FTP
        if (!txtFTPAdminUsername.getText().isEmpty()) { results.put("FTP Username", txtFTPAdminUsername.getText()); }
        if (!txtFTPAdminPassword.getText().isEmpty()) { results.put("FTP Password", txtFTPAdminPassword.getText()); }
        if (!txtFTPGuestUsername.getText().isEmpty()) { results.put("FTP Guest Username", txtFTPGuestUsername.getText()); }
        if (!txtFTPGuestPassword.getText().isEmpty()) { results.put("FTP Guest Password", txtFTPGuestPassword.getText()); }
        results.put("FTP Guest Enabled", String.valueOf(toggleBtnFTPGuest.isSelected()));

        // Monitoring
        if (!txtMonitoringPollingRate.getText().isEmpty()) { results.put("Monitoring Polling Rate", txtMonitoringPollingRate.getText()); }

        return results;
    }
}

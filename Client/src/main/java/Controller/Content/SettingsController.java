package Controller.Content;


import Controller.RAMTAlert;
import Model.Task.Response;
import Model.Task.Task;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.HashMap;

import static Controller.RootController.*;

/**
 * Controller class for the remote management of server settings.
 */
@SuppressWarnings("unchecked") // Safe when server & client respects request/response structure.
public class SettingsController extends ScrollPane {
    @FXML VBox container;

    String lastRequestID = "";
    HashMap<String, String> settings;

    // Form Controls
    @FXML JFXToggleButton toggleBtnSSL;
    @FXML JFXTextField txtNetworkPort;

    @FXML JFXTextField txtFTPPort;
    @FXML JFXTextField txtFTPAdminUsername;
    @FXML JFXTextField txtFTPAdminPassword;
    @FXML JFXTextField txtFTPGuestUsername;
    @FXML JFXTextField txtFTPGuestPassword;
    @FXML JFXToggleButton toggleBtnFTPGuest;

    @FXML JFXTextField txtMonitoringPort;
    @FXML JFXTextField txtMonitoringPollingRate;

    // Submit
    @FXML JFXButton btnFactoryReset;
    @FXML JFXButton btnSubmit;
    @FXML JFXButton btnRefresh;

    /**
     * Controller for displaying server settings and the tasks related
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

        lastRequestID = requestAutomatedStart(new TaskRequest<>(Task.GETSETTINGS, getLoggedInUser()));
    }

    private void applyEventHandlers() {

        btnSubmit.setOnMouseClicked(event ->
                lastRequestID = requestStart(new TaskRequest<>(Task.EDITSETTINGS, getLoggedInUser(), getNewSettings())));

        btnRefresh.setOnMouseClicked(event ->
                lastRequestID = requestStart(new TaskRequest<>(Task.GETSETTINGS, getLoggedInUser(), getNewSettings())));

        btnFactoryReset.setOnMouseClicked(event -> {
            Alert alert = new RAMTAlert(Alert.AlertType.INFORMATION,
                    "OpenRAMT Information",
                    "Factory Resets are Permanent, Confirm!",
                    "The server will lose all of its custom data including settings, accounts, certs and " +
                            "anything in the data folder of this application. \n\n" +
                            "Afterwards this server will also close.");
            alert.showAndWait();

            if (alert.getResult() == ButtonType.OK) {
                lastRequestID = requestStart(new TaskRequest<>(Task.FACTORYRESET, getLoggedInUser(), getNewSettings()));
            }
        });

        getTaskService().addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            if (lastRequestID.equals(getTaskService().getRequest().getRequestID())) { // Our Request?
                TaskResponse<?> getTaskService = getTaskService().getValue();

                if (getTaskService.getRequest().getTask() == Task.FACTORYRESET) {
                    if (getTaskService.getResponse() == Response.SUCCESS) {
                        Alert alert = new RAMTAlert(Alert.AlertType.INFORMATION,
                                "OpenRAMT Information",
                                "Factory Restart successfully!",
                                "The server will now proceed to shutdown, please continue on the host system\n" +
                                        "This application will now close...");
                        alert.showAndWait();

                        System.exit(0);
                    } else {
                        Alert alert = new RAMTAlert(Alert.AlertType.INFORMATION,
                                "OpenRAMT Information",
                                "Factory Restart failed!",
                                "The server failed to restart the server for some unknown reason.\n\n" +
                                        "If this keeps occurring, please make sure the server is running as admin or" +
                                        " sudo. If all else fails, this reset can be done manually by deleting the " +
                                        "data folder in the applications install folder.");
                        alert.show();
                    }
                } else if (getTaskService.getRequest().getTask() == Task.GETSETTINGS && getTaskService.getResponse() == Response.SUCCESS) {
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
        txtFTPPort.clear();
        txtFTPAdminUsername.clear();
        txtFTPAdminPassword.clear();
        txtFTPGuestUsername.clear();
        txtFTPGuestPassword.clear();
        txtMonitoringPort.clear();
        txtMonitoringPollingRate.clear();

        // Set Pre values and placeholders
        toggleBtnSSL.selectedProperty().set(Boolean.parseBoolean(settings.get("Security")));
        txtNetworkPort.setPromptText(settings.get("Port"));

        txtFTPPort.setPromptText(settings.get("FTP Port"));
        txtFTPAdminUsername.setPromptText(settings.get("FTP Username"));
        txtFTPAdminPassword.setPromptText("*".repeat(settings.get("FTP Password").length()));
        txtFTPGuestUsername.setPromptText(settings.get("FTP Guest Username"));
        txtFTPGuestPassword.setPromptText("*".repeat(settings.get("FTP Guest Password").length()));
        toggleBtnFTPGuest.selectedProperty().set(Boolean.parseBoolean(settings.get("FTP Guest Enabled")));

        txtMonitoringPort.setPromptText(settings.get("Monitoring Port"));
        txtMonitoringPollingRate.setPromptText(settings.get("Monitoring Polling Rate"));
    }

    /**
     * Gets all the settings newly added byt the user in a hashmap composed of the key for settings and its new
     * corresponding value.
     * @return The hashmap of the new settings. The key is the setting name in the database for information.
     */
    private HashMap<String, String> getNewSettings() {
        HashMap<String, String> results = new HashMap<>();

        // Security
        results.put("Security", String.valueOf(toggleBtnSSL.isSelected()));
        if (!txtNetworkPort.getText().isEmpty()) { results.put("Port", txtNetworkPort.getText()); }

        // Embedded FTP
        if (!txtFTPPort.getText().isEmpty()) { results.put("FTP Port", txtFTPPort.getText()); }
        if (!txtFTPAdminUsername.getText().isEmpty()) { results.put("FTP Username", txtFTPAdminUsername.getText()); }
        if (!txtFTPAdminPassword.getText().isEmpty()) { results.put("FTP Password", txtFTPAdminPassword.getText()); }
        if (!txtFTPGuestUsername.getText().isEmpty()) { results.put("FTP Guest Username", txtFTPGuestUsername.getText()); }
        if (!txtFTPGuestPassword.getText().isEmpty()) { results.put("FTP Guest Password", txtFTPGuestPassword.getText()); }
        results.put("FTP Guest Enabled", String.valueOf(toggleBtnFTPGuest.isSelected()));

        // Monitoring
        if (!txtMonitoringPort.getText().isEmpty()) { results.put("Monitoring Port", txtMonitoringPort.getText()); }
        if (!txtMonitoringPollingRate.getText().isEmpty()) { results.put("Monitoring Polling Rate", txtMonitoringPollingRate.getText()); }

        return results;
    }
}

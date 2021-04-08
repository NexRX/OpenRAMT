package Controller.Content;

import Controller.RAMTAlert;
import Controller.RootController;
import Model.Misc.ProcessItem;
import Model.Task.Task;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import com.jfoenix.controls.JFXButton;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

/**
 * Controller class for process related tasks and display.
 */
public class ProcessController extends AnchorPane {
    private String lastRequestID = "";

    @FXML JFXButton btnKill;
    @FXML JFXButton btnRestart;
    @FXML JFXButton btnRefresh;

    // Table
    @FXML TableView<ProcessItem> tblProcesses;
    @FXML TableColumn<ProcessItem, String> colName;
    @FXML TableColumn<ProcessItem, String> colID;
    @FXML TableColumn<ProcessItem, String> colStatus;
    @FXML TableColumn<ProcessItem, String> colCPU;
    @FXML TableColumn<ProcessItem, String> colMEM;

    /**
     * Controller for process related tasks and display.
     */
    public ProcessController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/Process.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        colName.setCellValueFactory(new PropertyValueFactory<>("Name"));
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("Status"));
        colCPU.setCellValueFactory(new PropertyValueFactory<>("cpu"));
        colMEM.setCellValueFactory(new PropertyValueFactory<>("mem"));

        applyEventHandlers();
    }

    private void applyEventHandlers() {
        btnKill.setOnMouseClicked(event -> {
            try {
                lastRequestID = RootController.requestStart(new TaskRequest<>(Task.KILLPROCESS, RootController.getLoggedInUser(), selectedPID()));
            } catch (IllegalStateException e) {
                new RAMTAlert(Alert.AlertType.WARNING,
                        "OpenRAMT Warning",
                        "Select a process first!",
                        "No process was selected, select a process from the table and then try again.").showAndWait();
            }
        });
        btnRestart.setOnMouseClicked(event -> {
            try{
                lastRequestID = RootController.requestStart(new TaskRequest<>(Task.RESTARTPROCESS, RootController.getLoggedInUser(), selectedPID()));
            } catch (IllegalStateException e) {
                new RAMTAlert(Alert.AlertType.WARNING,
                        "OpenRAMT Warning",
                        "Select a process first!",
                        "No process was selected, select a process from the table and then try again.").showAndWait();
            }
        });

        btnRefresh.setOnMouseClicked(event -> lastRequestID = RootController.requestStart(new TaskRequest<Void>(Task.FETCHPROCESSES, RootController.getLoggedInUser())));

        RootController.getTaskService().addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            if (lastRequestID.equals(RootController.getTaskService().getRequest().getRequestID())){ // Our Request?
                TaskResponse<?> response = ((TaskResponse<?>) event.getSource().getValue());

                switch (response.getRequest().getTask()) {
                    case FETCHPROCESSES:
                        @SuppressWarnings("unchecked") // Safe when server & client respect request/response structure.
                        TaskResponse<String> fetchResponse = (TaskResponse<String>) event.getSource().getValue();

                        try {
                            JSONArray json = new JSONArray(fetchResponse.getResponseData());

                            tblProcesses.getItems().clear();

                            for (int i = 0; i < json.length(); i++) {
                                tblProcesses.getItems().add(new ProcessItem(json.getJSONObject(i).get("Name").toString(),
                                        json.getJSONObject(i).get("IDProcess").toString(),
                                        json.getJSONObject(i).get("Status").toString(),
                                        json.getJSONObject(i).get("PercentProcessorTime").toString(),
                                        json.getJSONObject(i).get("WorkingSetPrivate").toString()));
                            }
                        } catch (JSONException e) {
                            System.out.println("Server JSON parsing failed, exception message: "+ e.getMessage());
                        }

                    case KILLPROCESS:
                    case RESTARTPROCESS:
                        @SuppressWarnings("unchecked") // Safe when server & client respect request/response structure.
                        TaskResponse<Void> killResponse = (TaskResponse<Void>) event.getSource().getValue();
                        System.out.println(killResponse.getResponseCode());
                        break;
                }
            }
        });
    }

    /**
     * Gets the selected process ID in the table by the user if any.
     * @return The selected process ID.
     * @throws IllegalStateException if nothing is selected.
     */
    private Integer selectedPID() throws IllegalStateException {
        if (tblProcesses.getSelectionModel().getSelectedItem() != null) {
            return Integer.valueOf(tblProcesses.getSelectionModel().getSelectedItem().getId());
        }
        throw new IllegalStateException("Nothing selected in the Model.");
    }
}

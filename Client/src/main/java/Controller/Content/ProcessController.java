package Controller.Content;

import Controller.RAMTAlert;
import Controller.RootController;
import Model.Misc.ProcessItem;
import Model.Task.Task;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

public class ProcessController extends AnchorPane {
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
     * Constructs the VBox and loads its FXML file.
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
            if (RootController.getTaskService().isRunning()) {
                Alert alert = RAMTAlert.getAlertMidTask();
                alert.showAndWait();

                if (alert.getResult() == ButtonType.OK) {
                    RootController.getTaskService().updateAndRestart(
                            new TaskRequest<>(Task.KILLPROCESS, RootController.getLoggedInUser(), selectedPID()));

                }
            } else {
                RootController.getTaskService().updateAndRestart(
                        new TaskRequest<>(Task.KILLPROCESS, RootController.getLoggedInUser(), selectedPID()));
            }
        });

        btnRestart.setOnMouseClicked(event -> {
            if (RootController.getTaskService().isRunning()) {
                Alert alert = RAMTAlert.getAlertMidTask();
                alert.showAndWait();

                if (alert.getResult() == ButtonType.OK) {
                    RootController.getTaskService().updateAndRestart(
                            new TaskRequest<>(Task.RESTARTPROCESS, RootController.getLoggedInUser(), selectedPID()));

                }
            } else {
                RootController.getTaskService().updateAndRestart(
                        new TaskRequest<>(Task.RESTARTPROCESS, RootController.getLoggedInUser(), selectedPID()));
            }
        });

        btnRefresh.setOnMouseClicked(event -> {
            if (RootController.getTaskService().isRunning()) {
                Alert alert = RAMTAlert.getAlertMidTask();
                alert.showAndWait();

                if (alert.getResult() == ButtonType.OK) {
                    RootController.getTaskService().updateAndRestart(
                            new TaskRequest<Void>(Task.FETCHPROCESSES, RootController.getLoggedInUser()));

                }
            } else {
                RootController.getTaskService().updateAndRestart(
                        new TaskRequest<Void>(Task.FETCHPROCESSES, RootController.getLoggedInUser()));
            }
        });

        RootController.getTaskService().setOnSucceeded(event -> {
            switch (((TaskResponse<?>) event.getSource().getValue()).getRequest().getTask()) {
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
                        new RAMTAlert(Alert.AlertType.ERROR,
                                "OpenRAMT Error",
                                "Server sent a unread list of process for this client.\n\n",
                                "The server sent a JSON response that couldn't be read." +
                                        "Please report this bug to a developer or try to ensure client and server" +
                                        "versions match support.\n JSON: ["+fetchResponse.getResponseData()+"]").show();
                    }

                case KILLPROCESS:
                case RESTARTPROCESS:
                    @SuppressWarnings("unchecked") // Safe when server & client respect request/response structure.
                    TaskResponse<Void> killResponse = (TaskResponse<Void>) event.getSource().getValue();
                    System.out.println(killResponse.getResponseCode());
                    break;
            }
            //TODO alert user somethings gone wrong if we got here.
        });
    }

    private Integer selectedPID() {
        //TODO detect not selected.
        return Integer.valueOf(tblProcesses.getSelectionModel().getSelectedItem().getId());
    }
}

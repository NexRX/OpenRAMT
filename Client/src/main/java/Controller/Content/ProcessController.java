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

import java.io.IOException;

public class ProcessController extends AnchorPane {
    @FXML
    JFXButton btnRefresh;

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
        btnRefresh.setOnMouseClicked(event -> {
            if (RootController.getTaskService().isRunning()) {
                Alert alert = new RAMTAlert(Alert.AlertType.CONFIRMATION,
                        "OpenRAMT Confirmation",
                        "A task was already running, cancel that one and continue?",
                        "If you select yes, the previous task will be canceled when possible and this one" +
                                "will take over.");
                alert.showAndWait();

                if (alert.getResult() == ButtonType.OK) {
                    RootController.getTaskService().updateAndRestart(
                            new TaskRequest(Task.FETCHPROCESSES, RootController.getLoggedInUser()));

                }
            } else {
                System.out.println("Here 1");
                RootController.getTaskService().updateAndRestart(
                        new TaskRequest(Task.FETCHPROCESSES, RootController.getLoggedInUser()));
                System.out.println("Here 2");
            }
        });

        RootController.getTaskService().setOnSucceeded(event -> {
            @SuppressWarnings("unchecked") // Safe when server & client respect request/response structure.
                    System.out.println("Here f0");
            TaskResponse<String> response = (TaskResponse<String>) event.getSource().getValue();
            System.out.println("Here f");
            //JSONArray json = new JSONArray(response.getResponseData().substring(1, response.getResponseData().length()-1));
            JSONArray json = new JSONArray(response.getResponseData());

            if (json.length() == 0) { // Check before clearing tbl.
                //TODO alert
            } else {
                tblProcesses.getItems().clear();

                for (int i = 0; i < json.length(); i++) {
                    tblProcesses.getItems().add(new ProcessItem(json.getJSONObject(i).get("Name").toString(),
                            json.getJSONObject(i).get("IDProcess").toString(),
                            json.getJSONObject(i).get("Status").toString(),
                            json.getJSONObject(i).get("PercentProcessorTime").toString(),
                            json.getJSONObject(i).get("WorkingSetPrivate").toString()));
                }
            }
        });
    }
}

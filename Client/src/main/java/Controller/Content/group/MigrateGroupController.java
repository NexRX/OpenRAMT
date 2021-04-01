package Controller.Content.group;

import Controller.RAMTAlert;
import Controller.RootController;
import Model.Task.Task;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import Model.User.UserGroup;
import com.jfoenix.controls.JFXButton;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unchecked") // Safe when server & client respects request/response structure.
public class MigrateGroupController extends AnchorPane {
    Stage stage;
    Stage callingStage;
    private double xOffset = 0;
    private double yOffset = 0;

    private String lastRequestID  = "";

    HashMap<String, UserGroup> groups = new HashMap<>();

    // Window Controls
    @FXML Pane topBar;
    @FXML JFXButton btnClose;
    @FXML Label lblGroupValue;

    @FXML ComboBox<String> comboGroup;

    //Submit
    @FXML JFXButton btnSubmit;

    public MigrateGroupController(Stage stage, Stage callingStage, String groupName) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/Group/MigrateUsers.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.stage = stage;
        this.callingStage = callingStage;
        lblGroupValue.setText(groupName);

        applyEventHandlers();

        lastRequestID = RootController.requestStart(new TaskRequest<>(Task.GETGROUPS, RootController.getLoggedInUser()));
    }

    private void applyEventHandlers() {
        topBar.setOnMousePressed(event -> { // These next two control window movement
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        topBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        btnClose.setOnMouseClicked(event -> returnToCaller());
        stage.setOnCloseRequest(event -> returnToCaller());

        btnSubmit.setOnMouseClicked(event -> lastRequestID = RootController.requestStart(new TaskRequest<>(
                Task.UPDATEGROUP,
                RootController.getLoggedInUser(),
                new String[]{"migrate", lblGroupValue.getText(), groups.get(comboGroup.getValue()).getName()})));

        RootController.getTaskService().addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            if (lastRequestID.equals(RootController.getTaskService().getRequest().getRequestID())) { // Our Request?
                if (RootController.getTaskService().getValue().getRequest().getTask() == Task.UPDATEGROUP) {
                    Alert alert;
                    Alert.AlertType type;
                    String alertMessage;
                    boolean exit = false;

                    switch (((TaskResponse<Void>) event.getSource().getValue()).getResponseCode()) {
                        case 0 -> {
                            type = Alert.AlertType.INFORMATION;
                            alertMessage = "Operation completed successfully.";
                            exit = true;
                        }
                        case 1 -> {
                            type = Alert.AlertType.INFORMATION;
                            alertMessage = "One or more group names couldn't be identified";
                        }
                        case 2 -> {
                            type = Alert.AlertType.WARNING;
                            alertMessage = "One or more group names are invalid.";
                        }
                        case 3 -> {
                            type = Alert.AlertType.WARNING;
                            alertMessage = "Trying to migrate a default user/group is not allowed and recommended.";
                        }
                        default -> {
                            type = Alert.AlertType.ERROR;
                            alertMessage = "Couldn't handle the request. Please double check everything and try again.";
                        }
                    }
                    alert = new RAMTAlert(type,"Form Message", alertMessage,"Click a button to continue.");
                    alert.showAndWait();

                    if (exit) { returnToCaller(); }
                } else {
                    ArrayList<UserGroup> groupList =
                            (ArrayList<UserGroup>) RootController.getTaskService().getLastResponse().getResponseData();

                    for (UserGroup group : groupList) {
                        String groupKey = group.getName() + " | " +
                                group.getAdmin() + ", " +
                                group.getGeneral() + ", " +
                                group.getProcess() + ", " +
                                group.getMonitoring() + ", " +
                                group.getPower() + ".";

                        groups.put(groupKey, group);
                        comboGroup.getItems().add(groupKey);
                    }
                }
            }
        });
    }

    private void returnToCaller() {
        callingStage.show();
        stage.close();
    }
}

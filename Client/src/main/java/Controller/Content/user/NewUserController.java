package Controller.Content.user;

import Controller.RAMTAlert;
import Controller.RootController;
import Model.Task.Task;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import Model.User.UserData;
import Model.User.UserGroup;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unchecked") // Safe when server & client respects request/response structure.
public class NewUserController extends AnchorPane {
    Stage stage;
    Stage callingStage;
    private double xOffset = 0;
    private double yOffset = 0;

    private String lastRequestID  = "";

    HashMap<String, UserGroup> groups = new HashMap<>();

    // Display/General Related
    @FXML JFXButton btnClose;
    @FXML Pane topBar;

    // Form
    @FXML JFXTextField txtUsername;
    @FXML JFXPasswordField txtPassword;
    @FXML ComboBox<String> comboGroup;

    @FXML JFXButton btnSubmit;


    public NewUserController(Stage stage, Stage callingStage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/User/NewUser.fxml"));
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

        btnSubmit.setOnMouseClicked(event -> lastRequestID = RootController.requestStart(new TaskRequest<>(Task.ADDUSER, RootController.getLoggedInUser(), getUserData())));

        RootController.getTaskService().addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            if (lastRequestID.equals(RootController.getTaskService().getRequest().getRequestID())) { // Our Request?

                if (RootController.getTaskService().getValue().getRequest().getTask() == Task.ADDUSER) {
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
                            alertMessage = "The user already exists or is reserved.";
                        }
                        case 2 -> {
                            type = Alert.AlertType.WARNING;
                            alertMessage = "One or more fields are invalid.";
                        }
                        case 3 -> {
                            type = Alert.AlertType.WARNING;
                            alertMessage = "Username is reserved by the system.";
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

    public UserData getUserData() {
        return new UserData(null,
                txtUsername.getText(),
                txtPassword.getText(),
                groups.get(comboGroup.getValue()).getName());
    }

    @FXML
    public void exitApplication(ActionEvent event) {
        returnToCaller();
    }

    private void returnToCaller() {
        callingStage.show();
        stage.close();
    }
}

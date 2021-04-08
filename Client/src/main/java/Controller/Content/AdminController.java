package Controller.Content;

import Controller.RAMTAlert;
import Controller.RootController;
import Model.Task.Task;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import Model.User.UserData;
import Model.User.UserGroup;
import Model.User.UserItem;
import application.Launcher;
import com.jfoenix.controls.JFXButton;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The controller class for handling admin related events. Most for user management.
 */
public class AdminController extends ScrollPane {
    private final Stage stage;

    private String lastRequestID  = "";

    private ArrayList<UserData> users;
    private final HashMap<String, UserGroup> groups = new HashMap<>();

    @FXML AnchorPane container;

    // User
    @FXML Label lblUserValue;
    @FXML JFXButton btnUserNew;
    @FXML JFXButton btnUserName;
    @FXML JFXButton btnUserPassword;
    @FXML JFXButton btnUserGroup;
    @FXML JFXButton btnUserSuspend;
    @FXML JFXButton btnUserUnsuspend;
    @FXML JFXButton btnUserDelete;

    // Group
    @FXML Label lblGroupValue;
    @FXML JFXButton btnGroupNew;
    @FXML JFXButton btnGroupName;
    @FXML JFXButton btnGroupPermission;
    @FXML JFXButton btnGroupMigrate;
    @FXML JFXButton btnGroupSuspendUsers;
    @FXML JFXButton btnGroupDeleteUsers;
    @FXML JFXButton btnGroupDelete;


    // Table
    @FXML JFXButton btnRefresh;
    @FXML TableView<UserItem> tblUsers;
    @FXML TableColumn<UserItem, String> colUsername;
    @FXML TableColumn<UserItem, String> colGroup;
    @FXML TableColumn<UserItem, String> colAdmin;
    @FXML TableColumn<UserItem, String> colGeneral;
    @FXML TableColumn<UserItem, String> colProcess;
    @FXML TableColumn<UserItem, String> colMonitoring;
    @FXML TableColumn<UserItem, String> colPower;
    @FXML TableColumn<UserItem, String> colSuspended;

    /**
     * Controller of the admin pane. The primary purpose is user management.
     */
    public AdminController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/Admin.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.stage = Launcher.MainStart.getStage();

        container.prefWidthProperty().bind(this.widthProperty());
        container.prefHeightProperty().bind(this.heightProperty());

        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colGroup.setCellValueFactory(new PropertyValueFactory<>("group"));
        colAdmin.setCellValueFactory(new PropertyValueFactory<>("admin"));
        colGeneral.setCellValueFactory(new PropertyValueFactory<>("general"));
        colProcess.setCellValueFactory(new PropertyValueFactory<>("process"));
        colMonitoring.setCellValueFactory(new PropertyValueFactory<>("monitoring"));
        colPower.setCellValueFactory(new PropertyValueFactory<>("power"));
        colSuspended.setCellValueFactory(new PropertyValueFactory<>("suspended"));

        applyEventHandlers();
    }

    private void applyEventHandlers() {
        /* User */
        btnUserNew.setOnMouseClicked(event -> {
            // Show User Creation and wait. Then get results and create user with it.
            try {
                reflectiveFormStart(Class.forName("Controller.Content.user.NewUserController"));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnUserName.setOnMouseClicked(event -> {
            try {
                reflectiveFormStart(Class.forName("Controller.Content.user.NewUsernameController"), selectedUsername());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }


        });

        btnUserPassword.setOnMouseClicked(event -> {
            try {
                reflectiveFormStart(Class.forName("Controller.Content.user.NewUserPasswordController"), selectedUsername());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnUserGroup.setOnMouseClicked(event -> {
            try {
                reflectiveFormStart(Class.forName("Controller.Content.user.NewUserGroupController"), selectedUsername());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnUserUnsuspend.setOnMouseClicked(event -> {
            try {
                String user = this.selectedUsername();
                Alert alert = new RAMTAlert(Alert.AlertType.CONFIRMATION,
                        "User suspension Confirmation.",
                        "Do you wish to suspend user " + user + "?",
                        "Please note that users must be unsuspended manually!");

                alert.showAndWait();

                if (alert.getResult() == ButtonType.OK) {
                    lastRequestID = RootController.requestStart(new TaskRequest<>(Task.UNSUSPENDUSER, RootController.getLoggedInUser(), selectedUsername()));
                }
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnUserSuspend.setOnMouseClicked(event -> {
            try {
                String user = this.selectedUsername();
                Alert alert = new RAMTAlert(Alert.AlertType.CONFIRMATION,
                        "User suspension Confirmation.",
                        "Do you wish to suspend user " + user + "?",
                        "Please note that users must be unsuspended manually!");

                alert.showAndWait();

                if (alert.getResult() == ButtonType.OK) {
                    lastRequestID = RootController.requestStart(new TaskRequest<>(Task.SUSPENDUSER, RootController.getLoggedInUser(), selectedUsername()));
                }
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnUserDelete.setOnMouseClicked(event -> {
            try {
                String user = this.selectedUsername();
                Alert alert = new RAMTAlert(Alert.AlertType.CONFIRMATION,
                        "User Deletion Confirmation.",
                        "Do you wish to delete user " + user + "?",
                        "This cannot be undone without a data backup! \n" +
                                "Please double check and ensure your at least delete the correct user.");

                alert.showAndWait();

                if (alert.getResult() == ButtonType.OK) {
                    lastRequestID = RootController.requestStart(new TaskRequest<>(Task.DELETEUSER, RootController.getLoggedInUser(), selectedUsername()));
                }
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });


        /* Group */
        btnGroupNew.setOnMouseClicked(event -> {
            try {
                reflectiveFormStart(Class.forName("Controller.Content.group.NewGroupController"));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnGroupName.setOnMouseClicked(event -> {
            try {
                reflectiveFormStart(Class.forName("Controller.Content.group.NewGroupNameController"), selectedGroupName());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnGroupPermission.setOnMouseClicked(event -> {
            try {
                reflectiveFormStart(Class.forName("Controller.Content.group.NewGroupPermissionsController"), selectedGroupName());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnGroupMigrate.setOnMouseClicked(event -> {
            try {
                reflectiveFormStart(Class.forName("Controller.Content.group.MigrateGroupController"), selectedGroupName());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnGroupSuspendUsers.setOnMouseClicked(event -> {
            try {
                String group = this.selectedGroupName();
                Alert alert = new RAMTAlert(Alert.AlertType.CONFIRMATION,
                        "Group suspension Confirmation.",
                        "Do you wish to suspend users in " + group + "?",
                        "Please note that users must be unsuspended manually! \n" +
                                "This means that they cannot be 'all' unsuspended via a one click solution.");

                alert.showAndWait();

                if (alert.getResult() == ButtonType.OK) {
                    lastRequestID = RootController.requestStart(new TaskRequest<>(Task.SUSPENDUSERS, RootController.getLoggedInUser(), selectedGroupName()));
                }
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnGroupDeleteUsers.setOnMouseClicked(event -> {
            try {
                String group = this.selectedGroupName();
                Alert alert = new RAMTAlert(Alert.AlertType.CONFIRMATION,
                        "Group User Deletion Confirmation.",
                        "Do you wish to delete all users in " + group + "?",
                        "This cannot be undone without a data backup! \n" +
                                "Please double check and ensure your at least delete users in the right group.");

                alert.showAndWait();

                if (alert.getResult() == ButtonType.OK) {
                    lastRequestID = RootController.requestStart(new TaskRequest<>(Task.DELETEUSERS, RootController.getLoggedInUser(), selectedGroupName()));
                }
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnGroupDelete.setOnMouseClicked(event -> {
            try {
                String group = this.selectedGroupName();
                Alert alert = new RAMTAlert(Alert.AlertType.CONFIRMATION,
                        "Group Deletion Confirmation.",
                        "Do you wish to delete the following " + group + "?",
                        "This cannot be undone without a data backup! \n" +
                                "All users will be moved to the default group. So please make sure everything is correct!");

                alert.showAndWait();

                if (alert.getResult() == ButtonType.OK) {
                    lastRequestID = RootController.requestStart(new TaskRequest<>(Task.DELETEGROUP, RootController.getLoggedInUser(), selectedGroupName()));
                }
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        /* Table */
        btnRefresh.setOnMouseClicked(event -> lastRequestID = RootController.requestStart(new TaskRequest<>(Task.GETUSERS, RootController.getLoggedInUser())));

        tblUsers.setOnMouseClicked(event -> {
            lblUserValue.setText(selectedUsername());
            lblGroupValue.setText(selectedGroupName());
        });

        RootController.getTaskService().addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            if (lastRequestID.equals(RootController.getTaskService().getRequest().getRequestID())) { // Our Request?
                System.out.println("Request ID match received!");

                switch (RootController.getTaskService().getValue().getRequest().getTask()) {
                    case GETUSERS -> tblRefreshPhaseOne();
                    case GETGROUPS -> tblRefreshPhaseTwo();
                    case DELETEUSER -> {
                        switch (RootController.getTaskService().getValue().getResponseCode()) {
                            case 0 -> new RAMTAlert(Alert.AlertType.INFORMATION,
                                    "OpenRAMT Information",
                                    "The user was deleted successfully!",
                                    "You can close this message at any time.").show();
                            case 1 -> new RAMTAlert(Alert.AlertType.WARNING,
                                    "OpenRAMT Warning",
                                    "That user couldn't be deleted!",
                                    "Maybe the user doesn't exist. Try go back and refreshing to check.").show();
                            case 3 -> new RAMTAlert(Alert.AlertType.WARNING,
                                    "OpenRAMT Warning",
                                    "Cannot delete default users!",
                                    "That user is protected/reserved by the system.").show();
                            default -> new RAMTAlert(Alert.AlertType.ERROR,
                                    "OpenRAMT Error",
                                    "Something went wrong during that request.",
                                    "User couldn't be delete because something went wrong in the server").show();
                        }
                    }
                    case SUSPENDUSER -> {
                        switch (RootController.getTaskService().getValue().getResponseCode()) {
                            case 0 -> new RAMTAlert(Alert.AlertType.INFORMATION,
                                    "OpenRAMT Information",
                                    "The user was suspended successfully!",
                                    "You can close this message at any time.").show();
                            case 1 -> new RAMTAlert(Alert.AlertType.WARNING,
                                    "OpenRAMT Warning",
                                    "That user couldn't be suspended!",
                                    "Maybe the user doesn't exist. Try go back and refreshing to check.").show();
                            case 3 -> new RAMTAlert(Alert.AlertType.WARNING,
                                    "OpenRAMT Warning",
                                    "Cannot suspend default users!",
                                    "That user is protected/reserved by the system.").show();
                            default -> new RAMTAlert(Alert.AlertType.ERROR,
                                    "OpenRAMT Error",
                                    "Something went wrong during that request.",
                                    "User couldn't be suspended because something went wrong in the server").show();
                        }
                    }
                    case UNSUSPENDUSER -> {
                        switch (RootController.getTaskService().getValue().getResponseCode()) {
                            case 0 -> new RAMTAlert(Alert.AlertType.INFORMATION,
                                    "OpenRAMT Information",
                                    "The user was unsuspended successfully!",
                                    "You can close this message at any time.").show();
                            case 1 -> new RAMTAlert(Alert.AlertType.WARNING,
                                    "OpenRAMT Warning",
                                    "That user couldn't be unsuspended!",
                                    "Maybe the user doesn't exist. Try go back and refreshing to check.").show();
                            case 3 -> new RAMTAlert(Alert.AlertType.WARNING,
                                    "OpenRAMT Warning",
                                    "Cannot unsuspend default users!",
                                    "That user is protected/reserved by the system.").show();
                            default -> new RAMTAlert(Alert.AlertType.ERROR,
                                    "OpenRAMT Error",
                                    "Something went wrong during that request.",
                                    "User couldn't be unsuspended because something went wrong in the server").show();
                        }
                    }
                    case SUSPENDUSERS -> {
                        switch (RootController.getTaskService().getValue().getResponseCode()) {
                            case 0 -> new RAMTAlert(Alert.AlertType.INFORMATION,
                                    "OpenRAMT Information",
                                    "The group of users was suspended successfully!",
                                    "You can close this message at any time.").show();
                            case 1 -> new RAMTAlert(Alert.AlertType.WARNING,
                                    "OpenRAMT Warning",
                                    "That group of users couldn't be suspended!",
                                    "Maybe the group doesn't exist. Try go back and refreshing to check.").show();
                            case 2 -> new RAMTAlert(Alert.AlertType.WARNING,
                                    "OpenRAMT Warning",
                                    "That group is not valid!",
                                    "Some how the given group name is not valid. Please go back and reselect").show();
                            case 3 -> new RAMTAlert(Alert.AlertType.WARNING,
                                    "OpenRAMT Warning",
                                    "Cannot suspend default groups!",
                                    "That group is protected/reserved by the system.").show();
                            default -> new RAMTAlert(Alert.AlertType.ERROR,
                                    "OpenRAMT Error",
                                    "Something went wrong during that request.",
                                    "Group couldn't be suspended because something went wrong in the server").show();
                        }
                    }
                    case DELETEUSERS -> {
                        switch (RootController.getTaskService().getValue().getResponseCode()) {
                            case 0 -> new RAMTAlert(Alert.AlertType.INFORMATION,
                                    "OpenRAMT Information",
                                    "The group of users was deleted successfully!",
                                    "You can close this message at any time.").show();
                            case 1 -> new RAMTAlert(Alert.AlertType.WARNING,
                                    "OpenRAMT Warning",
                                    "That group of users couldn't be deleted!",
                                    "Maybe the group doesn't exist. Try go back and refreshing to check.").show();
                            case 2 -> new RAMTAlert(Alert.AlertType.WARNING,
                                    "OpenRAMT Warning",
                                    "That group is not valid!",
                                    "Some how the given group name is not valid. Please go back and reselect").show();
                            case 3 -> new RAMTAlert(Alert.AlertType.WARNING,
                                    "OpenRAMT Warning",
                                    "Cannot delete default group of users!",
                                    "That group is protected/reserved by the system.").show();
                            default -> new RAMTAlert(Alert.AlertType.ERROR,
                                    "OpenRAMT Error",
                                    "Something went wrong during that request.",
                                    "Group of users couldn't be deleted because something went wrong in the server").show();
                        }
                    }
                    case DELETEGROUP -> {
                        switch (RootController.getTaskService().getValue().getResponseCode()) {
                            case 0 -> new RAMTAlert(Alert.AlertType.INFORMATION,
                                    "OpenRAMT Information",
                                    "The group was deleted successfully!",
                                    "You can close this message at any time.").show();
                            case 1 -> new RAMTAlert(Alert.AlertType.WARNING,
                                    "OpenRAMT Warning",
                                    "That group couldn't be deleted!",
                                    "Maybe the group doesn't exist. Try go back and refreshing to check.").show();
                            case 2 -> new RAMTAlert(Alert.AlertType.WARNING,
                                    "OpenRAMT Warning",
                                    "That group is not valid!",
                                    "Some how the given group name is not valid. Please go back and reselect").show();
                            case 3 -> new RAMTAlert(Alert.AlertType.WARNING,
                                    "OpenRAMT Warning",
                                    "Cannot deleted default groups!",
                                    "That group is protected/reserved by the system.").show();
                            default -> new RAMTAlert(Alert.AlertType.ERROR,
                                    "OpenRAMT Error",
                                    "Something went wrong during that request.",
                                    "Group couldn't be deleted because something went wrong in the server").show();
                        }
                    }
                    default -> new RAMTAlert(Alert.AlertType.ERROR, // Only occurs with a bug.
                            "OpenRAMT Error",
                            "Something went wrong with the servers response!.",
                            "Your last request was received correctly but the response was invalid.\n" +
                                    "Please report this to a developer/admin and perhaps try again.").show();
                }
            }
        });

    }

    @SuppressWarnings("unchecked") // Safe when server & client respects request/response structure.
    private void tblRefreshPhaseOne() {
        System.out.println("Got Users, Getting Groups");
        users = ((TaskResponse<ArrayList<UserData>>) RootController.getTaskService().getValue()).getResponseData();
        lastRequestID = RootController.requestAutomatedStart(new TaskRequest<>(Task.GETGROUPS, RootController.getLoggedInUser()));
    }

    @SuppressWarnings("unchecked") // Safe when server & client respects request/response structure.
    private void tblRefreshPhaseTwo() {
        System.out.println("Got Groups, Finishing Up");
        for (UserGroup group : ((TaskResponse<ArrayList<UserGroup>>) RootController.getTaskService().getValue()).getResponseData()) {
            groups.put(group.getName(), group);
        }

        tblUsers.getItems().clear();

        for (UserData user: users) {
            tblUsers.getItems().add(new UserItem(user, groups));
        }
    }

    /**
     * Of particular importance for documentation. This method will create a new instance of the class given with
     * parameters needed. It is hardcorded for handling only new instances of user and group controllers for user
     * management. Therefore, it should be updated where new classes are added and rely on the same reflective starting
     * requirements. The new instances is then shown in the a new stage and scene. The instances have our stage used
     * from here to return to when they are done with there work because after the are launcher, our stage hides.
     * @param clazz The class object which should be constructed.
     * @param name The parameters needed.
     * @throws NoSuchMethodException Invalid class = constructor couldn't be found essentially.
     * @throws IllegalAccessException The class given is protected in some way (private).
     * @throws InvocationTargetException Bad constructor(s) for class given.
     * @throws InstantiationException the class is not typical (I.e. abstract, interface) or constructor isn't suitable.
     */
    private void reflectiveFormStart(Class<?> clazz, String... name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Stage creation = new Stage();

        Constructor<?> ctor;
        Object[] params;

        try {
            ctor = clazz.getConstructor(Stage.class, Stage.class);
            params = new Object[] {creation, stage};
        } catch (NoSuchMethodException e){
            ctor = clazz.getConstructor(Stage.class, Stage.class, String.class);
            params = new Object[] {creation, stage, name[0]};
        }

        Parent controller = (Parent) ctor.newInstance(params);

        Scene userCreation = new Scene(controller);

        creation.setScene(userCreation);
        creation.initStyle(StageStyle.UNDECORATED);
        creation.show();

        stage.hide();
    }

    /**
     * Consults the table to see which user item is selected.
     * @return The selected user item from table.
     */
    public UserItem selectedUserItem() {
        return tblUsers.getSelectionModel().getSelectedItem();
    }

    /**
     * Gets the username portion of the selected user item if any.
     * @return the selected username in the table.
     * @throws IllegalStateException if nothing was selected.
     */
    public String selectedUsername() throws IllegalStateException {
        if (selectedUserItem() != null && !selectedUserItem().getUsername().isEmpty()) {
            return selectedUserItem().getUsername();
        } else if(lblUserValue != null && !lblUserValue.getText().isEmpty()) {
            return lblUserValue.getText();
        }

        throw new IllegalStateException("Nothing has been selected yet.");
    }

    /**
     * Gets the group's name portion of the selected user item if any.
     * @return the selected username in the table.
     * @throws IllegalStateException if nothing was selected.
     */
    public String selectedGroupName() throws IllegalStateException {
        if (selectedUserItem() != null && !selectedUserItem().getGroup().isEmpty()) {
            return selectedUserItem().getGroup();
        } else if(lblGroupValue != null && !lblGroupValue.getText().isEmpty()) {
            return lblGroupValue.getText();
        }

        throw new IllegalStateException("Nothing has been selected yet.");
    }

    private void nothingSelectedAlert() {
        new RAMTAlert(Alert.AlertType.INFORMATION,
                "Application Information",
                "No user (and therefore group) has been selected.",
                "Please select a user in the list that you wish to edit. If you wish to edit a group" +
                        "then select a user with that group. If there isn't one then you'll have to add one to the" +
                        "desired group to edit it first.").showAndWait();
    }


}

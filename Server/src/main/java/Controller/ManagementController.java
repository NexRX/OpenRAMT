package Controller;

import Controller.Database.DBManager;
import Controller.Socket.PlainMonitoringServer;
import Controller.Socket.PlainServer;
import Controller.Socket.SecureMonitoringServer;
import Controller.Socket.SecureServer;
import Controller.Socket.Task.RAMTTaskLibrary;
import Model.User.UserData;
import Model.User.UserGroup;
import Model.User.UserItem;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class ManagementController extends AnchorPane {
    private final Stage stage;

    @FXML Pane topBar;
    @FXML JFXButton btnClose;
    @FXML JFXButton btnMin;

    // User
    @FXML Label lblUserValue;
    @FXML JFXButton btnUserNew;
    @FXML JFXButton btnUserName;
    @FXML JFXButton btnUserPassword;
    @FXML JFXButton btnUserGroup;
    @FXML JFXButton btnUserSuspend;
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


    private double xOffset = 0;
    private double yOffset = 0;

    private SecureServer secureServer;
    private PlainServer plainServer;

    private boolean secure;

    public ManagementController(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Management.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.stage = stage;

        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colGroup.setCellValueFactory(new PropertyValueFactory<>("group"));
        colAdmin.setCellValueFactory(new PropertyValueFactory<>("admin"));
        colGeneral.setCellValueFactory(new PropertyValueFactory<>("general"));
        colProcess.setCellValueFactory(new PropertyValueFactory<>("process"));
        colMonitoring.setCellValueFactory(new PropertyValueFactory<>("monitoring"));
        colPower.setCellValueFactory(new PropertyValueFactory<>("power"));
        colSuspended.setCellValueFactory(new PropertyValueFactory<>("suspended"));

        RAMTTaskLibrary.getOS(); // Does introduce some startup time after login for PowerShell Environment.
                                 // But also reduces time for first PowerShell task.
        applyEventHandlers();

        try {
            secure = Boolean.parseBoolean(DBManager.getSetting("Security"));
        } catch (SQLException e) {
            secure = true; // Safest fallback.
        }
        System.out.println("Server secure? " + secure);

        startServer();
        startMonitoring();
    }

    private void startServer() {
        stopServer(); // Remove if causing ungraceful shutdown problems.

        if (secure) {
            try {
                System.out.println("SSL");
                secureServer = new SecureServer(Integer.parseInt(DBManager.getSetting("Port")));
            } catch (SQLException e) {
                e.printStackTrace();
                portWarning();
                secureServer = new SecureServer(); // Default port fall back.
            }
            new Thread(secureServer).start();
        } else {
            try {
                plainServer = new PlainServer(Integer.parseInt(DBManager.getSetting("Port")));
            } catch (SQLException e) {
                e.printStackTrace();
                portWarning();
                plainServer = new PlainServer(); // Default port fall back.
            }
            new Thread(plainServer).start();
        }
    }

    private void stopServer() {
        if (secureServer != null) {
            secureServer.stop();
            secureServer = null;
        } else if (plainServer != null) {
            plainServer.stop();
            plainServer = null;
        }
    }

    private void startMonitoring() {
        if (secure) {
            new Thread(new SecureMonitoringServer(), "Secure Monitoring Server").start();
        } else {
            new Thread(new PlainMonitoringServer(), "Plain Monitoring Server").start();
        }
    }

    private void stopMonitoring() {

    }

    private void applyEventHandlers() {
        /* Window */
        topBar.setOnMousePressed(event -> { // These next two control window movement
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        topBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        btnMin.setOnMouseClicked(event -> stage.setIconified(true));

        btnClose.setOnMouseClicked(event -> {
            stopServer();
            Platform.exit();
            System.exit(0);
        });

        /* User */
        btnUserNew.setOnMouseClicked(event -> {
            // Show User Creation and wait. Then get results and create user with it.
            try {
                reflectiveFormStart(Class.forName("Controller.user.NewUserController"));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnUserName.setOnMouseClicked(event -> {
            try {
                reflectiveFormStart(Class.forName("Controller.user.NewUsernameController"), selectedUsername());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnUserPassword.setOnMouseClicked(event -> {
            try {
                reflectiveFormStart(Class.forName("Controller.user.NewUserPasswordController"), selectedUsername());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnUserGroup.setOnMouseClicked(event -> {
            try {
                reflectiveFormStart(Class.forName("Controller.user.NewUserGroupController"), selectedUsername());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnUserSuspend.setOnMouseClicked(event -> {
            String user = this.selectedUsername();
            Alert alert = new RAMTAlert(Alert.AlertType.CONFIRMATION,
                    "User suspension Confirmation.",
                    "Do you wish to suspend user " + user + "?",
                    "Please note that users must be unsuspended manually!");

            alert.showAndWait();

            if (alert.getResult() == ButtonType.OK) {
                try {
                    DBManager.suspendUser(user);
                } catch (SQLException e) {
                    e.printStackTrace();
                    sqlError();
                }
            }
        });

        btnUserDelete.setOnMouseClicked(event -> {
            String user = this.selectedUsername();
            Alert alert = new RAMTAlert(Alert.AlertType.CONFIRMATION,
                    "User Deletion Confirmation.",
                    "Do you wish to delete user " + user + "?",
                    "This cannot be undone without a data backup! \n" +
                            "Please double check and ensure your at least delete the correct user.");

            alert.showAndWait();

            if (alert.getResult() == ButtonType.OK) {
                try {
                    DBManager.deleteUser(user);
                } catch (SQLException e) {
                    e.printStackTrace();
                    sqlError();
                }
            }
        });


        /* Group */
        btnGroupNew.setOnMouseClicked(event -> {
            try {
                reflectiveFormStart(Class.forName("Controller.group.NewGroupController"));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnGroupName.setOnMouseClicked(event -> {
            try {
                reflectiveFormStart(Class.forName("Controller.group.NewGroupNameController"), selectedGroupName());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnGroupPermission.setOnMouseClicked(event -> {
            try {
                reflectiveFormStart(Class.forName("Controller.group.NewGroupPermissionsController"), selectedGroupName());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnGroupMigrate.setOnMouseClicked(event -> {
            try {
                reflectiveFormStart(Class.forName("Controller.group.MigrateGroupController"), selectedGroupName());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                nothingSelectedAlert();
            }
        });

        btnGroupSuspendUsers.setOnMouseClicked(event -> {
            String group = this.selectedGroupName();
            Alert alert = new RAMTAlert(Alert.AlertType.CONFIRMATION,
                    "Group suspension Confirmation.",
                    "Do you wish to suspend users in " + group + "?",
                    "Please note that users must be unsuspended manually! \n" +
                            "This means that they cannot be 'all' unsuspended via a one click solution.");

            alert.showAndWait();

            if (alert.getResult() == ButtonType.OK) {
                try {
                    DBManager.suspendGroupUsers(group);
                } catch (SQLException e) {
                    e.printStackTrace();
                    sqlError();
                }
            }
        });

        btnGroupDeleteUsers.setOnMouseClicked(event -> {
            String group = this.selectedGroupName();
            Alert alert = new RAMTAlert(Alert.AlertType.CONFIRMATION,
                    "Group User Deletion Confirmation.",
                    "Do you wish to delete all users in " + group + "?",
                    "This cannot be undone without a data backup! \n" +
                            "Please double check and ensure your at least delete users in the right group.");

            alert.showAndWait();

            if (alert.getResult() == ButtonType.OK) {
                try {
                    DBManager.deleteGroupUsers(group);
                } catch (SQLException e) {
                    e.printStackTrace();
                    sqlError();
                }
            }
        });

        btnGroupDelete.setOnMouseClicked(event -> {
            String group = this.selectedGroupName();
            Alert alert = new RAMTAlert(Alert.AlertType.CONFIRMATION,
                    "Group Deletion Confirmation.",
                    "Do you wish to delete the following " + group + "?",
                    "This cannot be undone without a data backup! \n" +
                            "All users will be moved to the default group. So please make sure everything is correct!");

            alert.showAndWait();

            if (alert.getResult() == ButtonType.OK) {
                try {
                    DBManager.deleteGroup(group);
                } catch (SQLException e) {
                    e.printStackTrace();
                    sqlError();
                }
            }
        });

        /* Table */
        btnRefresh.setOnMouseClicked(event -> {
            try {
                ArrayList<UserData> users = DBManager.getAllUsers();
                HashMap<String, UserGroup> groups = new HashMap<>();

                for (UserGroup group : DBManager.getAllGroups()) {
                    groups.put(group.getName(), group);
                }

                tblUsers.getItems().clear();

                for (UserData user: users) {
                    tblUsers.getItems().add(new UserItem(user, groups));
                }

            } catch (SQLException e) {
                e.printStackTrace();
                sqlError();
            }
        });

        tblUsers.setOnMouseClicked(event -> {
            lblUserValue.setText(selectedUsername());
            lblGroupValue.setText(selectedGroupName());
        });
    }

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

    public void sqlError() {
        Alert alertError = new RAMTAlert(Alert.AlertType.ERROR,
                "Application Error.",
                "The application ran into a error with that request.",
                "Please notify an admin. The error was an 'SQLException' if an admin asks.");
        stopServer();
        alertError.showAndWait();
        System.exit(1);
    }

    public UserItem selectedUserItem() {
        return tblUsers.getSelectionModel().getSelectedItem();
    }

    public String selectedUsername() {
        if (selectedUserItem() != null && !selectedUserItem().getUsername().isEmpty()) {
            return selectedUserItem().getUsername();
        } else if(lblUserValue != null && !lblUserValue.getText().isEmpty()) {
            return lblUserValue.getText();
        }

        throw new IllegalStateException("Nothing has been selected yet.");
    }

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

    private void portWarning() {
        new RAMTAlert(Alert.AlertType.WARNING,
                "Application Warning",
                "A custom port couldn't be retrieved",
                "If this is unexpected, you may need to change the port in settings or if that failed" +
                        "then a reset of this application will be required.\n\n" +
                        "A Default port will be used for now 3069.").show();
    }

}
package Controller;

import Controller.Database.DBManager;
import Controller.Socket.SecureServer;
import Model.UserData;
import Model.UserGroup;
import Model.UserItem;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

import java.io.IOException;
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
    @FXML JFXButton btnGroupGroupMigrate;
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


    private double xOffset = 0;
    private double yOffset = 0;

    SecureServer server;

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

        applyEventHandlers();
        serverStart();
    }

    private void serverStart() {
        server = new SecureServer(); // Pass port later and check it.
        new Thread(server).start();
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
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

            Stage creation = new Stage();
            NewUserController nuc = new NewUserController(creation, stage);
            Scene userCreation = new Scene(nuc);

            creation.setScene(userCreation);
            creation.initStyle(StageStyle.UNDECORATED);
            creation.show();

            stage.hide();
        });

        /* Group */
        btnGroupNew.setOnMouseClicked(event -> {
            // Show User Creation and wait. Then get results and create user with it.

            Stage creation = new Stage();
            NewGroupController ngc = new NewGroupController(creation, stage);
            Scene userCreation = new Scene(ngc);

            creation.setScene(userCreation);
            creation.initStyle(StageStyle.UNDECORATED);
            creation.show();

            stage.hide();
        });

        btnGroupName.setOnMouseClicked(event -> {

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
                //ToDo Tell user about the error.
                e.printStackTrace();
            }
        });

        tblUsers.setOnMouseClicked(event -> {
            UserItem selected = tblUsers.getSelectionModel().getSelectedItem();

            lblUserValue.setText(selected.getUsername());
            lblGroupValue.setText(selected.getGroup());
        });
    }

}
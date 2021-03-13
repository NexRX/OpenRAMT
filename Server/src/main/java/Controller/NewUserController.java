package Controller;

import Controller.Database.DBManager;
import Model.UserData;
import Model.UserGroup;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

public class NewUserController extends AnchorPane {
    @FXML JFXButton btnClose;
    @FXML JFXButton btnSubmit;
    @FXML Pane topBar;

    @FXML JFXTextField txtUsername;
    @FXML JFXTextField txtPassword;
    @FXML ComboBox<String> comboGroup;

    Stage stage;
    Stage callingStage;

    private double xOffset = 0;
    private double yOffset = 0;

    HashMap<String, UserGroup> groups = new HashMap<>();

    public NewUserController(Stage stage, Stage callingStage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/User/NewUser.fxml"));
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

        try {
            for (UserGroup group : DBManager.getAllGroups()) {
                //Instead of splitting by a certain char, this way is compat with all chars.

                String groupKey = group.getName() + " | " +
                        group.getAdmin() +", "+
                        group.getGeneral()+", "+
                        group.getProcess()+", "+
                        group.getMonitoring()+", "+
                        group.getPower()+".";

                groups.put(groupKey , group);

                comboGroup.getItems().add(groupKey);
            }
        } catch (SQLException e) { //ToDo Tell user about the error.
            e.printStackTrace();
        }
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

        btnClose.setOnMouseClicked(event -> {
            callingStage.show();
            stage.close();
        });

        btnSubmit.setOnMouseClicked(event -> {
            callingStage.show();
            stage.close();

            try {
                DBManager.addUser(getUserData());
            } catch (SQLException e) {
                //ToDo Tell user about the error.
                e.printStackTrace();
            }
        });

    }

    public UserData getUserData() {
        return new UserData(null,
                txtUsername.getText(),
                txtPassword.getText(),
                groups.get(comboGroup.getValue()).getName());
                //Todo check if the group exists before submitting, if it doesn't let cancel and tell user.
    }
}

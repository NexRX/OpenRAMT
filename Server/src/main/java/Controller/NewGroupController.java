package Controller;

import Controller.Database.DBManager;
import Model.UserGroup;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class NewGroupController extends AnchorPane {
    @FXML JFXButton btnClose;
    @FXML JFXButton btnSubmit;
    @FXML JFXTextField txtName;

    //Group Toggles
    @FXML JFXToggleButton toggleBtnAdmin;
    @FXML JFXToggleButton toggleBtnGeneral;
    @FXML JFXToggleButton toggleBtnProcess;
    @FXML JFXToggleButton toggleBtnMonitor;
    @FXML JFXToggleButton toggleBtnPower;

    @FXML Pane topBar;

    Stage stage;
    Stage callingStage;

    private double xOffset = 0;
    private double yOffset = 0;

    public NewGroupController(Stage stage, Stage callingStage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Group/NewGroup.fxml"));
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

            try { //Todo check if the group exists before submitting, if it doesn't let cancel and tell user.
                DBManager.addGroup(getUserGroup());
            } catch (SQLException e) {
                //ToDo Tell user about the error.
                e.printStackTrace();
            }
        });

    }

    public UserGroup getUserGroup() { //ToDo make DBManager accept a UserGroup object and change this class to pass this
        return new UserGroup(txtName.getText(),
                toggleBtnAdmin.isSelected(),
                toggleBtnGeneral.isSelected(),
                toggleBtnProcess.isSelected(),
                toggleBtnMonitor.isSelected(),
                toggleBtnPower.isSelected());
    }
}

package Controller.user;

import Controller.Database.DBManager;
import Controller.RAMTAlert;
import Model.User.UserData;
import Model.User.UserGroup;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

public class NewUserController extends AnchorPane {
    @FXML JFXButton btnClose;
    @FXML JFXButton btnSubmit;
    @FXML Pane topBar;

    @FXML JFXTextField txtUsername;
    @FXML JFXPasswordField txtPassword;
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
        } catch (SQLException e) {
            Alert alertError = new RAMTAlert(Alert.AlertType.ERROR,
                    "Application Alert.",
                    "The list of groups couldn't be fetched.",
                    "Return to previous menu as the list of groups couldn't be fetch.");
            alertError.showAndWait();

            e.printStackTrace();

            returnToCaller();
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

        btnClose.setOnMouseClicked(event -> {returnToCaller();});
        stage.setOnCloseRequest(event -> {returnToCaller();});

        btnSubmit.setOnMouseClicked(event -> {
            Alert alert = new RAMTAlert(Alert.AlertType.NONE);
            Alert.AlertType type;
            String alertMessage;
            boolean exit = false;

            try {
                switch (DBManager.addUser(getUserData())) {
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
                alert.setAlertType(type);
                alert.setTitle("Form Message");
                alert.setHeaderText(alertMessage);
                alert.setContentText("Click a button to continue.");
                alert.showAndWait();

                if (exit) {returnToCaller(); }
            } catch (SQLException e) {
                Alert alertError = new RAMTAlert(Alert.AlertType.ERROR,
                        "Application Error.",
                        "The application ran into a error with that request.",
                        "Please notify an admin. The error was an 'SQLException' if an admin asks.");
                alertError.showAndWait();

                e.printStackTrace();
                System.exit(1);
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

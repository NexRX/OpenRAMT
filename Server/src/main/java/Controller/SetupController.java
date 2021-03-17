package Controller;

import Controller.Database.DBManager;
import application.Launcher.MainStart;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
//import javafx.scene.control.DialogPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

import static Controller.CryptographyToolbox.generatePBKDF2WithHmacSHA512;

public class SetupController extends AnchorPane {
    private final Stage stage;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML JFXButton btnClose;
    @FXML JFXButton btnSetup;
    @FXML Pane topBar;

    // Form Children
    @FXML JFXTextField txtUsername;
    @FXML JFXPasswordField txtPassword;
    @FXML JFXPasswordField txtPasswordConfirm;

    @FXML ToggleGroup socketToggle;
    /*@FXML JFXRadioButton radioSecure;*/
    /*@FXML JFXRadioButton radioInsecure;*/
    @FXML JFXTextField txtPort;

    public SetupController(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Setup.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.stage = stage;

        applyEventHandlers();
    }

    private void applyEventHandlers() {

        btnSetup.setOnMouseClicked(event -> {
            try {
                String state = setup();

                if (state.equals("Success")) {

                    try {
                        MainStart.mainScene();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    Alert alert = new RAMTAlert(Alert.AlertType.ERROR,
                            "Invalid data.",
                            "Incorrect/Invalid information given.",
                            state);

                    alert.showAndWait();
                }
            } catch (InvalidKeySpecException | NoSuchAlgorithmException | SQLException e) {
                e.printStackTrace();
            }
        });

        btnClose.setOnMouseClicked(event -> {
            Platform.exit();
            System.exit(0);
        });

        topBar.setOnMousePressed(event -> { // These next two control window movement
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        topBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    private String setup() throws InvalidKeySpecException, NoSuchAlgorithmException, SQLException {
        // Username
        if ( txtUsername.getText().length() < 3 || txtUsername.getText().length() > 100) {
            return txtUsername.getText().length() < 3 ? "Username is too short" : "Username is too Long";
        }

        // Socket
        JFXRadioButton selectedRadioButton = (JFXRadioButton) socketToggle.getSelectedToggle();
        String socketType = selectedRadioButton.getText();

        boolean socket;
        /* "Secure (SSL/TLS)" */
        socket = !socketType.equals("Insecure (No SSL)");

        // (Default) Port
        int port = 3069;

        try {
            port = txtPort.getText().isEmpty() ? 3069 : Integer.parseInt(txtPort.getText());
        } catch (NumberFormatException e) {
            // do nothing.
        }


        if (port > 65535) {
            return "port is too big.";
        }

        //Password (most resource intensive here).
        String hashedPassword;
        if (txtPassword.getText().equals(txtPasswordConfirm.getText())) {
            hashedPassword = generatePBKDF2WithHmacSHA512(txtPasswordConfirm.getText());
        } else {
            return "Passwords do not match";
        }

        int setupCode = DBManager.setup(txtUsername.getText(), hashedPassword, socket, port);

        System.out.println("Setup Code: " + setupCode);

        switch (setupCode) {
            case 0:
                return "Success";
            case 2:
                return "One or more fields aren't valid, please check and try again.";
            default:
                System.out.println("Setup not 0. is: " + setupCode +
                        "| params are: " +
                        txtUsername.getText() +" "+
                        hashedPassword +" "+
                        socket+" "+
                        port);
                return "An error has occurred internally within the application.";
        }
    }
}
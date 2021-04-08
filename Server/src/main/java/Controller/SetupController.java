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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

import static Controller.CryptographyToolbox.generatePBKDF2WithHmacSHA512;

public class SetupController extends AnchorPane {
    private final Stage stage;

    private double xOffset = 0;
    private double yOffset = 0;

    private final FileChooser fileChooser = new FileChooser();

    @FXML JFXButton btnClose;
    @FXML JFXButton btnSetup;
    @FXML Pane topBar;

    // Form Children
    // Root User Details
    @FXML JFXTextField txtUsername;
    @FXML JFXPasswordField txtPassword;
    @FXML JFXPasswordField txtPasswordConfirm;

    // Socket Details
    @FXML ToggleGroup socketToggle;
    /*@FXML JFXRadioButton radioSecure;*/
    /*@FXML JFXRadioButton radioInsecure;*/
    @FXML JFXTextField txtPort;

    // Misc
    /*@FXML JFXButton btnCert;*/
    @FXML JFXButton btnCert;
    @FXML JFXTextField txtFTPUsername;
    @FXML JFXPasswordField txtFTPPassword;


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
        btnCert.setOnMouseClicked(e -> {
            new RAMTAlert(Alert.AlertType.INFORMATION,
                    "OpenRAMT Information",
                    "Import information, please read the following!",
                    "Keystore passwords are hard coded in this application, please make sure that the " +
                            "password of the keystore(JKS) formatted file are password protected with the following" +
                            "password 'jknm43c23C1EW342we'.\n\n" +
                            "Once a custom cert is imported, you must ensure clients have a copy of this imported file" +
                            "otherwise SSL connections will fail. Imports can be done by deleting the keystore.jks" +
                            "file in the data folder of this applications install directory.").showAndWait();

            File srcFile = fileChooser.showOpenDialog(stage);

            if (srcFile != null) {
                try {
                    Files.copy(srcFile.toPath(),
                            (new File(DBManager.dbPath + "keystore.jks")).toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ioException) {
                    new RAMTAlert(Alert.AlertType.ERROR,
                            "OpenRAMT Copy Error",
                            "That file couldn't be copied!",
                            "Please ensure the file is still there and isn't being used by another program.\n\n" +
                                    "You can also do this manually after setup by renaming the file to 'keystore.jks' and" +
                                    "copying it to the data folder where this application is installed.\n" +
                                    "The file must be in keystore(JKS) format and password protected with the" +
                                    "password 'jknm43c23C1EW342we'. It is not recommended to do this manually unless this" +
                                    "setup fails consistently.").showAndWait();
                }
            }
        });

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

        // Let user know one value in ftp details are empty.
        String ftpUsername = txtFTPUsername.getText();
        String ftpPassword = txtFTPPassword.getText();
        if (ftpUsername.isEmpty() || ftpPassword.isEmpty()) {
            new RAMTAlert(Alert.AlertType.INFORMATION,
                    "OpenRAMT Information",
                    "One or more FTP values are currently empty and will have default values!",
                    "Since the some FTP details aren't given, we are assigning a default value for them. \n" +
                            "It is recommended for security reasons that you change this especially for passwords.\n\n"+
                            "FTP Username Default:" + ftpUsername.isEmpty() + " | Default Value: RAMTUser\n" +
                            "FTP Password Default:" + ftpPassword.isEmpty() + " | Default Value: $%^DFG543*z\n\n" +
                            "These values (default or not) can be change later in the settings.").showAndWait();

            ftpUsername = ftpUsername.isEmpty() ? "RAMTUser" : ftpUsername;
            ftpPassword = ftpPassword.isEmpty() ? "$%^DFG543*z" : ftpPassword;
        }

        int setupCode = DBManager.setup(txtUsername.getText(), hashedPassword, socket, port, ftpUsername, ftpPassword);

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
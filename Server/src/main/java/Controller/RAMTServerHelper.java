package Controller;


import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;

public class RAMTServerHelper extends AnchorPane{
    private final Stage hostStage;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML JFXButton btnClose;
    @FXML Pane topBar;

    //TextFlows

    @FXML ScrollPane scrollGeneral;
    @FXML TextFlow tfGeneral;

    @FXML ScrollPane scrollAdmin;
    @FXML TextFlow tfAdmin;

    @FXML ScrollPane scrollFTP;
    @FXML TextFlow tfFTP;

    public RAMTServerHelper(Stage hostStage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Helper.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.hostStage = hostStage;

        // Set General Content
        setupGeneralTab();
        setupFTPTab();
        setupAdminTab();

        applyEventHandlers();
    }

    private void setupGeneralTab() {
        Label head1 = new Label("Welcome to OpenRAMT!\n");
        head1.setPrefWidth(scrollGeneral.getPrefWidth()-50);
        head1.setWrapText(true);
        head1.getStyleClass().add("lbl-header");

        Label txt1 = new Label("This window is meant to help you with using this application so please use this as a reference.\n\n" +
                "There may be information lacking from here and a strongly recommend to view the client helper which has more specific " +
                "information available at hand per tab of its main window.\n\n");
        txt1.setWrapText(true);
        txt1.setPrefWidth(scrollGeneral.getPrefWidth()-50);
        txt1.getStyleClass().add("lbl-regular");

        Label head2 = new Label("Window Layout.\n");
        head2.setPrefWidth(scrollGeneral.getPrefWidth()-50);
        head2.setWrapText(true);
        head2.getStyleClass().add("lbl-header");

        Label txt2 = new Label("Once in you'll see a 2 distinct sections of the application appear if all went well. 1st is the top bar. The top bar " +
                "of the application contains the help same help button you used to get here and a button to minimise, maximise and close " +
                "this application. You can also drag the top bar to move the application around. The top bar also contains a progress bar and " +
                "some text when active. This reflects the current state of any task *except* monitoring related tasks in the monitoring pane. " +
                "2nd and lastly is the admin menu which is used to make on the fly edits to users and groups. Please refer to the Admin tab " +
                "in this window.\n\n");
        txt2.setPrefWidth(scrollGeneral.getPrefWidth()-50);
        txt2.setWrapText(true);
        txt2.getStyleClass().add("lbl-regular");

        Label head3 = new Label("Extras.\n");
        head3.setPrefWidth(scrollGeneral.getPrefWidth()-50);
        head3.setWrapText(true);
        head3.getStyleClass().add("lbl-header");

        Label txt3 = new Label("Some final general information for yourself. Please ensure your always connection to " +
                "the server in secure or plain security mode that the server is using. If for some reason some goes wrong " +
                "you can always reset the program by deleting the data folder. This will delete all settings and users and " +
                "allow setting up the server again. Do this while the application is closed only however. Also, please attempt " +
                "to use the same version of the application where possible. It is recommended to keep a copy of the installer " +
                "for the client or a achieved copy of the installation of the client *especially* if your using custom certificates.\n\n" +
                "With that out of the way, please refer to any more tabs here for more specific information. You will find even more " +
                "on the clients helper.");
        txt3.setPrefWidth(scrollGeneral.getPrefWidth()-50);
        txt3.setWrapText(true);
        txt3.getStyleClass().add("lbl-regular");

        tfGeneral.getChildren().addAll(head1, txt1, head2, txt2, head3, txt3);
    }

    private void setupAdminTab() {
        Label head1 = new Label("Permissions\n");
        head1.setPrefWidth(scrollAdmin.getPrefWidth()-50);
        head1.setWrapText(true);
        head1.getStyleClass().add("lbl-header");

        Label txt1 = new Label("There are 5 permissions admins need be concerned about.\n" +
                "\t Admin - Master permission that grants all privileges, full access to everything including user " +
                "creation and group creation. Can change their own permissions too as well as others. Also, has ability " +
                "to change server settings.\n" +
                "\t General - Grants access to just the General tab and its abilities. Disk cleaning, ftp start, etc.\n" +
                "\t Process - Grants access to just the process tab. Allowing users to view, kill and restart processes.\n" +
                "\t Monitoring - Grants access to just monitoring data in the monitoring tab. Keeps permission until " +
                "monitoring disconnect, server restart or logout.\n" +
                "\t Power - Grants access to power tab. Allowing restart, shutdown, sleep * WOL of host system.\n" +
                "\n\n");
        txt1.setWrapText(true);
        txt1.setPrefWidth(scrollAdmin.getPrefWidth()-50);
        txt1.getStyleClass().add("lbl-regular");

        Label head2 = new Label("Uneditable Users\n");
        head2.setPrefWidth(scrollAdmin.getPrefWidth()-50);
        head2.setWrapText(true);
        head2.getStyleClass().add("lbl-header");

        Label txt2 = new Label("The default user and groups are uneditable so please bare this in mind, users in " +
                "the 'Default' group don't have any permissions but can still login to the welcome screen unless " +
                "suspended. The default groups cannot be deleted either. The default user can only have minor changes " +
                "to it as well as to protect the user from unwanted side effects like locking themselves out.\n\n");
        txt2.setWrapText(true);
        txt2.setPrefWidth(scrollAdmin.getPrefWidth()-50);
        txt2.getStyleClass().add("lbl-regular");

        tfAdmin.getChildren().addAll(head1, txt1, head2, txt2);
    }

    private void setupFTPTab() {
        Label head1 = new Label("How it works\n");
        head1.setPrefWidth(scrollFTP.getPrefWidth()-50);
        head1.setWrapText(true);
        head1.getStyleClass().add("lbl-header");

        Label txt1 = new Label("This server comes bundled with a embedded FTP server. You can configure it from " +
                "the settings menu in the client (See client helper for more information on settings). To connect to it " +
                "with the information set at setup or in the settings, you will need to use a FTP client or features " +
                "built into any OS to connect to it. This server doesn't have TLS built into its embedded FTP as of yet " +
                "so please be aware of this. The admin account has full access to the drive but the guest user is locked " +
                "to the data folder of the servers installation directory and only has read permissions. Bare in mind that " +
                "the servers database and certificate are also in this directory which may be a security concern for yourself.\n\n");
        txt1.setWrapText(true);
        txt1.setPrefWidth(scrollFTP.getPrefWidth()-50);
        txt1.getStyleClass().add("lbl-regular");

        tfFTP.getChildren().addAll(head1, txt1);
    }


    private void applyEventHandlers() {
        btnClose.setOnMouseClicked(event -> hostStage.close());

        topBar.setOnMousePressed(event -> { // These next two control window movement
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        topBar.setOnMouseDragged(event -> {
            hostStage.setX(event.getScreenX() - xOffset);
            hostStage.setY(event.getScreenY() - yOffset);
        });
    }
}

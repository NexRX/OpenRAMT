package Controller.Library;


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

public class RAMTClientHelper extends AnchorPane{
    private final Stage hostStage;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML JFXButton btnClose;
    @FXML Pane topBar;

    //TextFlows

    @FXML ScrollPane scrollGeneral;
    @FXML TextFlow tfGeneral;

    @FXML ScrollPane scrollPower;
    @FXML TextFlow tfPower;

    @FXML ScrollPane scrollProcess;
    @FXML TextFlow tfProcess;

    @FXML ScrollPane scrollAdmin;
    @FXML TextFlow tfAdmin;

    @FXML ScrollPane scrollMonitoring;
    @FXML TextFlow tfMonitoring;

    @FXML ScrollPane scrollSettings;
    @FXML TextFlow tfSettings;

    public RAMTClientHelper(Stage hostStage) {
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
        setupPowerTab();
        setupProcessTab();
        setupAdminTab();
        setupMonitoringTab();
        setupSettingsTab();

        applyEventHandlers();
    }

    private void setupGeneralTab() {
        Label head1 = new Label("Welcome to OpenRAMT!\n");
        head1.setPrefWidth(scrollGeneral.getPrefWidth()-50);
        head1.setWrapText(true);
        head1.getStyleClass().add("lbl-header");

        Label txt1 = new Label("This window is meant to help you with using this application so please use this as a reference.\n\n" +
                "First off, you'll need to log in of course. This can be done by filling in the host text field with the " +
                " name of the host or its IP address, followed by the port of the server. A valid example would be 127.0.0.1:3069\n" +
                "Next you'll need a username and password. If you're not the owner of the host (server owner) then you'll need " +
                "to ask the administrator of the system where the server is installed upon. They'll be able to provide you with " +
                "a username and password which you can fill into the respective text fields. Once you have done that, finish up " +
                "by pressing the 'Login' button at the bottom of the login window.\n\n");
        txt1.setWrapText(true);
        txt1.setPrefWidth(scrollGeneral.getPrefWidth()-50);
        txt1.getStyleClass().add("lbl-regular");

        Label head2 = new Label("Window Layout.\n");
        head2.setPrefWidth(scrollGeneral.getPrefWidth()-50);
        head2.setWrapText(true);
        head2.getStyleClass().add("lbl-header");

        Label txt2 = new Label("Once in you'll see a 3 distinct sections of the application appear if all went well. 1st is the top bar. The top bar " +
                "of the application contains the help same help button you used to get here and a button to minimise, maximise and close " +
                "this application. You can also drag the top bar to move the application around. The top bar also contains a progress bar and " +
                "some text when active. This reflects the current state of any task *except* monitoring related tasks in the monitoring pane. " +
                "2nd up we have the side bar which is used to change which panel you are currently viewing. Panels are displayed in the 3rd " +
                "section of the main window which we call the main content display. Here you will see the welcome panel by default and any panels " +
                "switched to.\n\n");
        txt2.setPrefWidth(scrollGeneral.getPrefWidth()-50);
        txt2.setWrapText(true);
        txt2.getStyleClass().add("lbl-regular");

        Label head3 = new Label("Extras.\n");
        head3.setPrefWidth(scrollGeneral.getPrefWidth()-50);
        head3.setWrapText(true);
        head3.getStyleClass().add("lbl-header");

        Label txt3 = new Label("Now some note worth points. If for some reason the server goes down you'll be able to tell by all tasks and monitoring being unable " +
                "to continue, so long as your details stay the same on the server, when it next goes back online you may continue. But only when the " +
                "server is back online. Also, if your details change mid session, you will lose the ability to continue with tasks, at which point you " +
                "must close the application and start it again while logging in with the new credentials. This may not be needed if your permissions change " +
                "but should be done anyway otherwise you'll see panes to which you may not have access to any thus will not work. A final point of order is " +
                "to make sure your connecting with the same security type as the server (secure or plain) otherwise connections will fail and if for some " +
                "something goes wrong in the future, delete the data folder in the server (and client for extra measure) as this is akin to a manual factory" +
                "reset.\n\n" +
                "With that out of the way, please refer to any more tabs here for more specific information.");
        txt3.setPrefWidth(scrollGeneral.getPrefWidth()-50);
        txt3.setWrapText(true);
        txt3.getStyleClass().add("lbl-regular");

        tfGeneral.getChildren().addAll(head1, txt1, head2, txt2, head3, txt3);
    }

    private void setupPowerTab() {
        Label head1 = new Label("General Power Information\n");
        head1.setPrefWidth(scrollPower.getPrefWidth()-50);
        head1.setWrapText(true);
        head1.getStyleClass().add("lbl-header");

        Label txt1 = new Label("The power tasks available should be used with caution. Once used you obviously will " +
                "not be able to use the server anymore. However, there are exceptions. If you have set your OS to auto " +
                "run OpenRAMT server at startup. You can restart or even use the wake function to do reconnection once " +
                "ramt has started up again. Please consult your operating systems instructions or help with this.\n\n");
        txt1.setWrapText(true);
        txt1.setPrefWidth(scrollPower.getPrefWidth()-50);
        txt1.getStyleClass().add("lbl-regular");

        Label head2 = new Label("Wake On Lan \n");
        head2.setPrefWidth(scrollPower.getPrefWidth()-50);
        head2.setWrapText(true);
        head2.getStyleClass().add("lbl-header");

        Label txt2 = new Label(" This feature isn't guaranteed to always work, it depends on your computer and " +
                "various firewall settings and BIOS configurations so please test its working for you first before " +
                "relying on it. Ideally you'll have a bios with magic packets enabled and a clear route for internet or" +
                "localhost network traffic to pass through.\n\n");
        txt2.setPrefWidth(scrollPower.getPrefWidth()-50);
        txt2.setWrapText(true);
        txt2.getStyleClass().add("lbl-regular");

        tfPower.getChildren().addAll(head1, txt1, head2, txt2);
    }

    private void setupProcessTab() {
        Label head1 = new Label("Heads-up / warning.\n");
        head1.setPrefWidth(scrollProcess.getPrefWidth()-50);
        head1.setWrapText(true);
        head1.getStyleClass().add("lbl-header");

        Label txt1 = new Label("The process tab works by using platform dependant code so please take note of the " +
                "following information. You can cause harm to your system when utilised wrong. Killing processes that " +
                "the system is depending on or another app may cause corrupt data or crashing. So use with care. Also, " +
                "this application depends on Java runtimes so when you see Java in the process list, it may be the Java " +
                "process this application depends on which if killed or restarted will cause this application to terminate " +
                "instantly. The same goes for the server.\n" +
                "*Some* process are protected by the system and may be un-killable, so if a process remains after a refresh " +
                "and kill, that may be why.\n\n");
        txt1.setWrapText(true);
        txt1.setPrefWidth(scrollProcess.getPrefWidth()-50);
        txt1.getStyleClass().add("lbl-regular");

        Label head2 = new Label("Restarting Processes \n");
        head2.setPrefWidth(scrollProcess.getPrefWidth()-50);
        head2.setWrapText(true);
        head2.getStyleClass().add("lbl-header");

        Label txt2 = new Label("Typically with most operating systems, restarting a process isn't possible. This " +
                "is for various technically reasons, but essentially this program doesn't start the same process exactly " +
                "but rather start a new process with a limited set of information to imitate a restarted process. Most " +
                "applications will not response well to a restart so again, use with care. It is ideal for restarting " +
                "'single threaded' applications like *some* browser instances. \n\n");
        txt2.setPrefWidth(scrollProcess.getPrefWidth()-50);
        txt2.setWrapText(true);
        txt2.getStyleClass().add("lbl-regular");

        Label head3 = new Label("Process Information (Status meaning, etc) \n");
        head3.setPrefWidth(scrollProcess.getPrefWidth()-50);
        head3.setWrapText(true);
        head3.getStyleClass().add("lbl-header");

        Label txt3 = new Label("Some process data is different depending on the OS you are running, so here is " +
                "some OS specific process information explained...\n\n");
        txt3.setPrefWidth(scrollProcess.getPrefWidth()-50);
        txt3.setWrapText(true);
        txt3.getStyleClass().add("lbl-regular");

        Label head4 = new Label("Windows: \n");
        head4.setPrefWidth(scrollProcess.getPrefWidth()-50);
        head4.setWrapText(true);
        head4.getStyleClass().add("lbl-header");

        Label txt4 = new Label("Process Name: The name assigned to the process.\n" +
                "PID: Short for Process Identification. A unique number used to identify a process.\n" +
                "Status: The running status of the process. It has 3 possible values...\n" +
                "\t Running - The process is actively performing tasks.\n" +
                "\t Suspended - The process is currently not executing tasks\n" +
                "\t Suspended(*) - Part of the process (some threads) is suspended but some is still running.\n" +
                "CPU: Represents the percentage of CPU usage of a single core. Can be higher than 100% for apps that use " +
                "more than one CPU core/thread i.e. 4 cores fully used would be 400%.\n" +
                "MEM: Presents the amount of physical RAM usage in BYTES.\n\n\n");
        txt4.setPrefWidth(scrollProcess.getPrefWidth()-50);
        txt4.setWrapText(true);
        txt4.getStyleClass().add("lbl-regular");

        Label head5 = new Label("Unix (Linux & MacOS): \n");
        head5.setPrefWidth(scrollProcess.getPrefWidth()-50);
        head5.setWrapText(true);
        head5.getStyleClass().add("lbl-header");

        Label txt5 = new Label("Process Name: The name of the executable file which the process is from.\n" +
                "PID: Short for Process Identification. A unique number used to identify a process.\n" +
                "Status: This has many values and is derived from the ps -aux command from a Unix terminal, meanings..." +
                "Here are the different values that the s, stat and state output\n" +
                "       specifiers (header \"STAT\" or \"S\") will display to describe the state of\n" +
                "       a process:\n" +
                "\n" +
                "               D    uninterruptible sleep (usually IO)\n" +
                "               I    Idle kernel thread\n" +
                "               R    running or runnable (on run queue)\n" +
                "               S    interruptible sleep (waiting for an event to complete)\n" +
                "               T    stopped by job control signal\n" +
                "               t    stopped by debugger during the tracing\n" +
                "               W    paging (not valid since the 2.6.xx kernel)\n" +
                "               X    dead (should never be seen)\n" +
                "               Z    defunct (\"zombie\") process, terminated but not reaped by\n" +
                "                    its parent\n" +
                "\n" +
                "       For BSD formats and when the stat keyword is used, additional\n" +
                "       characters may be displayed:\n" +
                "\n" +
                "               <    high-priority (not nice to other users)\n" +
                "               N    low-priority (nice to other users)\n" +
                "               L    has pages locked into memory (for real-time and custom IO)\n" +
                "               s    is a session leader\n" +
                "               l    is multi-threaded (using CLONE_THREAD, like NPTL pthreads\n" +
                "                    do)\n" +
                "               +    is in the foreground process group\n" +
                "CPU: Represents the percentage of CPU usage of a single core. Can be higher than 100% for apps that use " +
                "more than one CPU core/thread i.e. 4 cores fully used would be 400%.\n" +
                "MEM: Represents the amount of physical RAM usage as a total percentage.\n\n\n");
        txt5.setPrefWidth(scrollProcess.getPrefWidth()-50);
        txt5.setWrapText(true);
        txt5.getStyleClass().add("lbl-regular");

        Label head6 = new Label("Caveats\n");
        head6.setPrefWidth(scrollProcess.getPrefWidth()-50);
        head6.setWrapText(true);
        head6.getStyleClass().add("lbl-header");

        Label txt6 = new Label("Please note that some values presented may be out of date or slightly inaccurate in its snapshot as work " +
                "had to be done by the Os before during and after this task which effects the results. The application " +
                "running this task can also be intensive itself depending on the system running it which will temporarily " +
                "make the system appear to be running a higher load than it actually is.");
        txt6.setPrefWidth(scrollProcess.getPrefWidth()-50);
        txt6.setWrapText(true);
        txt6.getStyleClass().add("lbl-regular");

        tfProcess.getChildren().addAll(head1, txt1, head2, txt2, head3, txt3, head4, txt4, head5, txt5, head6, txt6);
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

    private void setupMonitoringTab() {
        Label head1 = new Label("Data\n");
        head1.setPrefWidth(scrollMonitoring.getPrefWidth()-50);
        head1.setWrapText(true);
        head1.getStyleClass().add("lbl-header");

        Label txt1 = new Label("The data retrieved from the host should be the same regardless of host unlike the " +
                "process manager. \n" +
                "-CPU Usage is out of 100% per polled rate (Default 3) defined.\n\n" +
                "-RAM usage is the same as the previous but displayed as a pie chart.\n\n" +
                "-Disk Usage shows the total of drives used with the charts max value being the largest disks space.\n\n" +
                "-Next chart shows IO usage which is the reads and writes performed between a small amount of time.\n\n" +
                "-CPU Temp is the temperature every polling rate in Celsius. \n\n" +
                "-Lastly, system temp is the average temperate of all the readable temperature data from the system. " +
                "-Including CPU usage and any power sources (Laptop batteries, etc).\n\n");
        txt1.setWrapText(true);
        txt1.setPrefWidth(scrollMonitoring.getPrefWidth()-50);
        txt1.getStyleClass().add("lbl-regular");

        Label head2 = new Label("Missing Data\n");
        head2.setPrefWidth(scrollMonitoring.getPrefWidth()-50);
        head2.setWrapText(true);
        head2.getStyleClass().add("lbl-header");

        Label txt2 = new Label("This is mostly a problem on windows but if you get missing data (most likely Temperature) " +
                "then first make sure the program is running as admin if that doesn't work and your on windows. You can install " +
                "a program called 'Open Hardware Monitor' which will running, may satisfy the dependency needed to fix " +
                "this issue for you. Otherwise, please report this as a bug to the OpenRAMT Github.\n\n");
        txt2.setWrapText(true);
        txt2.setPrefWidth(scrollMonitoring.getPrefWidth()-50);
        txt2.getStyleClass().add("lbl-regular");

        tfMonitoring.getChildren().addAll(head1, txt1, head2, txt2);
    }

    private void setupSettingsTab() {
        Label head1 = new Label("Real time changes\n");
        head1.setPrefWidth(scrollSettings.getPrefWidth()-50);
        head1.setWrapText(true);
        head1.getStyleClass().add("lbl-header");

        Label txt1 = new Label("Changes to settings can effect current connections and it is always recommended to " +
                "restart clients and servers after changing them. You can still continue however but new connections may " +
                "experience out of sync settings with the host as the get the changes processed first until the server " +
                "restarts.\n\n");
        txt1.setWrapText(true);
        txt1.setPrefWidth(scrollSettings.getPrefWidth()-50);
        txt1.getStyleClass().add("lbl-regular");

        Label head2 = new Label("Restrictions and Effects\n");
        head2.setPrefWidth(scrollSettings.getPrefWidth()-50);
        head2.setWrapText(true);
        head2.getStyleClass().add("lbl-header");

        Label txt2 = new Label("Some minimum requires for settings are set as constraints, attempts to change data " +
                "that doesn't form to the servers requirements will be reported back to the user. Some settings also require " +
                "a server restart like port settings and polling rate. At which point the server should be exited and " +
                "started again.\n\n");
        txt2.setWrapText(true);
        txt2.setPrefWidth(scrollSettings.getPrefWidth()-50);
        txt2.getStyleClass().add("lbl-regular");

        Label head3 = new Label("Factory Reset.\n");
        head3.setPrefWidth(scrollSettings.getPrefWidth()-50);
        head3.setWrapText(true);
        head3.getStyleClass().add("lbl-header");

        Label txt3 = new Label("This option in settings has major impacts so use carefully. You will be asked to confirm " +
                "and after confirming the server will process the request. If successful the server will exit and the client " +
                "requesting the request will be told if done successfully and then exited too after pressing OK in the alert. " +
                "The user must then start the server again manually and only then the data folder will be deleted in the " +
                "installation directory. This will wipe all settings and certs imported and the application will act as if " +
                "launched for the first time again. \n\n");
        txt3.setWrapText(true);
        txt3.setPrefWidth(scrollSettings.getPrefWidth()-50);
        txt3.getStyleClass().add("lbl-regular");

        tfSettings.getChildren().addAll(head1, txt1, head2, txt2, head3, txt3);
    }


    private void applyEventHandlers() {
        btnClose.setOnMouseClicked(e -> hostStage.close());

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

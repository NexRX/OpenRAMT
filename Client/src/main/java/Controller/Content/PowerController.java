package Controller.Content;

import Controller.RootController;
import Model.Task.Task;
import Model.Task.TaskRequest;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * The controller class for power related tasks.
 */
public class PowerController extends AnchorPane {
    @FXML JFXButton btnRestart;
    @FXML JFXButton btnSleep;
    @FXML JFXButton btnShutdown;
    @FXML JFXButton btnWake;

    private String lastRequestID;

    /**
     * Controller for handling power management.
     */
    public PowerController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/Power.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
        applyEventHandlers();
    }

    private void applyEventHandlers() {
        btnRestart.setOnMouseClicked(event -> lastRequestID = RootController.requestStart(new TaskRequest<>(Task.RESTART, RootController.getLoggedInUser())));

        btnSleep.setOnMouseClicked(event -> lastRequestID = RootController.requestStart(new TaskRequest<>(Task.SLEEP, RootController.getLoggedInUser())));

        btnShutdown.setOnMouseClicked(event -> lastRequestID = RootController.requestStart(new TaskRequest<>(Task.SHUTDOWN, RootController.getLoggedInUser())));

        btnWake.setOnMouseClicked(event -> wakeOnLAN(RootController.getLoggedInUser().getMacAddress(), RootController.getLoggedInUser().getHost()));
    }


    // Credit to Jibble for the tutorial.
    private void wakeOnLAN(byte[] macAddress, String ipAddress) throws IllegalArgumentException {
        byte[] bytes = new byte[6 + 16 * macAddress.length];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }
        for (int i = 6; i < bytes.length; i += macAddress.length) {
            System.arraycopy(macAddress, 0, bytes, i, macAddress.length);
        }

        InetAddress address;
        try {
            address = InetAddress.getByName(ipAddress);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 9);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

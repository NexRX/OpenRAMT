package Controller.Content;


import Controller.RootController;
import Model.User.UserData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Controller class for displaiyng welcome information
 */
public class WelcomeController extends VBox {
    @FXML AnchorPane container;

    @FXML Label lblHostValue;
    @FXML Label lblPortValue;
    @FXML Label lblConnectionValue;

    @FXML Label lblUsernameValue;
    @FXML Label lblGroupValue;

    /**
     * Controller for welcoming the user after login. This should be accessible always regardless of permission.
     */
    public WelcomeController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Content/Welcome.fxml"));
        this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            exception.printStackTrace();
            throw new RuntimeException(exception);
        }

        container.prefWidthProperty().bind(this.widthProperty());
        container.prefHeightProperty().bind(this.heightProperty());

        AnchorPane.setTopAnchor(this, 0.0);
        AnchorPane.setRightAnchor(this, 0.0);
        AnchorPane.setBottomAnchor(this, 0.0);
        AnchorPane.setLeftAnchor(this, 0.0);

        UserData user = RootController.getLoggedInUser();

        System.out.println(user.getUsername());

        lblHostValue.setText(user.getHost());
        lblPortValue.setText(String.valueOf(user.getPort()));
        lblConnectionValue.setText(String.valueOf(user.isSecure()));

        lblUsernameValue.setText(user.getUsername());
        lblGroupValue.setText(user.getGroup());
    }
}

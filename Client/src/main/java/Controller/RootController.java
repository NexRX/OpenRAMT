package Controller;

import Controller.Content.*;
import Controller.Library.SideButton;
import Model.AppPermission;
import application.Launcher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.swing.*;
import java.io.IOException;

/**
 * The root pains controller.
 * Handles most of the post-initial tasks as well as the many generic (non-specific pane) tasks.
 */
public class RootController extends AnchorPane {
	@FXML Pane resizeHelperR;
	@FXML Pane resizeHelperB;

	MainContentController mcc = new MainContentController(new WelcomeController());
	SideBarController sbc = new SideBarController(mcc);


	/**
	 * Constructs the AnchorPane and does a bulk of the post-initial tasks (First tasks after completing launch).
	 */
	public RootController() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Root.fxml"));
		this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();            
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		addInitialChildren();
		applyEventHandlers();
	}

	private void addInitialChildren() {
		// Top Bar
		TopBarController tb = new TopBarController();
		this.getChildren().add(tb);

		// Content Wizard
		//nothing :3

		this.getChildren().add(mcc);

		// Side Bar
		ScrollPane sbScroll = new ScrollPane(sbc);

		sbScroll.setStyle("-fx-background-color: #474343;");
		AnchorPane.setTopAnchor(sbScroll, 26.0);
		AnchorPane.setBottomAnchor(sbScroll, 0.0);
		AnchorPane.setLeftAnchor(sbScroll, 0.0);
		sbScroll.setMinWidth(100);
		sbScroll.setPrefWidth(100);
		sbScroll.setMaxWidth(100);
		sbScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		sbScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);


		this.getChildren().add(sbScroll);
		addPaneButton(new GeneralController(), "General", AppPermission.GENERAL, 1);
		addPaneButton(new AdminController(), "Admin", AppPermission.ADMINISTRATOR, 2);
		addPaneButton(new PowerController(), "Power", AppPermission.POWER, 3);
		addPaneButton(new ProcessController(), "Process", AppPermission.PROCESS, 4);
		addPaneButton(new SettingsController(), "Server Settings", AppPermission.ADMINISTRATOR, 5);

		resizeHelperR.toFront();
		resizeHelperB.toFront();
		tb.toFront(); // This one should be unneeded. But avoids some unexpected behavior found 'only' in testing.
	}

	private boolean addPaneButton(Node child, String btnName, AppPermission buttonPerm, int index) {
		mcc.getChildren().add(child);
		sbc.addButton(new SideButton(btnName, buttonPerm), index);
		return false;
	}

	private void applyEventHandlers() {
	    UndecoratedResizable.addResizeListener(Launcher.MainStart.getStage(), this, true);
	}
}

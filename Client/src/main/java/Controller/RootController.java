package Controller;

import application.Launcher;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

/**
 * The root pains controller.
 * Handles most of the innital tasks as well as the many generic (non-specific pane) tasks.
 */
public class RootController extends AnchorPane {
	/**
	 * Constructs the AnchorPane and does a bulk of the post-initial tasks.
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
		this.getChildren().add(new TopBarController());
		this.getChildren().add(new SideBarController());
		this.getChildren().add(new StackDisplayController());
	}

	private void applyEventHandlers() {
	    UndecoratedResizable.addResizeListener(Launcher.MainStart.getStage(), this);
	}
}

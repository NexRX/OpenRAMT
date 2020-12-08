package Controller;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * The root pains controller.
 * Handles most of the innital tasks as well as the many generic (non-specific pane) tasks.
 */
public class LauncherController extends AnchorPane {
	private final Stage stage;
	/**
	 * Constructs the AnchorPane and does a bulk of the initial tasks.
	 * @param stage The stage in which this controller or children may depend.
	 */
	public LauncherController(Stage stage) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/Launcher.fxml"));
		this.getStylesheets().add(getClass().getResource("/CSS/Launcher.css").toExternalForm());
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();            
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		
		this.stage = stage;

		addInitialChildren();
		applyEventHandlers();
	}

	private void addInitialChildren() {
		this.getChildren().add(new TopBarController(stage));
		this.getChildren().add(new SideBarController());
		this.getChildren().add(new StackDisplayController());
	}

	private void applyEventHandlers() {
	    UndecoratedResizable.addResizeListener(stage, this);
	}

}

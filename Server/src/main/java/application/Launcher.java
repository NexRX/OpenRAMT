package application;

import Controller.ManagementController;
import Controller.SetupController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Essentially the bootstrap to kickstart the main start class.
 */
public class Launcher {
    /**
     * The first function called to start all our processes.
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        Application.launch(MainStart.class);
    }

    public static class MainStart extends Application {
        private static Stage stage;
        @Override
        public void start(Stage stage) throws Exception {
            this.stage = stage;

            initScene();
        }

        public static void initScene() {
            stage.close();
            stage = new Stage();

            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(new SetupController(stage)));
            stage.getIcons().add(new Image("file:src/main/resources/openramt.png"));
            stage.setTitle("OpenRAMT");
            stage.setMinWidth(420);
            stage.setMinHeight(300);

            stage.show();
        }

        public static void mainScene() throws IOException {
            stage.close();
            stage = new Stage();

            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(new ManagementController(stage)));
            stage.getIcons().add(new Image("file:src/main/resources/openramt.png"));
            stage.setTitle("OpenRAMT");
            stage.setMinWidth(580);
            stage.setMinHeight(410);

            stage.show();
        }

        /**
         * After launcher, the most prompt function called. Finally does the launching.
         * @param args Command-line arguments.
         */
        public static void main(String[] args) {
            launch(args);
        }
    }
}

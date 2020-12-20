package application;

import Controller.InitialController;
import Controller.LauncherController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Essentially the bootstrap to kickstart the main start class.
 */
public class Launcher {
    /**
     * The first function called to start all our processes.
     * Experienced errors not starting like this.
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        Application.launch(MainStart.class);
    }


    public static class MainStart extends Application {
        static Stage stage;
        @Override
        public void start(Stage stage) throws Exception {
            this.stage = stage;

            stage.initStyle(StageStyle.UNDECORATED);

            InitialController ic = new InitialController();
            stage.setScene(new Scene(ic));
            stage.getIcons().add(new Image("file:src/main/resources/openramt.png"));
            stage.setTitle("OpenRAMT");
            stage.show();

            //stage.setMinWidth(800);
            //stage.setMinHeight(450);
            //stage.setScene(new Scene(new LauncherController(stage)));
        }

        public static Stage getStage() {
            return stage;
        }

        public void initScene() {
            stage.getScene().setRoot(new LauncherController());
            stage.setMinWidth(220);
            stage.setMinHeight(300);
        }

        public void mainScene() {
            stage.getScene().setRoot(new InitialController());
            stage.setMinWidth(800);
            stage.setMinHeight(450);
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



package application;

import Controller.InitialController;
import Controller.RootController;
import Model.User.UserData;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

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


    /**
     * Starting point for client OpenRAMT. Disabled JFoenix illegal reflection warnings and sets the stage.
     * the init method is then called for initialisation.
     */
    public static class MainStart extends Application {
        static Stage stage;
        @Override
        public void start(Stage stage) throws Exception {
            disableReflectionWarning();
            this.stage = stage;

            initScene();
        }

        /**
         * Gets the main stage of the application where the main window should appear. Use this to call center events or
         * actions from (sub-)controllers.
         * @return
         */
        public static Stage getStage() {
            return stage;
        }

        /**
         * Brings back the stage to the initial window for login.
         */
        public static void initScene() {
            stage.close();
            stage = new Stage();

            stage.initStyle(StageStyle.UNDECORATED);

            stage.setScene(new Scene(new InitialController()));
            stage.getIcons().add(new Image("file:src/main/resources/openramt.png"));
            stage.setTitle("OpenRAMT");
            stage.setMinWidth(220);
            stage.setMinHeight(300);
            stage.show();
        }

        /**
         * Used once at least after a login event is confirmed to start the main stage of the application.
         * @param user The userdata which was confirmed by the server.
         */
        public static void rootScene(UserData user) {
            stage.close();
            stage = new Stage();

            stage.initStyle(StageStyle.UNDECORATED);

            stage.setScene(new Scene(new RootController(user)));
            stage.getIcons().add(new Image("file:src/main/resources/openramt.png"));
            stage.setTitle("OpenRAMT");
            stage.setMinWidth(710);
            stage.setMinHeight(425);
            stage.show();
        }

        /**
         * This method disables the illegal access warning which is unnecessary from print to console every time.
         */
        private static void disableReflectionWarning() {
            try {
                Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                Unsafe unsafe = (Unsafe) theUnsafe.get(null);

                Class clazz = Class.forName("jdk.internal.module.IllegalAccessLogger");
                Field logger = clazz.getDeclaredField("logger");

                unsafe.putObjectVolatile(clazz, unsafe.staticFieldOffset(logger), null);
            } catch (Exception e) {
                // Do nothing.
            }
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



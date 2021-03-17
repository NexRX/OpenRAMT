package application;

import Controller.InitialController;
import Controller.RootController;
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
            disableReflectionWarning();
            this.stage = stage;

            initScene();
            //rootScene();
        }

        public static Stage getStage() {
            return stage;
        }

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

        public static void rootScene() {
            stage.close();
            stage = new Stage();

            stage.initStyle(StageStyle.UNDECORATED);

            stage.setScene(new Scene(new RootController()));
            stage.getIcons().add(new Image("file:src/main/resources/openramt.png"));
            stage.setTitle("OpenRAMT");
            stage.setMinWidth(710);
            stage.setMinHeight(425);
            stage.show();
        }

        public static void disableReflectionWarning() {
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



package application;

import Controller.Database.DBManager;
import Controller.ManagementController;
import Controller.RAMTAlert;
import Controller.SetupController;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import oshi.SystemInfo;
import sun.misc.Unsafe;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
            disableReflectionWarning();
            MainStart.stage = stage;

            // Detect if elevated
            boolean notElevated = !new SystemInfo().getOperatingSystem().isElevated();

            if (notElevated) {
                new RAMTAlert(Alert.AlertType.WARNING,
                        "OpenRAMT Warning (Not Elevated)",
                        "OpenRAMT is not elevated!!!",
                        "OpenRAMT is designed to be ran as administrator, running without is unsupported.\n" +
                                "You may continue but certain tasks will not work as warned here.\n\n" +
                                "Otherwise, please restart as Administrator / with sudo.\n" +
                                "(Note: Clients won't detect this as the problems.)").show();
                try {TimeUnit.SECONDS.sleep(5); } catch (InterruptedException ignored){} // Small warning wait
            }

            // Detect wipe needed.
            File markedForWipe = new File(DBManager.dbPath.getAbsolutePath()+".wipe");

            if (markedForWipe.isFile()) {
                DBManager.wipeDatabase();
                markedForWipe.delete();
            }

            // Detect Setup or start
            if (DBManager.isSetup()) {
                mainScene();
            } else {
                initScene();
            }
        }

        public static void initScene() {
            stage.close();
            stage = new Stage();

            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(new SetupController(stage)));
            stage.getIcons().add(new Image(Objects.requireNonNull(Launcher.class.getClassLoader().getResourceAsStream("openramt.png"))));
            stage.setTitle("OpenRAMT Setup");
            stage.setMinWidth(420);
            stage.setMinHeight(300);

            stage.show();
        }

        public static void mainScene() throws IOException {
            stage.close();
            stage = new Stage();

            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(new ManagementController(stage)));
            stage.getIcons().add(new Image(Objects.requireNonNull(Launcher.class.getClassLoader().getResourceAsStream("openramt.png"))));
            stage.setTitle("OpenRAMT");
            stage.setMinWidth(580);
            stage.setMinHeight(410);

            stage.show();
        }

        public static void disableReflectionWarning() {
            try {
                Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                Unsafe unsafe = (Unsafe) theUnsafe.get(null);

                Class<?> clazz = Class.forName("jdk.internal.module.IllegalAccessLogger");
                Field logger = clazz.getDeclaredField("logger");

                unsafe.putObjectVolatile(clazz, unsafe.staticFieldOffset(logger), null);
            } catch (Exception ignored) {}
        }

        /**
         * After launcher, the most prompt function called. Finally does the launching.
         * @param args Command-line arguments.
         */
        public static void main(String[] args) {
            launch(args);
        }

        /**
         * Fully closes the server ensuring that even if the OS calls for a window/stage to close, if the launcher stage
         * is closed then everything exists, including none JavaFX threads.
         */
        @Override
        public void stop(){
            System.exit(0);
        }
    }
}

package application;

import javafx.application.Application;

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
}

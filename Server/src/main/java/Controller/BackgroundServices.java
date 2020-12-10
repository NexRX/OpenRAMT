package Controller;

import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Label;

public class BackgroundServices<V> {
    JFXProgressBar jfxProgress;
    Label lblProgress;

    Service initService = new Service<V>() {
        @Override
        protected Task<V> createTask() {
            double progress = 0;
            for (int i = 0; i < 10; i++) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                progress += 0.1;
                final double reportedProgress = progress;

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        jfxProgress
                                .setProgress(reportedProgress);
                    }
                });
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    lblProgress.setText("Section Complete.");
                }

            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                }
            });
            return null;
        }
    };
}



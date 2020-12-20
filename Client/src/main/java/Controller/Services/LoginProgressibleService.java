package Controller.Services;

import Controller.Progressible;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class LoginProgressibleService extends Service<Void> implements Progressible {
    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() {
                setProgress(0d);
                double inc = (1d/7d); // total progress per loop and increment amount.
                for (double i = 0d; i < 1d; i+= inc) {
                    if (isCancelled()) {break;}// Finish early (also return null works)

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("InterruptedException Caught... Sleep Interrupted");
                    }

                    addProgress(inc);
                }
                return null;
            }
        };
    }
}

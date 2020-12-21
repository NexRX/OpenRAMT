package Controller.Services;

import Controller.Library.enums.Login;
import Controller.Progressible;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class LoginProgressibleService extends Service<Login> implements Progressible {
    @Override
    protected Task<Login> createTask() {
        return new Task<Login>() {
            @Override
            protected Login call() {
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
                return Login.SUCCESS;
            }
        };
    }
}

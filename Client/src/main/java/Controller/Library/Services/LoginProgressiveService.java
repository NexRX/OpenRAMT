package Controller.Library.Services;

import Controller.Library.enums.Login;
import Controller.Progressible;
import Controller.Library.Socket.ClientWorker;
import Controller.RAMTAlert;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import Model.User.UserData;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class houses the login constructor needed to craft an instance needed to do the work needed.
 */
public class LoginProgressiveService extends Service<TaskResponse<UserData>> implements Progressible {
    private UserData user;

    private final AtomicBoolean secure = new AtomicBoolean(true);

    /**
     * This service is designed to get the process userdata and get a response from the server defined in the request
     * given to contact the server and get more information about our user based on their known credentials. If the
     * request is successful the response in the service value will reflect that, same goes for failed logins with more
     * information provided in the response code.
     * @param user the user with a valid host (+port) and username + password to validate. Also instructions on if the
     *             server is secure or not.
     */
    public LoginProgressiveService(UserData user) {
        this.user = user;
    }

    public void updateUser(UserData user) {
        this.user = user;
    }

    @Override
    protected Task<TaskResponse<UserData>> createTask() {
        return new Task<>() {
            TaskResponse<UserData> result;
            @Override
            protected TaskResponse<UserData> call() throws IOException {
                setProgress(0d);
                double inc = (1d / 7d); // total progress per loop and increment amount.
                for (double i = 0d; i < 1d; i += inc) {
                    if (isCancelled()) {
                        break;
                    }// Finish early (also return null works)

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("InterruptedException. Sleep Interrupted!");
                    }

                    System.out.println("Starting client socket");
                    try {
                        FutureTask<TaskResponse<UserData>> futureTask = new FutureTask<>(new ClientWorker<>(new TaskRequest<>(Model.Task.Task.LOGIN, user), secure.get()));
                        Thread thread = new Thread(futureTask);
                        thread.start();
                        result = futureTask.get(); // will wait for the async completion
                    } catch (InterruptedException | ExecutionException e) {
                        Alert alert = new RAMTAlert(Alert.AlertType.ERROR,
                                "Application Error",
                                "An internal application error has occurred.",
                                "Please report this to an Admin/Developer as the application "+
                                        "has failed run properly. Proving information  as to what " +
                                        "happened prior to this error may help fix the issue promptly." +
                                        "\n\n The program will now shut down.");

                        e.printStackTrace();
                        alert.showAndWait();
                        Platform.exit();
                        System.exit(1);
                    }

                    if ((result != null ? result.getResponseCode() : 99) == 0) {
                        setProgress(1d);
                        return result;
                    } else {
                        addProgress(inc); //retry
                    }
                }

                return result;
            }
        };
    }

    public void setSecure(boolean secure) {
        this.secure.set(secure);
    }

    public void getSecure() {
        this.secure.get();
    }

    private void alertCertFailed() {
        Alert alert = new RAMTAlert(Alert.AlertType.ERROR,
                "Connection Certificate Error",
                "An error occurred using the bundled certificate.",
                "Please report this to an Admin/Developer as the certificate "+
                        "has failed to be loaded. Please ensure it was compiled correctly and provide "+
                        "what happened prior to this error to help fix the issue promptly." +
                        "\n\n The program will now shut down.");

        alert.showAndWait();
        Platform.exit();
        System.exit(1);
    }
}
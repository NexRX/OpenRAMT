package Controller.Library.Services;

import Controller.Library.enums.Login;
import Controller.Progressible;
import Controller.Library.Socket.ClientWorker;
import Model.TaskRequest;
import Model.TaskResponse;
import Model.UserData;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


public class LoginProgressibleService extends Service<Login> implements Progressible {
    private UserData user;

    public LoginProgressibleService(UserData user) {
        this.user = user;
    }

    public void updateUser(UserData user) {
        this.user = user;
    }

    @Override
    protected Task<Login> createTask() {
        return new Task<>() {
            @Override
            protected Login call() throws IOException {
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
                    TaskResponse result = null;
                    try {
                        FutureTask<TaskResponse> futureTask = new FutureTask<TaskResponse>(new ClientWorker(new TaskRequest(Model.Task.LOGIN, user)));
                        Thread thread = new Thread(futureTask);
                        thread.start();
                        result = futureTask.get(); // will wait for the async completion
                    } catch (UnrecoverableKeyException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException | InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        //Todo tell these errors to the user (in the switch statement) perhaps.
                    }

                    switch ((result != null ? result.getResponseCode() : 99)) {
                        case 0:
                            setProgress(1d);
                            return Login.SUCCESS;
                        case 10:
                            setProgress(0d);
                            return Login.FAILED_USERNAME;
                        case 11:
                            setProgress(0d);
                            return Login.FAILED_PASSWORD;
                        case 12:
                            setProgress(0d);
                            return Login.FAILED_SUSPENDED;
                        default: // Some error (probably not serious).
                            addProgress(inc); // essentially retry and add progress
                    }
                }

                return Login.FAILED_CONNECTION;
            }
        };
    }
}
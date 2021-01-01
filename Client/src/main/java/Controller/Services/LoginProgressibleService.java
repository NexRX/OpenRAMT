package Controller.Services;

import Controller.Socket.ClientWorker;
import Controller.Library.enums.Login;
import Controller.Progressible;
import Model.TaskRequest;
import Model.UserData;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;


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
        return new Task<Login>() {
            @Override
            protected Login call() throws IOException {
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
                    System.out.println("Starting client socket");
                    try {
                        new Thread(new ClientWorker(new TaskRequest(Model.Task.DEFAULT, user))).start();
                    } catch (UnrecoverableKeyException e) {
                        e.printStackTrace();
                    } catch (CertificateException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (KeyStoreException e) {
                        e.printStackTrace();
                    } catch (KeyManagementException e) {
                        e.printStackTrace();
                    }
                    addProgress(inc);
                }
                return Login.SUCCESS;
            }
        };
    }
}

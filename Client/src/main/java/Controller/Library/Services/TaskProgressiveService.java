package Controller.Library.Services;

import Controller.Progressible;
import Controller.Library.Socket.ClientWorker;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import Model.User.UserData;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


public class TaskProgressiveService extends Service<TaskResponse<?>> {
    private TaskRequest request;

    //private SSLSocket socket;
    private final char[] ksPwd = "jknm43c23C1EW342we".toCharArray();


    public TaskProgressiveService(TaskRequest request) {
        this.request = request;

        System.setProperty("javax.net.ssl.trustStore","data/keystore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", String.valueOf(ksPwd));
    }

    public void setRequest(TaskRequest request) {
        this.request = request;
    }

    /**
     * Updates the task and calls restart() in the service.
     *
     * @param request The new task to complete.
     */
    public void updateAndRestart(TaskRequest request) {
        this.request = request;
        this.restart();
    }

    @Override
    protected Task<TaskResponse<?>> createTask() {
        return new Task<>() {
            private final char[] ksPwd = "jknm43c23C1EW342we".toCharArray();

            @Override
            protected TaskResponse<?> call() throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException,  ClassNotFoundException {
                System.out.println("Starting client socket");
                jointUpdate(0.1f, "Starting");

                // Communications with server
                SSLSocket socket = (SSLSocket) generation();
                jointUpdate(0.33f, "Connected");

                ObjectOutputStream socketOutput = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream socketInput = new ObjectInputStream(socket.getInputStream());

                // Send request to the server.
                jointUpdate(0.45f, "Sending Request");
                socketOutput.writeObject(request);


                // Get the response
                jointUpdate(0.66f, "Server Processing");
                @SuppressWarnings("unchecked") // Safe when server & client respect request/response structure.
                TaskResponse<?> response = switch (request.getTask()) {
                    case FETCHPROCESSES ->  (TaskResponse<String>) socketInput.readObject();
                    default -> (TaskResponse<Void>) socketInput.readObject();
                };

                System.out.println("Response received: " + response.getRequestID() +" | "+ response.getResponse());

                jointUpdate(0.85f, "Finalising");
                //Stop Communications
                try {
                    socket.close(); } catch (IOException e) {e.printStackTrace();}

                jointUpdate(1f, "Finished");

                return response;
            }


            /**
             * Generates a certificate at runtime if (parts of) one are missing. Then returns a SSLSocket for use.
             * @return
             */
            private Socket generation() throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyManagementException {
                File ksFile = new File("data/keystore.jks");

                if(!ksFile.isFile()) {
                    jointUpdate(0.125f, "Creating Cert");
                    byte[] in = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("Cert/keystore.jks")).readAllBytes();

                    ksFile.getParentFile().mkdirs();
                    ksFile.createNewFile();

                    jointUpdate(0.15f, "Saving Cert");
                    FileOutputStream out = new FileOutputStream("data/keystore.jks");
                    out.write(in);
                    out.close();
                }

                jointUpdate(0.2f, "Loading Cert");

                KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(new FileInputStream(ksFile), ksPwd);


                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, ksPwd);

                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(ks);

                jointUpdate(0.3f, "Connecting");

                SSLContext sc = SSLContext.getInstance("TLS");

                TrustManager[] trustManagers = tmf.getTrustManagers();
                sc.init(kmf.getKeyManagers(), trustManagers, null);

                SSLSocketFactory ssf = sc.getSocketFactory();

                return ssf.createSocket(request.getUser().getHost(), request.getUser().getPort());

            }

            private void jointUpdate(float progress, String message) {
                updateProgress(progress, 1f);
                updateMessage("State: " + message);
            }
        };
    }
}

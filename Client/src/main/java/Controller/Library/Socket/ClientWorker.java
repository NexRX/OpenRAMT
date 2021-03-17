package Controller.Library.Socket;

import Model.Task.Response;
import Model.Task.TaskRequest;
import Model.Task.TaskResponse;

import javax.net.ssl.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.concurrent.Callable;

public class ClientWorker implements Callable<TaskResponse> {
    private SSLSocket socket;
    private final TaskRequest request;
    private final char[] ksPwd = "jknm43c23C1EW342we".toCharArray();

    public ClientWorker(TaskRequest request) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        System.setProperty("javax.net.ssl.trustStore","data/keystore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword","jknm43c23C1EW342we");

        this.request = request;
    }

    @Override
    public TaskResponse call() throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyManagementException {

        this.socket = (SSLSocket) Generation();

        TaskResponse result; // 1 for generic error because (if result hasn't changed) something hasn't gone as planned.
        try {
            result = work();
        } catch (ClassNotFoundException e) {
            System.out.println("Server's TaskResponse invalid, returning custom response & printing stacktrace...");
            e.printStackTrace();
            result = new TaskResponse(request, Response.FAILED, null);
        } finally {
            try {socket.close(); } catch (IOException e) {e.printStackTrace(); }
        }

        return result;
    }

    private TaskResponse work() throws IOException, ClassNotFoundException {
        ObjectOutputStream socketOutput = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream socketInput = new ObjectInputStream(socket.getInputStream());

        // Send request to the server.
        socketOutput.writeObject(request);

        // Get the response
        TaskResponse response = (TaskResponse) socketInput.readObject();
        System.out.println("Response received: " + response.getRequestID() +" | "+ response.getResponse());

        return response;
    }

    private Socket Generation() throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyManagementException {
        File ksFile = new File("data/keystore.jks");

        if(!ksFile.isFile()) {
            byte[] in = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("Cert/keystore.jks")).readAllBytes();
            ksFile.getParentFile().mkdirs();
            ksFile.createNewFile();
            FileOutputStream out = new FileOutputStream("data/keystore.jks");
            out.write(in);
            out.close();
        }

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(ksFile), ksPwd);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, ksPwd);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext sc = SSLContext.getInstance("TLS");

        TrustManager[] trustManagers = tmf.getTrustManagers();
        sc.init(kmf.getKeyManagers(), trustManagers, null);

        SSLSocketFactory ssf = sc.getSocketFactory();

        return ssf.createSocket(this.request.getUser().getHost(), this.request.getUser().getPort());

    }
}

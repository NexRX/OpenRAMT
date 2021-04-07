package Controller.Library.Socket;

import Model.Task.TaskRequest;
import Model.Task.TaskResponse;
import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.concurrent.Callable;

public class ClientWorker<T> implements Callable<TaskResponse<T>> {
    private static final char[] ksPwd = "jknm43c23C1EW342we".toCharArray();

    private Socket socket;
    private final boolean secure;

    private final TaskRequest<?> request;

    public ClientWorker(TaskRequest<?> request, boolean secure) {
        System.setProperty("javax.net.ssl.trustStore", "data/keystore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", String.valueOf(ksPwd));

        this.secure = secure;
        this.request = request;
    }

    @Override
    public TaskResponse<T> call() throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyManagementException, ClassNotFoundException {
        if (secure) {
            this.socket = secureGeneration(request.getUser().getHost(), request.getUser().getPort());
        } else {
            this.socket = new Socket(request.getUser().getHost(), request.getUser().getPort());
        }

        try { socket.close(); } catch (IOException ignored){}

        return work();
    }

    private TaskResponse<T> work() throws IOException, ClassNotFoundException {
        ObjectOutputStream socketOutput = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream socketInput = new ObjectInputStream(socket.getInputStream());

        System.out.println(request.getTask().toString());
        // Send request to the server.
        socketOutput.writeObject(request);

        // Get the response
        @SuppressWarnings("unchecked") // Safe when server & client respect request/response structure.
        TaskResponse<T> response = (TaskResponse<T>) socketInput.readObject();
        System.out.println("Response received: " + response.getRequestID() +" | "+ response.getResponse());

        return response;
    }

    /**
     * Generates a certificate at runtime if (parts of) one are missing. Then returns a SSLSocket for use.
     * @return A secure socket reflecting the environment requested.
     */
    private static SSLSocket secureGeneration(String host, int port) throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyManagementException {
        final File ksFile = new File("data/keystore.jks");

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

        return (SSLSocket) ssf.createSocket(host, port);

    }
}

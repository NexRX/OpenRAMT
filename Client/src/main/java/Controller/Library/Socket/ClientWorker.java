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
    private SSLSocket secureSocket;
    private Socket socket;

    private boolean secure;

    private final TaskRequest<?> request;
    private final char[] ksPwd = "jknm43c23C1EW342we".toCharArray();

    public ClientWorker(TaskRequest<?> request) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        System.setProperty("javax.net.ssl.trustStore","data/keystore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", String.valueOf(ksPwd));

        this.request = request;
    }

    public ClientWorker(TaskRequest<?> request, boolean secure) {
        System.setProperty("javax.net.ssl.trustStore", "data/keystore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", String.valueOf(ksPwd));

        this.secure = secure;
        this.request = request;
    }

    @Override
    public TaskResponse<T> call() throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyManagementException, ClassNotFoundException {
        if (secure) {
            this.secureSocket = secureGeneration();
        } else {
            this.socket = new Socket(this.request.getUser().getHost(), this.request.getUser().getPort());
        }

        System.out.println("Client Secure? " + secure);

        TaskResponse<T> result = work();
        if (secure) {
            try {
                secureSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private TaskResponse<T> work() throws IOException, ClassNotFoundException {
        ObjectOutputStream socketOutput = new ObjectOutputStream((secure ? secureSocket : socket).getOutputStream());
        ObjectInputStream socketInput = new ObjectInputStream((secure ? secureSocket : socket).getInputStream());

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
    private SSLSocket secureGeneration() throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyManagementException {
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

        return (SSLSocket) ssf.createSocket(this.request.getUser().getHost(), this.request.getUser().getPort());

    }
}

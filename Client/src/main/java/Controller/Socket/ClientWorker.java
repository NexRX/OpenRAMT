package Controller.Socket;

import Model.TaskRequest;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;

public class ClientWorker implements Runnable {
    private SSLSocket socket;
    private final TaskRequest request;
    private final char[] ksPwd = "jknm43c23C1EW342we".toCharArray();

    public ClientWorker(TaskRequest request) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        System.setProperty("javax.net.ssl.trustStore","data/keystore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword","jknm43c23C1EW342we");

        this.request = request;
    }

    @Override
    public void run() {
        try {
            this.socket = (SSLSocket) Generation();
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | KeyManagementException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        try {
            ObjectOutputStream socketOutput = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream socketInput = new ObjectInputStream(socket.getInputStream());

            System.out.println("Sending request to the ServerSocket");
            socketOutput.writeObject(request);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {socket.close(); } catch (IOException e) {e.printStackTrace(); }
        }

    }

    private Socket Generation() throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyManagementException {
        File ksFile = new File("data/keystore.jks");

        if(!ksFile.isFile()) {
            byte in[] = Thread.currentThread().getContextClassLoader().getResourceAsStream("Cert/keystore.jks").readAllBytes();
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

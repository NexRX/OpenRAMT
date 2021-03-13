package Controller.Socket;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Objects;

public class SecureServer implements Runnable {
    protected int port = 3069; // Default
    protected boolean isStopped = false;
    private boolean isInitialised = false;

    protected Thread runningThread = null;

    protected File ksFile = new File("data/keystore.jks");

    protected SSLServerSocket serverSocket;

    public SecureServer() { // default (use default port)
        try {
            initialisation();
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public SecureServer(int port) {
        this.port = port;

        try {
            initialisation();
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public boolean isInitialised() {
        return isInitialised;
    }

    @Override
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }

        if (isInitialised) {
            System.out.println("Server not initialised");
        }

        while (!isStopped()) {
            SSLSocket clientSocket;

            try {
                clientSocket = (SSLSocket) this.serverSocket.accept();
            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("Server Stopped.");
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }

            new Thread(new ServerWorker(clientSocket, "Multithreaded Server")).start();
        }

        System.out.println("Server Stopped.");
    }

    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop() {
        this.isStopped = true;
        try {
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);

        }
    }

    /**
     * Creates our keystore file which is used to create a certificate for our secure server connection.
     *
     * @throws IOException               If the file doesn't exist then this exception will be thrown.
     * @throws KeyStoreException         If the KeyStore file most likely doesn't contain correct keys. Can also be thrown for generic errors.
     * @throws NoSuchAlgorithmException  If the configured algorithm cannot be found then this exception is thrown.
     * @throws UnrecoverableKeyException Thrown when the key cannot be extracted from the keystore.
     * @throws CertificateException      This will only be thrown if the certificate is not valid.
     * @throws KeyManagementException    If keystore fails generally in the processes here then this exception is thrown.
     */
    private void initialisation() throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyManagementException {
        char[] ksPwd = "jknm43c23C1EW342we".toCharArray();

        if (!ksFile.isFile()) {
            Files.copy(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("Cert/keystore.jks")), Paths.get("data/keystore.jks"), StandardCopyOption.REPLACE_EXISTING);
        }

        System.out.println(ksFile.exists() + " | " + ksFile.getAbsolutePath() + " | " + ksFile.canRead() + " | " + ksFile.length());

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(ksFile), ksPwd);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, ksPwd);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext sc = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = tmf.getTrustManagers();
        sc.init(kmf.getKeyManagers(), trustManagers, null);

        SSLServerSocketFactory ssf = sc.getServerSocketFactory();
        this.serverSocket = (SSLServerSocket) ssf.createServerSocket(this.port);

        isInitialised = true;

    }
}
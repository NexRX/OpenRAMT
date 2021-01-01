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
    protected Thread runningThread = null;
    protected SSLServerSocket serverSocket = null;

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

    @Override
    public void run() {
        synchronized(this){this.runningThread = Thread.currentThread();}

        while(! isStopped()){
            SSLSocket clientSocket;

            try {
                clientSocket = (SSLSocket) this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }

            new Thread(new ServerWorker(clientSocket, "Multithreaded Server")).start();
        }

        System.out.println("Server Stopped.") ;
    }

    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void initialisation() throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyManagementException {
        File ksFile = new File("data/keystore.jks");
        char[] ksPwd = "jknm43c23C1EW342we".toCharArray();

        //if (!ksFile.isFile()) { //This was to support future implementation of replaceable cert.
            Files.copy(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("Cert/keystore.jks")), Paths.get("data/keystore.jks"), StandardCopyOption.REPLACE_EXISTING);
        //}

        System.out.println(ksFile.exists() + " | " + ksFile.getAbsolutePath() + " | " +  ksFile.canRead()  + " | " +  ksFile.length());

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
    }

}

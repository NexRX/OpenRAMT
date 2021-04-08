package Controller.Socket;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
public class SecureMonitoringServer extends PlainMonitoringServer {

    public SecureMonitoringServer() {
        super();
    }

    /**
     * Creates our secure server by depending on the SecureServer implementation of initialisation.
     *
     * @throws IOException      If the file doesn't exist then this exception will be thrown.
     * @throws BindException    If the port or server couldn't be opened when creating server.
     * @return A new secure server socket using the information composed form the class.
     */
    @Override
    protected ServerSocket initialisation(int port) throws IOException {
        System.out.println("Secure Monitoring Init");
        try {
            return SecureServer.initialisation(port);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | KeyManagementException e) {
            e.printStackTrace();
            return null;
        }
    }
}

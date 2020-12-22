package Controller.Socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SecureServer implements Runnable {
    protected int port = 3069; // Default
    protected boolean isStopped = false;
    protected Thread runningThread = null;
    protected ServerSocket serverSocket = null;

    public SecureServer() { // default (use default port)
    }

    public SecureServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        synchronized(this){this.runningThread = Thread.currentThread();}
        openServerSocket();

        while(! isStopped()){
            Socket clientSocket = null;

            try {
                clientSocket = this.serverSocket.accept();
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

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open " + this.port, e);
        }
    }

}

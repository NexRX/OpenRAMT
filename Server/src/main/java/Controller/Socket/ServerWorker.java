package Controller.Socket;

import Model.TaskRequest;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**

 */
public class ServerWorker implements Runnable{
    protected SSLSocket clientSocket = null;
    protected String serverText = null;

    public ServerWorker(SSLSocket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
    }

    public void run() {
        System.out.println(clientSocket);
        try {
            // create a DataInputStream so we can read data from it.
            ObjectInputStream socketInput = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream socketOutput = new ObjectOutputStream(clientSocket.getOutputStream());

            // read the list of messages from the socket
            TaskRequest request = (TaskRequest) socketInput.readObject();
            System.out.println("Received [" + request.getUser() + "] messages from: " + clientSocket);

            //ToDo When completing, Start a new thread here with a new task
            // (defined by switch statement) and then get that task to return
            // the errors and attack a listener to the tasks onSucced to update
            // the user on its success/failure. Update Progress of task to client.
            // Idea on remote progress update. Only have 3 states in which client is
            // updated. 33% starting, 66% started, 100% finished etc.

        } catch (IOException | ClassNotFoundException e) {
            //report exception somewhere.
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("Socket close completed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
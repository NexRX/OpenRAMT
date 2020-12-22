package Controller.Library;

import Model.TaskRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientWorker implements Runnable {
    private final Socket socket;
    private final TaskRequest request;

    public ClientWorker(TaskRequest request) throws IOException {
        this.request = request;
        this.socket = new Socket(this.request.getUser().getHost(), this.request.getUser().getPort());
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream socketOutput = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream socketInput = new ObjectInputStream(socket.getInputStream());

            System.out.println("Sending messages to the ServerSocket");
            socketOutput.writeObject(request);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

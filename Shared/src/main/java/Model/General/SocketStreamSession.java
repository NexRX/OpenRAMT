package Model.General;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketStreamSession {
    private final Socket socket;
    private final ObjectInputStream inputSocket;
    private final ObjectOutputStream outputSocket;

    /**
     * A object used to encapsulate an object streaming session of a socket and transport it to another thread.
     * This object doesn't garentee any degree of Thread safety or socket cohesion so this must be upheld independently.
     * @param socket The socket of the object streams
     * @param inputSocket The input stream of object for the socket.
     * @param outputSocket The output stream of object for the socket.
     */
    public SocketStreamSession(Socket socket, ObjectInputStream inputSocket, ObjectOutputStream outputSocket) {
        this.socket = socket;
        this.inputSocket = inputSocket;
        this.outputSocket = outputSocket;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectInputStream getInputSocket() {
        return inputSocket;
    }

    public ObjectOutputStream getOutputSocket() {
        return outputSocket;
    }
}

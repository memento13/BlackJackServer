import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(7890);
        String passcode = args[0];

        boolean alive = true;
        while (alive){
            Socket socket = serverSocket.accept();
            new Game(socket,passcode).start();
        }

        serverSocket.close();

    }
}

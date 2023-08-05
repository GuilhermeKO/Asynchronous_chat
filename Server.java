import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {

        try {

            while (!serverSocket.isClosed()) {

                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        try { //deleting txt files for new runs
            File file = new File("/home/guilherme/java_SD/projeto_2/server.txt");
            file.delete();

            file = new File("/home/guilherme/java_SD/projeto_2/client1.txt");
            file.delete();

            file = new File("/home/guilherme/java_SD/projeto_2/client2.txt");
            file.delete();

        } catch (Exception e){
            e.printStackTrace();;
        }

        ServerSocket serverSocket = new ServerSocket(5000);
        Server server = new Server(serverSocket);
        server.startServer();
        server.closeServerSocket();
    }
}
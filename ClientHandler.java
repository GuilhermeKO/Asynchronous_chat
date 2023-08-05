import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Calendar;
import java.text.SimpleDateFormat;
public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> vet_client = new ArrayList<>();
    private LinkedList<String> fifo = new LinkedList<>();
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String name;
    private FileOutputStream fos;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.name = bufferedReader.readLine();
            vet_client.add(this);

            try {
                this.fos = new FileOutputStream("server.txt", true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String sentence = (name + " is online " + (formatter.format(calendar.getTime())));
            fifo.add(sentence);
            write_file_operation(sentence);
            broadcast_operation(name + " is online");

        } catch (IOException e) {
            end_all(socket, bufferedReader, bufferedWriter);
        }
    }

    public void write_file_operation (String message) throws IOException {
        fos.write(message.getBytes());
        fos.write("\n".getBytes());
        fos.flush();
    }

    @Override
    public void run() {
        String message_received;

        Calendar calendar;
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        while(socket.isConnected()) {
            try {

                calendar = Calendar.getInstance();
                message_received = (bufferedReader.readLine());

                if(message_received == null) throw new IOException(); //null exception for offline procedure

                if(message_received.equalsIgnoreCase("is_online_feedback_message")){ //broadcasts FIFO content at the moment that other client gets online again
                    if(vet_client.size() == 2){//------------------------------
                        while (!fifo.isEmpty()){
                            broadcast_operation(fifo.removeFirst());
                        }
                    }
                } else {

                    message_received = (message_received + "  " + (formatter.format(calendar.getTime())));

                    //write on server file
                    write_file_operation(message_received);

                    //data structure to hold messages
                    fifo.add(message_received);

                    //server only broadcasts if both clients are online
                    if (vet_client.size() == 2) {
                        while (!fifo.isEmpty()) {
                            broadcast_operation(fifo.removeFirst());
                        }
                    }
                }
            } catch (IOException e) {
                end_all(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcast_operation(String message) {
        for (ClientHandler clientHandler : vet_client) {
            try {
                clientHandler.bufferedWriter.write(message);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            } catch (IOException e) {
                end_all(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void remove_client() {
        vet_client.remove(this);

        broadcast_operation(name + " is offline");
    }

    public void end_all(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        remove_client();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
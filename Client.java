import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private FileOutputStream fos;
    public Client (Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;

            try {
                this.fos = new FileOutputStream(username + ".txt", true);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            end_all(socket, bufferedReader, bufferedWriter);
        }
    }

    public void send_message() {
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(username + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            end_all(socket, bufferedReader, bufferedWriter);
        }
    }

    public void write_file_operation(String message_read) throws IOException {
        fos.write(message_read.getBytes());
        fos.write("\n".getBytes());
        fos.flush();
    }

    public void receive_message() {
        new Thread(() -> {
            String message_read;

            while (socket.isConnected()) {
                try {
                    message_read = bufferedReader.readLine();
                    if(message_read.equalsIgnoreCase("client1 is offline") || message_read.equalsIgnoreCase("client2 is offline")){ //Informs client prompt when other is offline
                        System.out.println(message_read);
                    }

                    if(!(message_read.equalsIgnoreCase("client1 is online") || message_read.equalsIgnoreCase("client1 is offline") || message_read.equalsIgnoreCase("client2 is online") || message_read.equalsIgnoreCase("client2 is offline"))){
                        System.out.println(message_read);
                        write_file_operation(message_read);

                    } else {
                        if(message_read.equalsIgnoreCase("client1 is online") || message_read.equalsIgnoreCase("client2 is online")){
                            bufferedWriter.write("is_online_feedback_message"); //receives feedback other client is online, sends specific message tom server to request sending FIFO
                        } else {
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                            bufferedWriter.write(message_read);
                        }
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    end_all(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    public void end_all(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
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
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please state your name: ");
        String name = scanner.nextLine();

        Socket socket = new Socket("localhost", 5000);
        Client client = new Client(socket, name);

        client.receive_message();
        client.send_message();
    }
}
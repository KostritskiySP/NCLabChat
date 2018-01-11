import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;

import java.io.IOException;
import java.io.*;
import java.net.InetAddress;
import java.net.*;
import java.util.ArrayList;

public class SocketClient implements ConnectionListener {
    private final static int serverPort = Config.PORT;
    private final static String host = Config.HOST;
    private ArrayList<ServerListenerThread> connections = new ArrayList<>();
    private static String userName = "";
    static Socket socket = null;
    private ServerListenerThread connection;
    XStream xStream = new XStream(new XppDriver());

    public static void main(String[] args) {
        try {
            new SocketClient(host, serverPort);
        } catch (IOException e) {
            System.out.println("Unable to connect. Server not running?");
        }
    }
    public SocketClient(String host, int port) throws IOException {
        System.out.println("Вас приветствует клиент чата!\n");

        socket = new Socket(host, port);
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        try {
            String login;
            String password;
            connection = new ServerListenerThread(socket, outputStream, inputStream);
            String message = null;
            System.out.println("Логин: ");
            login =keyboard.readLine();
            System.out.println("Пароль: ");
            password =keyboard.readLine();

            while (true) {
                System.out.println("Введите команду");
                message = keyboard.readLine();
                if (message.equals("!authorize")) {
                    connection.autoriz(login,password);
                } else if (message.equals("!registration")) {
                    connection.registr(login,password);
                }
                else connection.send(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
            @Override
            public void onConnectionReady (ServerListenerThread tcpConnection){
            }

            @Override
            public void onReceiveMessage (ServerListenerThread сonnect, Message message){

            }

            @Override
            public void onDisconnect (ServerListenerThread сonnect){

            }

            @Override
            public void onException(ServerListenerThread сonnect, Exception e){

            }

        }



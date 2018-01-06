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
//    ObjectOutputStream objectOutputStream;
//    ObjectInputStream objectInputStream;
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
        System.out.println("Введите команду");
        socket = new Socket(host, port);
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        try {
            userName = keyboard.readLine();
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
//            try {
//                objectOutputStream = new ObjectOutputStream(outputStream);
//                objectInputStream = new ObjectInputStream(inputStream);
                //new Thread(new ServerListenerThread(objectOutputStream, objectInputStream)).start();
                connection = new ServerListenerThread(socket, outputStream, inputStream);
                String message = null;

                System.out.println("Наберите сообщение и нажмите \"Enter\"\n");

                while (true) {
                    message = keyboard.readLine();

                    if (message.equals("!authorize")) {
                        connection.autoriz();
                        System.out.println("ВВЕД лог затем пароль");
                    } else if (message.equals("!registration")) {
                        connection.registr();
                        System.out.println("ВВЕД лог затем пароль");
                    }
                    else connection.send(new Message(userName, message));
//                    objectOutputStream.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//        }
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



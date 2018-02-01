import java.io.IOException;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class SocketClient {
    private static int serverPort;
    private final static String host = Config.HOST;
    private ConnectToClient connections;
    private static Socket socket = null;
    private ArrayList<String> onlineList = null;


    public static void main(String[] args) throws IOException {
        System.out.println("Введите порт!\n");
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        int p = Integer.parseInt(keyboard.readLine());
        setPort(p);
        System.out.println("Port:" + serverPort);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new SocketClient(host, serverPort);
                } catch (IOException e) {
                    System.out.println("Unable to connect. Server not running?");
                }
            }
        });
        thread.start();
    }

    public SocketClient(String host, int port) throws IOException {
        System.out.println("Вас приветствует клиент чата!\n");
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        socket = new Socket(host, port);
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        try {
            String login;
            String password;
            connections = new ServerListenerThread(socket, outputStream, inputStream);
            String message = null;
            connections.send("Client");
            System.out.println("Логин: ");
            login = keyboard.readLine();
            System.out.println("Пароль: ");
            password = keyboard.readLine();
            System.out.println("Введите команду");
            message = keyboard.readLine();

            if (message.equals("!authorize")) {
                if (connections.authorization(login, password))
                    System.out.println("Succsess");
            } else if (message.equals("!registration"))
                System.out.println(connections.registration(login, password));
            connections.printMessage();

            while (true) {
                System.out.println("Введите сообщение");
                message = keyboard.readLine();
                if (message.equals("!HISTORY"))
                    connections.getHistoryChat();
                else if (message.equals("!LOGOUT"))
                    connections.logOut();
                else if (message.equals("!online")) {
                    onlineList = connections.getOnlineUser();
                    System.out.println("Список онлайн" + onlineList);
                } else connections.send(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void setPort(int p){
        for(int i=0;i<Config.PORTARRAY.length;i++)
            if(Config.PORTARRAY[i]==p)
                serverPort=p;
    }
}



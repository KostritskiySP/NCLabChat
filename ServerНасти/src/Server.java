import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.xml.DomDriver;
public class Server implements UsersData {

    private UserList list = new UserList();
    private ChatHistory chatHistory = new ChatHistory();
    private ArrayList<Server> serverList = new ArrayList<Server>();

    public static void main(String[] args) {
        try {
            Server server = new Server();
            ServerSocket socketListener = new ServerSocket(Config.PORT);
            System.out.println("\nWaiting for a client...");
            while (true) {
                Socket client = null;
                while (client == null) {
                    client = socketListener.accept();
                }
                new ClientServiceThread(client, server);
            }
        } catch (SocketException e) {
            System.err.println("Socket exception");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("I/O exception");
            e.printStackTrace();
        }
    }

    @Override
    public UserList getUserList() {
        return list;
    }

    @Override
    public ChatHistory getChatHistory() {
        return chatHistory;
    }

    @Override
    public boolean addMessage(Message message) {
        return false;
    }
}

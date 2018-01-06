import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by shado_000 on 14.12.2017.
 */
public interface ServerData {
//    ArrayList<ClientServiceThread> getClientServiceThreads();

    ArrayList<Message> getChatHistory();

    boolean addMessage(Message message);

    boolean addLogin(String login);

    ArrayList<String> getUserList();

    HashMap<String,String> getRegisteredUsers();

    void addRegisteredUser(String login,String password);
}

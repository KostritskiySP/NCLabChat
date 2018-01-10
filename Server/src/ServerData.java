import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by shado_000 on 14.12.2017.
 */
public interface ServerData {

    List<Message> getChatHistory();

    /**
     * returns a list of currently online users
     */
    ArrayList<String> getOnlineUsers();


    String registration(Account account);

    String authorization(Account account);
}

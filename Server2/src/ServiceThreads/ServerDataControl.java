package ServiceThreads;

import Entities.Account;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shado_000 on 14.12.2017.
 */
public interface ServerDataControl {

    /**
     * returns server chat history
     */
    List<Entities.ServerMessage> getChatHistory();

    /**
     * returns a list of currently online users
     */
    ArrayList<String> getOnlineUsers();

    /**
     * starts a registration process on server
     */
    String registration(Account account);
    /**
     * starts an authorization process on server
     */
    String authorization(Account account);
}

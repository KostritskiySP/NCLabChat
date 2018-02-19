import java.util.ArrayList;
public interface ConnectionListener {

    void onConnectionReady(ServerListenerThread tcpConnection);

    void onReceiveMessage(ServerListenerThread сonnect, Entities.Message message);

    void onlineUsers(ServerListenerThread сonnect, ArrayList<String> onlineList);

    void onException(ServerListenerThread сonnect, Exception e);

}
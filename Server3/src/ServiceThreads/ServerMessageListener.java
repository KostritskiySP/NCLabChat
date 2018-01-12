package ServiceThreads;

import Entities.Message;
import Entities.ServerMessage;

/**
 * Created by Sergio on 16.12.2017.
 */
public interface ServerMessageListener {
    /**
     * starts a send request on server
     */
    public void broadcast(ServerMessage message);
}

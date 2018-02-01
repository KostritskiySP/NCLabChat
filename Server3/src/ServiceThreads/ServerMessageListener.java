package ServiceThreads;

import Entities.Message;
import Entities.ServerMessage;

/**
 * Created by Sergio on 16.12.2017.
 */
public interface ServerMessageListener {
    /**
     * broadcasts a message to clients and nearby servers
     * if loop detected - does nothing
     * @return false if loop detected, true otherwise
     */
    public boolean broadcast(ServerMessage message);

    /**
     * broadcasts a message nearby servers
     */
    public void broadcastOnNet(ServerMessage message);
}

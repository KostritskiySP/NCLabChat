package ServiceThreads;

import Entities.ServerMessage;

/**
 * Created by shado_000 on 14.12.2017.
 */
public interface ServiceMessageSender {
    /**
     * sends a message to user
     */
    boolean sendMessage(ServerMessage message);
}

package ServiceThreads;

import ServiceThreads.ClientServiceThread;

/**
 * Created by Sergio on 20.12.2017.
 */
public interface ClientDisconnectionListener {
    /**
     * indicates whenever client is disconnected
     */
    public boolean clientDisconnected(ClientServiceThread clientServiceThread);

}

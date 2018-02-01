package ServiceThreads;

/**
 * Created by Sergio on 11.01.2018.
 */
public interface ServersDisconnectionListener {
    /**
     * indicates whenever server is disconnected
     */
    public boolean serverDisconnected(ServerNetServiceThread serverNetServiceThread);
}

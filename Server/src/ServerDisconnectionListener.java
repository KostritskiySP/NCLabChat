/**
 * Created by Sergio on 20.12.2017.
 */
public interface ServerDisconnectionListener {
    public void clientDisconnected(ClientServiceThread clientServiceThread, String login);
}

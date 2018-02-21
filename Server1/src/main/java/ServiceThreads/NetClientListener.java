package ServiceThreads;

/**
 * Created by Sergio on 30.01.2018.
 */
public interface NetClientListener {

    public void netClientDisconnected(String login);

    public void netClientConnected(String login);

}

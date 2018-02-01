package ServiceThreads;

/**
 * Created by Sergio on 30.01.2018.
 */
public interface NetClientListener {

    public boolean netClientDisconnected(String login);

    public void netClientAppeared(String login);

}

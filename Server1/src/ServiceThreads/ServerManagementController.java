package ServiceThreads;

/**
 * Created by Sergio on 01.02.2018.
 */
public interface ServerManagementController {
    /**
     * disconnects client from server
     * @return false - if client with such login not found, true if success
     */
    public boolean kick(String login);
}

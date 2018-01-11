import java.io.IOException;
import java.util.ArrayList;

public interface ConnectToClient {
    void send(String message);
    void disconnect();
    boolean authorization(String login,String password) throws IOException;
    boolean registration(String login,String password) throws IOException;
    ArrayList<String> listOnline() throws IOException;
    String getMessage() throws IOException;
}

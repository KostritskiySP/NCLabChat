
import java.io.Serializable;

public class Message implements Serializable {

    private String from;
    private String message;
    //private String[] users;

    public Message(String from, String message){
        this.from = from;
        this.message = message;
    }

    public String getFrom() {
        return this.from;
    }

    public String getMessage() {
        return this.message;
    }

}

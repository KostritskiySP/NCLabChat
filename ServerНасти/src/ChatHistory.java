import java.io.Serializable;
import java.util.*;

public class ChatHistory implements Serializable {
    private List<Message> history;

    public ChatHistory() {
        this.history = new ArrayList<Message>();
    }

    public void addMessage(Message message){

        this.history.add(message);
    }

    public List<Message> getHistory(){
        return this.history;
    }

}
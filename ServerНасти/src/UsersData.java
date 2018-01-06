/**
 * Created by shado_000 on 14.12.2017.
 */
public interface UsersData {
    UserList getUserList();

    ChatHistory getChatHistory();

    boolean addMessage(Message message);

}

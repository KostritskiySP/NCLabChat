import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.xml.XppDriver;
import Entities.Account;
import Entities.Message;

/**
 * Класс, связывающийся с сервером
 */

public class ServerListenerThread implements ConnectToClient {
    private Socket socket;
    private Thread thread = null;
    private ArrayList<String> onlineList = null;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Message messageIn = null;
    private Message message=null;
    private XStream xStream= new XStream(new XppDriver());


    public ServerListenerThread(Socket socket, OutputStream outputStream, InputStream inputStream) {
        this.socket = socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }


    public void startThread(ConnectionListener listener) {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!thread.isInterrupted()) {
                    messageIn = receiptMessage();
                    System.out.println(messageIn.getMessage());
                    if ((messageIn.getFrom().equals("Server"))&& (messageIn.getMessage().equals("#Online"))){
                        if(messageIn != null) {
                            ArrayList<String> online = getOnline();
                            listener.onlineUsers(ServerListenerThread.this, online);
                        }
                    }else {
                        listener.onReceiveMessage(ServerListenerThread.this, messageIn);
                    }
                }
            }

        });
        thread.start();
    }


    /**
     * Получение сообщения ст сервера
     */
    public Message receiptMessage() {
        message = (Message) xStream.fromXML(inputStream);
        if ((message.getFrom().equals("Server"))&& (message.getMessage().equals("#Online")))
            System.out.println("OKK");
            getOnlineUsers();
        xStream.toXML("!OK",outputStream);
        return message;
    }


    /**
     *  Отправка сообщения на сервер
     *  @param message - заданное сообщение
     */

    public synchronized void send(String message) {
        xStream.toXML(message,outputStream);
    }

    /**
     *  Отсоединение пользователя, если нажата кнопка выйти
     */

    public void logOut() throws IOException {
        send("!logout");
        thread.interrupt();
    }

    /**
     *  Отсоединение пользователя, если закрыто приложение
     */

    public void disconnect()  {
        try {
            send("!disconnect");
            thread = null;
            xStream = null;
            socket.close();
        }catch (IOException | StreamException error){
            System.out.println("Ошибка");
        }
    }

    /**
     *  Получение историй сообщений
     */

    public void getHistoryChat()throws IOException{
        send("!HISTORY");
    }

    /**
     *  Авторизация пользователя
     *  @param login - логин пользователя
     *  @param password - пароль пользователя
     */
    public boolean authorization(String login, String password) throws  IOException {
        boolean check = false;
        send("!AUTHORIZE");
        Account account = new Account(login, password);
        xStream.fromXML(inputStream);
        xStream.toXML(account,outputStream);
        Message answer = (Message) xStream.fromXML(inputStream);
        System.out.println(answer.getMessage());
        send("!OK");
        if (answer.getMessage().equals("#Success"))
            check = true;
        else  check = false;
        return check;
    }



    /**
     *  Регистрация пользователя
     *  @param login - логин пользователя
     *  @param password - пароль пользователя
     */

    public String registration(String login, String password) throws IOException {
        String answer = "";
        send("!REGISTRATION");
        Account account = new Account(login, password);
        System.out.println(login + password);
        Message message = (Message) xStream.fromXML(inputStream);
        xStream.toXML(account,outputStream);
        message = (Message) xStream.fromXML(inputStream);
        System.out.println(message.getMessage());
        answer = message.getMessage();
        return answer;
    }

    /**
     *  Получение  списка пользователей онлайн
     */

    public void getListOnline() throws IOException {
        try {
            send("!OK");
            ArrayList<String> message = (ArrayList<String>) xStream.fromXML(inputStream);
            onlineList = message;
        }catch (ClassCastException ex){
            ArrayList<String> message = (ArrayList<String>) xStream.fromXML(inputStream);
            onlineList = message;
        }
    }

    /**
     *  Отправка запроса на получение списка пользователей онлайн
     */

    public void sendOnlineRequest()throws InterruptedException{
        send("!online");
    }

    /**
     *  Получение списка пользователей онлайн
     */
    public ArrayList<String> getOnline(){
        return onlineList;
    }
    /**
     * Вспомагательный метод для получения списка пользователей онлайн
     */
    public void getOnlineUsers(){
        try {
            getListOnline();
        }
        catch (IOException e) {
              }
    }
}



import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.xml.Xpp3Driver;
import Entities.Account;
import Entities.Message;

/**
 * Класс, связывающийся с сервером
 */

public class ServerListenerThread implements ConnectToClient {
    private Socket socket;
    private Thread thread = null;
    ArrayList<String> onlineList = null;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Message messageIn = null;
    private  Message messageIn1=null;
    XStream xStream= new XStream(new Xpp3Driver());


    public ServerListenerThread(Socket socket, OutputStream outputStream, InputStream inputStream) {
        this.socket = socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    /**
     * Вывод данных от сервера
     */

    public void printMessage() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                   while (!thread.isInterrupted()) {
                               messageIn = receiptMessage();
                               if (messageIn.getFrom().equals("Server"))
                                   System.out.println("SERVER : " + messageIn.getMessage());
                               else System.out.println("[ " + messageIn.getFrom() + " ] : " + messageIn.getMessage());
                   }
            }

        });
        thread.start();
    }

    /**
     * Получение сообщения ст сервера
     */

    public Message receiptMessage() {
        messageIn1 = (Message) xStream.fromXML(inputStream);
        if ((messageIn1.getFrom().equals("Server"))&& (messageIn1.getMessage().equals("Online")))
            getOnlineUsers();
        xStream.toXML("!OK",outputStream);
        return messageIn1;
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
        thread.interrupt();//!!!!!!!!!!!!!!!
    }

    /**
     *  Отсоединение пользователя, если закрыто приложение
     */

    public void disconnect() throws IOException {
        send("!disconnect");
        thread.interrupt();//!!!!!!!!!!!!!!!
        socket.close();
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
        send("!authorize");
        Account account = new Account(login, password);
        try {
            thread.sleep(10);
            Message answer = (Message) xStream.fromXML(inputStream);
            xStream.toXML(account,outputStream);
            answer = (Message) xStream.fromXML(inputStream);
            if (answer.getMessage().equals("#Success"))
                check = true;
            else  check = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        try {
            thread.sleep(10);
            Message message = (Message) xStream.fromXML(inputStream);
            xStream.toXML(account,outputStream);
            message = (Message) xStream.fromXML(inputStream);
            if (message.getMessage().equals("#Success"))
                answer = "#Success";
            else if (message.getMessage().equals("#incorrectPassword"))
                answer = "#incorrectPassword";
            else if (message.getMessage().equals("#alreadyRegistered"))
                answer = "#alreadyRegistered";
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return answer;
    }

    /**
     *  Получение  списка пользователей онлайн
     */

    public void getListOnline() throws IOException {
        ArrayList<String> message = (ArrayList<String>) xStream.fromXML(inputStream);
        onlineList = message;
    }

    /**
     *  Получение  списка пользователей онлайн
     */

    public ArrayList<String> getOnlineUser()throws InterruptedException{
        send("!online");
        Thread.sleep(100);
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



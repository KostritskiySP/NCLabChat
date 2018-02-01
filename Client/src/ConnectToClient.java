import Entities.Message;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;

public interface ConnectToClient {
    /**
     * Вывод данных от сервера
     */
    void printMessage();
    /**
     * Получение сообщения ст сервера
     */
    Message receiptMessage();
    /**
     *  Отправка сообщения на сервер
     *  @param message - заданное сообщение
     */
    void send(String message);
    /**
     *  Отсоединение пользователя, если нажата кнопка выйти
     */
    void logOut() throws IOException;
    /**
     *  Отсоединение пользователя, если закрыто приложение
     */
    void disconnect()throws IOException;
    /**
     *  Получение историй сообщений
     */
    void getHistoryChat()throws IOException;
    /**
     *  Авторизация пользователя
     *  @param login - логин пользователя
     *  @param password - пароль пользователя
     */
    boolean authorization(String login,String password) throws IOException;
    /**
     *  Регистрация пользователя
     *  @param login - логин пользователя
     *  @param password - пароль пользователя
     */
    String registration(String login,String password) throws IOException;
    /**
     *  Получение  списка пользователей онлайн
     */
    ArrayList<String> getOnlineUser() throws InterruptedException;


}

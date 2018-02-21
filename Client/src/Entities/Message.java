package Entities;

import java.io.Serializable;
/**
 * Класс, представляющий сообщение пользователя
 */
public class Message implements Serializable {
    private String from;
    private String message;

    public Message(Message message){
        this.message=message.getMessage();
        this.from=message.getFrom();
    }

    public Message(String from, String message){
        this.from = from;
        this.message = message;
    }

    /**
     * Установка сообщения
     * @param message - заданное сообщение
     */

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Получение имени пользователя, от кооторого сообщение
     */

    public String getFrom() {
        return this.from;
    }

    /**
     * Получение сообщения
     */

    public String getMessage() {
        return this.message;
    }


}

package Entities;

import java.io.Serializable;

public class Message implements Serializable {

    private String from;
    private String message;

    public Message(Message message) {
        this.message = message.getMessage();
        this.from = message.getFrom();
    }

    public Message(String from, String message) {
        this.from = from;
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return this.from;
    }

    public String getMessage() {
        return this.message;
    }

}

package Entities;

import com.sun.corba.se.spi.activation.Server;

import java.util.Date;

/**
 * Created by Sergio on 12.01.2018.
 */
public class ServerMessage {
    private Message message;
    private long date;

    public ServerMessage(String from, String message) {
        this.date = new Date().getTime();
        this.message = new Message(from, message);
    }

    public ServerMessage(Message message) {
        this.message = new Message(message);
        this.date = new Date().getTime();
    }

    public ServerMessage(ServerMessage serverMessage) {
        this.date = serverMessage.date;
        this.message = new Message(serverMessage.message);
    }

    public Message getMessage() {
        return message;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        ServerMessage serverMessage = (ServerMessage) obj;
        return (serverMessage.date == this.date &&
                serverMessage.message.getMessage().equals(this.message.getMessage()) &&
                serverMessage.message.getFrom().equals(this.message.getFrom()));
    }
}

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.xml.XppDriver;

import java.io.IOException;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;


public class ClientServiceThread extends Thread implements ServiceMessageSender {


    private Socket socket;
    private Message userMessage;
    private ServerDataControl serverData;
    private XStream xStream;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ServerMessageListener messageListener;
    private ClientDisconnectionListener disconnectionListener;

    public ClientServiceThread(Socket socket) {
        xStream = new XStream(new XppDriver());
        this.socket = socket;
    }

    public void addClientDisconnectionListener(ClientDisconnectionListener sdl) {
        this.disconnectionListener = sdl;
    }

    public void addServerMessageListener(ServerMessageListener sml) {
        this.messageListener = sml;
    }

    public void addServerDataControl(ServerDataControl sd) {
        this.serverData = sd;
    }

    public String getLogin() {
        return userMessage.getFrom();
    }

    private void registration() throws IOException, ClassNotFoundException {
        xStream.toXML(new Message("Server", "#SendAccountInfo"), outputStream);
        Account account = (Account) xStream.fromXML(inputStream);
        String response = serverData.registration(account);
        xStream.toXML(new Message("Server", response), outputStream);
    }

    private void authorization() {
        while (true) {
            String message =(String) xStream.fromXML(inputStream);
            System.out.println(message);
            if (message.toUpperCase().equals("!AUTHORIZE")) {
                xStream.toXML(new Message("Server", "#SendAccountInfo"), outputStream);
                Account account = (Account) xStream.fromXML(inputStream);
                String response = serverData.authorization(account);
                if(response.equals("#Success")){
                            userMessage = new Message(account.login, null);
                            xStream.toXML(new Message("Server", "#Success"), outputStream);
                            System.out.println("Client"+userMessage.getFrom()+"connected successfully");
                            break;
                }
                else xStream.toXML(new Message("Server", "#Success"), outputStream);
            } else if (message.toUpperCase().equals("!REGISTRATION"))
                try {
                    registration();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            else xStream.toXML(new Message("Server", "#authorizeFirst"), outputStream);
        }
    }


    public void run() {
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            authorization();
            System.out.println("Welcome " + userMessage.getFrom());
            for (Message message : serverData.getChatHistory()) {
                xStream.toXML(message, outputStream);
            }
            for (String login : serverData.getOnlineUsers()) {
                System.out.println("Online:" + login);
            }

            while (true) {
                userMessage.setMessage((String)xStream.fromXML(inputStream));
//                userMessage = (Message) xStream.fromXML(inputStream);
                System.out.println("[" + userMessage.getFrom() + "]: " + userMessage.getMessage());
                if (userMessage.getMessage().toUpperCase().equals("!LOGOUT")){
                    userMessage= new Message(null,"");
                    authorization();
                }
                else if (userMessage.getMessage().toUpperCase().equals("!DISCONNECT"))
                    break;
                else {
                    messageListener.broadcast(this.userMessage);
                }

            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println(userMessage.getFrom() + " disconnected!");
            disconnectionListener.clientDisconnected(this, userMessage.getFrom());
        }

    }


    @Override
    public boolean sendMessage(Message message) {
        try {
            xStream.toXML(message, outputStream);
        } catch (XStreamException e) {
            System.out.println(userMessage.getFrom() + " disconnected!");
            disconnectionListener.clientDisconnected(this, userMessage.getFrom());
        }
        return true;
    }
}

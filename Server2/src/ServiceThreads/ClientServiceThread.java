package ServiceThreads;

import Entities.Account;
import Entities.Message;
import Entities.ServerMessage;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.xml.XppDriver;

import java.io.IOException;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;


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

    /**
     * Starts user registration
     */
    private void registration() throws IOException, ClassNotFoundException {
        xStream.toXML(new Message("Server", "#SendAccountInfo"), outputStream);
        Account account = (Account) xStream.fromXML(inputStream);
        String response = serverData.registration(account);
        xStream.toXML(new Message("Server", response), outputStream);
    }

    /**
     * Starts user authorization
     */
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
                            System.out.println("Client "+userMessage.getFrom()+" connected successfully");
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
<<<<<<< HEAD:Server/src/ClientServiceThread.java
<<<<<<< HEAD
//            for (Message message : serverData.getChatHistory()){
//                xStream.toXML(message,outputStream);
//               // outputStream.writeObject(message);
//            }
//            for (String login : serverData.getUserList()) {
//                System.out.println(login);
//            }
            //serverData.addMessage(this.userMessage);
//            serverData.getClientServiceThreads().addUser(login, socket, outputStream, inputStream);

            //System.out.println("[" + this.userMessage.getFrom() + "]: " + this.userMessage.getMessage());
            for (String login : serverData.getUserList()) {
=======
            for (Message message : serverData.getChatHistory()) {
=======
            for (ServerMessage serverMessage : serverData.getChatHistory()) {
                Message message = serverMessage.getMessage();
>>>>>>> Branch_Sergey:Server2/src/ServiceThreads/ClientServiceThread.java
                xStream.toXML(message, outputStream);
            }
            for (String login : serverData.getOnlineUsers()) {
>>>>>>> Branch_Sergey
                System.out.println("Online:" + login);
            }

            while (true) {
                String mes = xStream.fromXML(inputStream).toString();
                System.out.println("[" + userMessage.getFrom() + "]: " + mes);
                if(mes.toUpperCase().equals("!ONLINE")){
                    xStream.toXML(new Message("Server", String.valueOf(serverData.getOnlineUsers().size())), outputStream); //число онлайн пользователей
                    for (String login : serverData.getOnlineUsers()) {
                        xStream.toXML(new Message("Server", login), outputStream);
                    }
                }
                else if(mes.toUpperCase().equals("!HISTORY")){
                    List<ServerMessage> L = serverData.getChatHistory();
                    for (ServerMessage serverMessage : L) {
                        Message message = serverMessage.getMessage();
                        System.out.println("[" + message.getFrom() + "] " + message.getMessage());
                        xStream.toXML(new Message(message), outputStream);
                    }
                }
                else if (mes.toUpperCase().equals("!LOGOUT")){
                    userMessage= new Message(null,"");
                    authorization();
                }
                else if (mes.toUpperCase().equals("!DISCONNECT"))
                    break;
                else {
                    userMessage.setMessage(mes);
                    messageListener.broadcast(new ServerMessage(this.userMessage));
                }

            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println(userMessage.getFrom() + " disconnected!");
            disconnectionListener.clientDisconnected(this);
        }

    }


    @Override
    public boolean sendMessage(ServerMessage serverMessage) {
        try {
            Message message = new Message(serverMessage.getMessage());
            xStream.toXML(message, outputStream);
        } catch (XStreamException e) {
            System.out.println(userMessage.getFrom() + " disconnected!");
            disconnectionListener.clientDisconnected(this);
        }
        return true;
    }
}

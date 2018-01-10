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
    private ServerData serverData;
    private XStream xStream;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ServerMessageListener messageListener;
    private ServerDisconnectionListener disconnectionListener;

    public ClientServiceThread(Socket socket, ServerMessageListener sml, ServerData sd,
                               ServerDisconnectionListener sdl) {
        xStream = new XStream(new XppDriver());
        this.socket = socket;
        this.messageListener = sml;
        this.serverData = sd;
        this.disconnectionListener = sdl;
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
            System.out.println("78787878");
            String message =(String) xStream.fromXML(inputStream);
            System.out.println("88888888888");
            System.out.println(message);
            if (message.toUpperCase().equals("!AUTHORIZE")) {
                xStream.toXML(new Message("Server", "#SendAccountInfo"), outputStream);
                Account account = (Account) xStream.fromXML(inputStream);
                String response = serverData.authorization(account);
                if(response.equals("#Success")){
                            userMessage = new Message(account.login, null);
                            xStream.toXML(new Message("Server", "#Success"), outputStream);
                             System.out.println("sucess");
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
            System.out.println("4444444444");
            authorization();
            System.out.println("11010101010");
            System.out.println("Welcome " + userMessage.getFrom());
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
                xStream.toXML(message, outputStream);
            }
            for (String login : serverData.getOnlineUsers()) {
>>>>>>> Branch_Sergey
                System.out.println("Online:" + login);
            }

            while (true) {
                userMessage.setMessage((String)xStream.fromXML(inputStream));
//                userMessage = (Message) xStream.fromXML(inputStream);
                System.out.println("[" + userMessage.getFrom() + "]: " + userMessage.getMessage());
                if (userMessage.getMessage().equals("!logout")){
                    userMessage= new Message(null,"");
                    authorization();
                }
                else if (userMessage.getMessage().equals("!disconnect"))
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

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
    private String login;
    private ServerData serverData;
    private XStream xStream = new XStream(new XppDriver());
    private InputStream inputStream;
    private OutputStream outputStream;
//    private ObjectInputStream inputStream;
//    private ObjectOutputStream outputStream;
    private ServerMessageListener messageListener;
    private ServerDisconnectionListener disconnectionListener;

    public ClientServiceThread(Socket socket, Server server) {
        this.socket = socket;
        this.messageListener = server;
        this.serverData = server;
        disconnectionListener = server;
    }

    public void registration() throws IOException, ClassNotFoundException {
        String newLogin;
        String newPassword;
        xStream.toXML(new Message(null, "#Enterlogin"),outputStream);
//        outputStream.writeObject(new Message(null, "#Enterlogin"));
        userMessage = (Message)xStream.fromXML(inputStream);
//        userMessage = (Message) inputStream.readObject();
        newLogin = userMessage.getMessage();
        if (!newLogin.equals("") && !serverData.getRegisteredUsers().containsKey(newLogin)) {
            xStream.toXML(new Message(null, "#Enterpassword"),outputStream);
//            outputStream.writeObject(new Message(null, "#Enterpassword"));
            newLogin = userMessage.getMessage();
            userMessage = (Message)xStream.fromXML(inputStream);
//            userMessage = (Message) inputStream.readObject();
            if (!userMessage.getMessage().equals("")) {
                newPassword = userMessage.getMessage();
                serverData.addRegisteredUser(newLogin,newPassword);
                xStream.toXML(new Message(null, "#Success"),outputStream);
//                outputStream.writeObject(new Message(null, "#Success"));
            } else
//                outputStream.writeObject(new Message(null, "#passwordIsEmpty"));
                xStream.toXML(new Message(null, "#passwordIsEmpty"), outputStream);

        } else
//            outputStream.writeObject(new Message(null, "#alreadyRegistered"));
            xStream.toXML(new Message(null, "#alreadyRegistered"),outputStream);

    }

    public void authorization(){
        while (true) {
            userMessage = (Message)xStream.fromXML(inputStream);
            //userMessage = (Message) inputStream.readObject();
            System.out.println(userMessage.getMessage());
            if (userMessage.getMessage().toUpperCase().equals("!AUTHORIZE"))
            {
                String Login;
                String Password;
                xStream.toXML(new Message(null, "#Enterlogin"),outputStream);
//                    outputStream.writeObject(new Message(null, "#Enterlogin"));
                userMessage = (Message)xStream.fromXML(inputStream);
                //userMessage = (Message) inputStream.readObject();
                Login = userMessage.getMessage();
                if (!Login.equals("") || serverData.getRegisteredUsers().containsKey(Login)) {
                    Login = userMessage.getMessage();
                    xStream.toXML(new Message(null, "#Enterpassword"),outputStream);
//                        outputStream.writeObject(new Message(null, "#Enterpassword"));
                    userMessage = (Message)xStream.fromXML(inputStream);
                    //userMessage = (Message) inputStream.readObject();
                    if (!userMessage.getMessage().equals("")) {
                        Password = userMessage.getMessage();
                        if (serverData.getRegisteredUsers().get(Login).equals(Password)) {
                            userMessage = new Message(Login,null);
                            xStream.toXML(new Message(null, "#Success"),outputStream);
//                                outputStream.writeObject(new Message(null, "#Success"));
                            break;
                        } else  xStream.toXML(new Message(null, "#passwordIncorrect"),outputStream);
//                                outputStream.writeObject(new Message(null, "#passwordIncorrect"));
                    }
                } else xStream.toXML(new Message(null, "#notRegistered"),outputStream);
//                        outputStream.writeObject(new Message(null, "#notRegistered"));
            } else if (userMessage.getMessage().toUpperCase().equals("!REGISTRATION"))
                try {
                    registration();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            else xStream.toXML(new Message(null, "#authorizeFirst"), outputStream);
//                    outputStream.writeObject(new Message(null, "#authorizeFirst"));
        }
    }

    public void run() {
        try {
//            inputStream = xStream.createObjectInputStream(this.socket.getInputStream());
//            outputStream = xStream.createObjectOutputStream(this.socket.getOutputStream());
            inputStream=socket.getInputStream();
            outputStream=socket.getOutputStream();
            authorization();
            System.out.println("Welcome " + userMessage.getFrom());
            for (Message message : serverData.getChatHistory()){
                xStream.toXML(message,outputStream);
               // outputStream.writeObject(message);
            }
//            for (String login : serverData.getUserList()) {
//                System.out.println(login);
//            }
            //serverData.addMessage(this.userMessage);
//            serverData.getClientServiceThreads().addUser(login, socket, outputStream, inputStream);

            //System.out.println("[" + this.userMessage.getFrom() + "]: " + this.userMessage.getMessage());
            for (String login : serverData.getUserList()) {
                System.out.println("Online:" + login);
            }

            while (true) {
                userMessage = (Message)xStream.fromXML(inputStream);
                //this.userMessage = (Message) inputStream.readObject();
                System.out.println("[" + userMessage.getFrom() + "]: " + userMessage.getMessage());
                serverData.addMessage(this.userMessage);
                // this.userMessage.setOnlineUsers(serverData.getClientServiceThreads().getUsers());

                messageListener.broadcast(this.userMessage);

            }

        } catch (SocketException e) {
            //this.userMessage.setOnlineUsers(serverData.getClientServiceThreads().getUsers());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            System.out.println(userMessage.getFrom() + " disconnected!");
            disconnectionListener.clientDisconnected(this,userMessage.getFrom());
        }

    }

    @Override
    public boolean sendMessage(Message message) {
        try {
            xStream.toXML(message,outputStream);
        } catch (XStreamException e) {
            System.out.println(userMessage.getFrom() + " disconnected!");
            disconnectionListener.clientDisconnected(this,userMessage.getFrom());
        }
        return true;
    }
}

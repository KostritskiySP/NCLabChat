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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class ClientServiceThread extends Thread implements ServiceMessageSender {


    private Socket socket;
    private Message userMessage;
    private ServerDataControl serverData;
    private XStream xStream;
    private BufferedInputStream inputStream;
    private BufferedOutputStream outputStream;
    private ServerMessageListener messageListener;
    private ClientDisconnectionListener disconnectionListener;
    private String stateResponse;
    private boolean isActive;
    private ReentrantLock senderLock = new ReentrantLock();
    //    private boolean readyToSend;
    private ReentrantLock readyToSendLock = new ReentrantLock();

    public ClientServiceThread(Socket socket) {
        xStream = new XStream(new XppDriver());
        this.socket = socket;
        isActive = true;
        stateResponse = "";
        userMessage = new Message(null,null);
    }

    public void disable() {
        isActive = false;
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
        while (isActive) {
            String message = (String) xStream.fromXML(inputStream);
            System.out.println(message);
            if (message.toUpperCase().equals("!AUTHORIZE")) {
                xStream.toXML(new Message("Server", "#SendAccountInfo"), outputStream);
                Account account = (Account) xStream.fromXML(inputStream);
                String response = serverData.authorization(account);
                if (response.equals("#Success")) {
                    userMessage = new Message(account.login, null);
                    xStream.toXML(new Message("Server", "#Success"), outputStream);
                    stateResponse = xStream.fromXML(inputStream).toString();
                    System.out.println("Client " + userMessage.getFrom() + " connected successfully");

                    break;
                } else {
                    xStream.toXML(new Message("Server", response), outputStream);
                    stateResponse = xStream.fromXML(inputStream).toString();
                }

            } else if (message.toUpperCase().equals("!REGISTRATION"))
                try {
                    registration();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            else {
                xStream.toXML(new Message("Server", "#authorizeFirst"), outputStream);
                stateResponse = xStream.fromXML(inputStream).toString();
            }
        }
    }


    public void run() {
        try {
//            readyToSend = false;
            readyToSendLock.lock();
            inputStream = new BufferedInputStream(socket.getInputStream());
            outputStream = new BufferedOutputStream(socket.getOutputStream());
       //     xStream.toXML(new Message("Notification", "#Welcome to server! Please authorize or register."), outputStream);
//            stateResponse = xStream.fromXML(inputStream).toString();
            authorization();
            if (isActive) {
                System.out.println("Welcome " + userMessage.getFrom());
                for (ServerMessage serverMessage : serverData.getChatHistory()) {
                    Message message = serverMessage.getMessage();
                    xStream.toXML(message, outputStream);
                    stateResponse = xStream.fromXML(inputStream).toString();
                }
//                for (String login : serverData.getOnlineUsers()) {
//                    System.out.println("Online:" + login);
//                }
                messageListener.broadcast(new ServerMessage("SERVER","connected "+userMessage.getFrom()));
                readyToSendLock.unlock();
//                readyToSend = true;
                while (isActive) {
                    if (inputStream.available() != 0) {
                        String mes = xStream.fromXML(inputStream).toString();
                        messageProcessing(mes);
                    }
                }
            }
        } catch (XStreamException e) {

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            disconnectionListener.clientDisconnected(this);
            System.out.println(userMessage.getFrom() + " disconnected!");
        }

    }

    public void messageProcessing(String mes) {
        if (mes.toUpperCase().equals("!OK")) {
            readyToSendLock.lock();
            readyToSendLock.unlock();
        }
//            readyToSend = true;
        else if (mes.toUpperCase().equals("!ONLINE")) {
            xStream.toXML(new Message("Server", "#Online"), outputStream);
            stateResponse = xStream.fromXML(inputStream).toString();
            xStream.toXML(serverData.getOnlineUsers(), outputStream);
            stateResponse = xStream.fromXML(inputStream).toString();
//            for (String login : serverData.getOnlineUsers()) {
//                xStream.toXML(new Message("Server", login), outputStream);
//                stateResponse = xStream.fromXML(inputStream).toString();
//            }
        } else if (mes.toUpperCase().equals("!HISTORY")) {
            List<ServerMessage> L = serverData.getChatHistory();
            for (ServerMessage serverMessage : L) {
                Message message = serverMessage.getMessage();
                System.out.println("[" + message.getFrom() + "] " + message.getMessage());
                xStream.toXML(new Message(message), outputStream);
                stateResponse = xStream.fromXML(inputStream).toString();
            }
        } else if (mes.toUpperCase().equals("!LOGOUT")) {
            readyToSendLock.lock();
//            readyToSend = false;
            userMessage = new Message(null, "");
            authorization();
        } else if (mes.toUpperCase().equals("!DISCONNECT"))
            isActive = false;
        else {
            userMessage.setMessage(mes);
            messageListener.broadcast(new ServerMessage(this.userMessage));
        }
    }


    @Override
    public boolean sendMessage(ServerMessage serverMessage) {
        senderLock.lock();
        try {
//            while (!readyToSend) {
//            }
//            readyToSend = false;
            readyToSendLock.lock();
            Message message = new Message(serverMessage.getMessage());
            xStream.toXML(message, outputStream);
            outputStream.flush();
        } catch (XStreamException e) {
            System.out.println(userMessage.getFrom() + " disconnected!");
            disconnectionListener.clientDisconnected(this);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            readyToSendLock.unlock();
            senderLock.unlock();
        }
        return true;
    }
}

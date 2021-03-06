package ServiceThreads;

import Entities.ServerMessage;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.xml.XppDriver;

import java.io.*;
import java.net.Socket;

/**
 * Created by Sergio on 11.01.2018.
 */
public class ServerNetServiceThread extends Thread implements ServiceMessageSender {
    private Socket socket;
    private XStream xStream;
    private Reader inputStream;
    private BufferedWriter outputStream;
    private ServerMessageListener messageListener;
    private ServersDisconnectionListener serversDisconnectionListener;
    private NetClientListener netClientListener;
    private RegistrationListener registrationListener;
    private boolean isActive;

//    private ServerDataControl serverData;

    public ServerNetServiceThread(Socket socket) {
        xStream = new XStream(new XppDriver());
        this.socket = socket;
        isActive = true;
    }

    public void addServerMessageListener(ServerMessageListener sml) {
        this.messageListener = sml;
    }

    public void addServersDisconnectionListener(ServersDisconnectionListener sdl) {
        this.serversDisconnectionListener = sdl;
    }

    public void addNetClientListener(NetClientListener ncdl) {
        this.netClientListener = ncdl;
    }

    public void addRegistrationListener(RegistrationListener rl){this.registrationListener = rl;}


    public void run() {
        try {
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new BufferedWriter(new PrintWriter(socket.getOutputStream()));
            while (isActive) {
                ServerMessage mes = (ServerMessage) xStream.fromXML(inputStream);
                if (messageListener.broadcast(mes)) {
                    if (mes.getMessage().getFrom().toUpperCase().equals("SERVER")) {         //check if command
                        String command = mes.getMessage().getMessage();
                        if(command.startsWith("REGISTRATION:")){
                            command = command.substring(13,command.length());
                            String[] accData = command.split(":PASSWORD:");
                            registrationListener.addRegisteredUser(accData[0],accData[1]);
                        }
                        if(command.startsWith("disconnected")) {     //check for disconnection message
                            String login = command.substring(13, command.length());
                            netClientListener.netClientDisconnected(login);
                            System.out.println(command);
                        }
                        if (command.startsWith("connected")) {
                            String login = command.substring(10, command.length());
                            netClientListener.netClientConnected(login);
                            System.out.println(command);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XStreamException e) {
            //e.printStackTrace();
        } finally {
            System.out.println("Server disconnected!");
            try {
                socket.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }


    @Override
    public boolean sendMessage(ServerMessage message) {
        try {
            outputStream.flush();
            xStream.toXML(message, outputStream);
        } catch (XStreamException e) {
            System.out.println("Server disconnected!");
            serversDisconnectionListener.serverDisconnected(this);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void disable() {
        isActive = false;
    }
}

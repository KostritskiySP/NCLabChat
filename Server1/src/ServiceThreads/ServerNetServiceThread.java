package ServiceThreads;

import Entities.Message;
import Entities.ServerMessage;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.xml.XppDriver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Sergio on 11.01.2018.
 */
public class ServerNetServiceThread extends Thread implements ServiceMessageSender {
    private Socket socket;
    private XStream xStream;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ServerMessageListener messageListener;
    private ServersDisconnectionListener serversDisconnectionListener;

    public ServerNetServiceThread(Socket socket) {
        xStream = new XStream(new XppDriver());
        this.socket = socket;
    }

    public void addServerMessageListener(ServerMessageListener sml) {
        this.messageListener = sml;
    }

    public void addServersDisconnectionListener(ServersDisconnectionListener sdl) {
        this.serversDisconnectionListener = sdl;
    }


    public void run() {
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            while (true) {
                ServerMessage mes = (ServerMessage) xStream.fromXML(inputStream);
                messageListener.broadcast(mes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean sendMessage(ServerMessage message) {
        try {
            xStream.toXML(message, outputStream);
        } catch (XStreamException e) {
            System.out.println("Server disconnected!");
            serversDisconnectionListener.serverDisconnected(this);
        }
        return true;
    }

}

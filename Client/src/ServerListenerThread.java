import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class ServerListenerThread {
    private Socket socket;
    private Thread thread = null;
    private  InputStream inputStream;
    private  OutputStream outputStream;
    private Message messageIn = null;
    public String nickName;
    XStream xStream = new XStream(new XppDriver());
    ConnectionListener listener;

    public ServerListenerThread(Socket socket, OutputStream outputStream, InputStream inputStream) {
        this.socket = socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    messageIn = (Message) xStream.fromXML(inputStream);
                    System.out.println("[ " + messageIn.getFrom() + " ] : " + messageIn.getMessage());
                }

            }
        });
        thread.start();
    }

    public synchronized void send(String message) {
        xStream.toXML(message,outputStream);

    }

    public synchronized void disconnect() {
        thread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            listener.onException(this,e);
        }
    }

    public boolean autoriz(String login,String password) throws IOException {
        boolean f=false;
        String  message =  "!authorize";
        xStream.toXML(message,outputStream);
        Account account=new Account(login,password);
        Message answer=(Message) xStream.fromXML(inputStream);
        xStream.toXML(account,outputStream);
        answer=(Message) xStream.fromXML(inputStream);
        if(answer.getMessage().equals("#Success"))
            return f=true;
        else return f=false;
    }

    public boolean registr(String login,String password) throws IOException {
        boolean f=false;
        String message ="!registration";
        xStream.toXML(message,outputStream);
        Account account=new Account(login,password);
        Message answer=(Message) xStream.fromXML(inputStream);
        xStream.toXML(account,outputStream);
        answer=(Message) xStream.fromXML(inputStream);
        if(answer.getMessage().equals("#Success"))
            return f=true;
        else return f=false;
    }
    public void zaprosOnline() throws IOException {
        String  message = "!online";
        xStream.toXML(message,outputStream);
    }
    public ArrayList<String> listOnline() throws IOException {
        ArrayList<String> onlineList = null;
        onlineList = (ArrayList<String>) xStream.fromXML(inputStream);
        return onlineList;
    }
}
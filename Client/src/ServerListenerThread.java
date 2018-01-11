import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class ServerListenerThread implements ConnectToClient{
    private Socket socket;
    private Thread thread = null;
    private  InputStream inputStream;
    private  OutputStream outputStream;
    private Message messageIn = null;
    String message="";
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
                    message="[ " + messageIn.getFrom() + " ] : " + messageIn.getMessage();
                }

            }
        });
        thread.start();
    }

    public synchronized void send(String message) {
        xStream.toXML(message,outputStream);

    }

    public synchronized void disconnect() {
        xStream.toXML("!LOGOUT",outputStream);
        thread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            listener.onException(this,e);
        }
    }

    public boolean authorization(String login,String password) throws IOException {
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

    public boolean registration(String login,String password) throws IOException {
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
        zaprosOnline();
        ArrayList<String> onlineList = null;
        int number=(Integer) xStream.fromXML(inputStream);
        for(int i=0;i<number;i++){
            Message answer=(Message) xStream.fromXML(inputStream);
            onlineList.add(answer.getFrom());
        }
        return onlineList;
    }
    public String getMessage() throws IOException{
        return message;
    }
}
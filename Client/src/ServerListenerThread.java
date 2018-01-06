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
//    private ObjectOutputStream objectOutputStream = null;
//    private ObjectInputStream objectInputStream = null;
    private  InputStream inputStream;
    private  OutputStream outputStream;
    private Message messageIn = null;
    public String nickName;
    XStream xStream = new XStream(new XppDriver());
    ConnectionListener listener;

    public ServerListenerThread(Socket socket, OutputStream outputStream, InputStream inputStream) {
        this.socket = socket;
//        this.objectOutputStream = objectOutputStream;
//        this.objectInputStream = objectInputStream;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        //  in =new XStream(new DomDriver());
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // listener.onConnectionReady(ServerListenerThread.this);
                while (true) {
                    messageIn = (Message) xStream.fromXML(inputStream);
                    // messageIn= (Message)in.fromXML(objectInputStream);
                   // listener.onReceiveMessage(ServerListenerThread.this,messageIn);                        
                    System.out.println("[ " + messageIn.getFrom() + " ] : " + messageIn.getMessage());
                }

            }
        });
        thread.start();
    }

    public synchronized void send(Message message) {
        //  xs =  new XStream()     ;
        // /FileOutputStream fs = new FileOutputStream("C:\\Users\\Анастасия\\IdeaProjects\\Chat\\employeedata.txt");
        //   xs.toXML(message,objectOutputStream);
        xStream.toXML(message,outputStream);
//            objectOutputStream.writeObject(message);
//            objectOutputStream.flush();
    }

    public synchronized void disconnect() {
        thread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            listener.onException(this,e);
        }
    }

    public void autoriz() throws IOException {
        Message message = new Message(null, "!authorize");
        xStream.toXML(message,outputStream);
//        objectOutputStream.writeObject(message);
//        objectOutputStream.flush();
    }

    public void registr() throws IOException {
        Message message = new Message(null, "!registration");
        xStream.toXML(message,outputStream);
//        objectOutputStream.writeObject(message);
    }

    public void zaprosOnline() throws IOException {

        Message message = new Message(null, "!online");
        xStream.toXML(message,outputStream);
//        objectOutputStream.writeObject(message);


    }

    public ArrayList<String> listOnline() throws IOException {
        ArrayList<String> onlineList = null;

//            onlineList = (ArrayList<String>) objectInputStream.readObject();
        onlineList = (ArrayList<String>) xStream.fromXML(inputStream);

        return onlineList;
    }
}
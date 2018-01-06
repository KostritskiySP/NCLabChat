import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
//import com.thoughtworks.xstream.*;
//import com.thoughtworks.xstream.io.xml.DomDriver;

public class ServerListenerThread {
    private Socket socket;
    private Thread thread = null;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;
    private Message messageIn = null;
    private ConnectionListener listener;

    public ServerListenerThread(ConnectionListener listener, String host, int port) throws IOException {
        this(listener, new Socket(host, port));
    }

    public ServerListenerThread(ConnectionListener listener,Socket socket) {
        this.socket = socket;
        this.listener = listener;
        try {
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println(e);
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        messageIn = (Message) objectInputStream.readObject();
                        listener.onReceiveMessage(ServerListenerThread.this, messageIn);
                    }
                } catch (ClassNotFoundException e) {
                } catch (IOException e) {
                    listener.onException(ServerListenerThread.this, e);
                }
            }
        });
        thread.start();
    }

    public synchronized void send(Message message) {
        //  xs =  new XStream()     ;
        // /FileOutputStream fs = new FileOutputStream("C:\\Users\\Анастасия\\IdeaProjects\\Chat\\employeedata.txt");
        //   xs.toXML(message,objectOutputStream);
        try {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        } catch (IOException e) {
            listener.onException(this, e);
            disconnect();
        }
    }

    public synchronized void disconnect() {
        thread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            listener.onException(this, e);
        }
    }

    public void autoriz() throws IOException {
        Message message = new Message(null, "!authorize");
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();
    }

    public void registr() throws IOException {
        Message message = new Message(null, "!registration");
        objectOutputStream.writeObject(message);
    }

    public void zaprosOnline() throws IOException {

        Message message = new Message(null, "!online");
        objectOutputStream.writeObject(message);


    }

    public ArrayList<String> listOnline() throws IOException {
        ArrayList<String> onlineList = null;
        try {
            onlineList = (ArrayList<String>) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            e.getMessage();
        }
        return onlineList;
    }
}

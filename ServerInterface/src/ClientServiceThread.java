//import com.thoughtworks.xstream.XStream;
//import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.IOException;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;


public class ClientServiceThread extends Thread implements Client2{


    private Socket socket;
    private Message userMessage;
    private String login;
    private UsersData usersData;
  //  XStream in;
 //   XStream out ;
    public ClientServiceThread(Socket socket, UsersData usersData) {
        this.socket = socket;
        this.usersData = usersData;
        this.start();
    }

    public void run() {
        try {
            ObjectInputStream inputStream   = new ObjectInputStream(this.socket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(this.socket.getOutputStream());
         //   in =new XStream(new DomDriver());
          //  out=new XStream();
            this.userMessage = (Message) inputStream.readObject();
           // this.userMessage = (Message) in.fromXML(inputStream);
            this.login = this.userMessage.getFrom();

            for(Message message : usersData.getChatHistory().getHistory()){
               outputStream.writeObject(message);
             //  out.toXML(message,outputStream);
            }
            usersData.addMessage(this.userMessage);
            usersData.getUserList().addUser(login, socket, outputStream, inputStream);

            System.out.println("[" + this.userMessage.getFrom() + "]: " + this.userMessage.getMessage());


            this.broadcast(usersData.getUserList().getClientsList(), this.userMessage);

            while (true) {

                this.userMessage = (Message) inputStream.readObject();
                // this.userMessage = (Message) in.fromXML(inputStream);
                System.out.println("[" + login + "]: " + userMessage.getMessage());
                usersData.getChatHistory().addMessage(this.userMessage);


                this.broadcast(usersData.getUserList().getClientsList(), this.userMessage);

            }

        } catch (SocketException e) {
            System.out.println(login + " disconnected!");
            usersData.getUserList().deleteUser(login);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    public void broadcast(ArrayList<Client> clientsArrayList, Message message) throws IOException {
            for (Client client : clientsArrayList) {
                client.getThisObjectOutputStream().writeObject(message);
            }
    }

    @Override
    public boolean sendMessage(Message message) {
        return false;
    }
}

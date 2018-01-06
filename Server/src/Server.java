import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Server implements ServerData, ServerMessageListener,ServerDisconnectionListener {

    private ArrayList<String> loginList = new ArrayList<String>();
    private ArrayList<ClientServiceThread> clientServiceThreads = new ArrayList<ClientServiceThread>();
    private ArrayList<Message> chatHistory = new ArrayList<>();
    private HashMap<String,String> registeredUsers = new HashMap<>();
//    private ArrayList<Server> connectedServers = new ArrayList<>();
    private HashMap<Integer,String> serversNetList = new HashMap<Integer,String>();
    private ArrayList<Socket> serverNetSockets = new ArrayList<>();
    private XStream xStream = new XStream(new DomDriver());

    public static void main(String[] args) {
        try {
            Server server = new Server();
//            HashMap<Integer,String> serverNet = new HashMap<>();
//            serverNet.put(6667,"localhost");
//            serverNet.put(6668,"localhost");
//            serverNet.put(6669,"localhost");
//            server.xStream.toXML(serverNet,new FileOutputStream("serversNet"));
//            server.registeredUsers.put("1","1");
//            server.registeredUsers.put("2","1");
//            server.registeredUsers.put("3","1");
//            server.registeredUsers.put("4","1");
//            server.registeredUsers.put("Vasya","1");
//            server.registeredUsers.put("Petya","1");
//            server.registeredUsers.put("Kolya","1");
//            server.xStream.toXML(server.registeredUsers,new FileOutputStream("registered"));
//            ArrayList<Message> AL=new ArrayList<Message>();
//            AL.add(new Message("111","1123123"));
//            server.xStream.toXML(AL,new FileOutputStream(Config.CHAT_HISTORY_FILE,true));
            server.chatHistory = (ArrayList<Message>) server.xStream.fromXML(new FileInputStream(Config.CHAT_HISTORY_FILE));
            server.registeredUsers = (HashMap<String,String>) server.xStream.fromXML(new FileInputStream(Config.REGISTERED_USERS_FILE));
            ServerSocket socketListener = new ServerSocket(Config.PORT);
            System.out.println("\nWaiting for a client...");
            for(Map.Entry nearbyServer:server.serversNetList.entrySet()){
                try {
                    Socket socket = new Socket(nearbyServer.getValue().toString(),(int)nearbyServer.getKey());
                    server.serverNetSockets.add(socket);
                } catch (IOException e) {
                }
            }
            while (true) {
//                Socket client = null;
//                while (client == null) {
                Socket client = socketListener.accept();
//                }
//                if(server.serversNetList.containsValue(client.getInetAddress()))
                    //Запуск серверов
                ClientServiceThread clientService =new ClientServiceThread(client, server);
//                clientService.addClientDisconnectionListner(server implements ClientDisconnectionListner);
//                clientService.addOnMessageListner(server implements ClientDisconnectionListner);
                server.clientServiceThreads.add(clientService);
                clientService.start();
            }
        } catch (SocketException e) {
            System.err.println("Socket exception");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("I/O exception");
            e.printStackTrace();
        }
    }
//
//    @Override
//    public ArrayList<ClientServiceThread> getClientServiceThreads() {
//        return clientServiceThreads;
//    }

    @Override
    public ArrayList<Message> getChatHistory() {
        if(chatHistory.size()>=5)
            return (ArrayList<Message>) chatHistory.subList(chatHistory.size()-5,chatHistory.size()-1);
        else return chatHistory;
    }

    @Override
    public boolean addMessage(Message message) {
        boolean b=chatHistory.add(message);
        try {
            xStream.toXML(chatHistory,new FileOutputStream(Config.CHAT_HISTORY_FILE,false));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return b;
    }

    @Override
    public boolean addLogin(String login) {
        if(loginList.add(login))
            return true;
        return  false;
    }

    @Override
    public ArrayList<String> getUserList() {
        return loginList;
    }

    @Override
    public HashMap<String, String> getRegisteredUsers() {
        return registeredUsers;
    }


    @Override
    public void addRegisteredUser(String login,String password) {
        registeredUsers.put(login,password);
    }

    @Override
    public void broadcast(Message message) {
        for(Socket socket: serverNetSockets){
            try {
                xStream.toXML(message,socket.getOutputStream());
            } catch (IOException e) {
                Socket socketTemp;
                Iterator<Socket> it = serverNetSockets.iterator();
                while (it.hasNext()) {
                    socketTemp = it.next();
                    if (socketTemp == socket)
                        it.remove();
                }
            }
        }
        for(ClientServiceThread thread : clientServiceThreads) thread.sendMessage(message);
    }

    @Override
    public void clientDisconnected(ClientServiceThread clientServiceThread, String login) {
//        String tempLogin;
        loginList.remove(login);
//        Iterator<String> it = loginList.iterator();
//        while (it.hasNext()) {
//            tempLogin = it.next();
//            if (login.equals(tempLogin))
//                it.remove();
//        }
        clientServiceThreads.remove(clientServiceThread);
//        ClientServiceThread thread;
//        Iterator<ClientServiceThread> it2 = clientServiceThreads.iterator();
//        while (it2.hasNext()) {
//            thread = it2.next();
//            if (thread == clientServiceThread)
//                it2.remove();
//        }
    }
}

import Entities.*;
import ServiceThreads.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XppDriver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class Server implements ServerDataControl, ServerMessageListener, ClientDisconnectionListener,ServersDisconnectionListener {

    private ArrayList<ClientServiceThread> clientServiceThreads = new ArrayList<ClientServiceThread>();
    private ArrayList<ServerNetServiceThread> serverNetServiceThreads = new ArrayList<ServerNetServiceThread>();
    private ArrayList<ServerMessage> chatHistory = new ArrayList<>();
    private HashMap<String, String> registeredUsers = new HashMap<>();
    //private ArrayList<Server> connectedServers = new ArrayList<>();
    private LinkedHashSet<ServerPortIP> serversNetList = new LinkedHashSet();
    //private ArrayList<Socket> serverNetSockets = new ArrayList<>();
    private XStream xStream = new XStream(new XppDriver());

    public static void main(String[] args) {
        try {
            Server server = new Server();

//            LinkedHashSet<ServerPortIP> serverNet = new LinkedHashSet<>();
//            serverNet.add(new ServerPortIP(6667,"127.0.0.1"));
//            serverNet.add(new ServerPortIP(6668,"127.0.0.1"));
//            serverNet.add(new ServerPortIP(6669,"127.0.0.1"));
//
//            server.xStream.toXML(serverNet,new FileOutputStream("serversNet"));

//            server.registeredUsers.put("1","1");
//            server.registeredUsers.put("2","1");
//            server.registeredUsers.put("3","1");
//            server.registeredUsers.put("4","1");
//            server.registeredUsers.put("Vasya","1");
//            server.registeredUsers.put("Petya","1");
//            server.registeredUsers.put("Kolya","1");
//            server.xStream.toXML(server.registeredUsers,new FileOutputStream("registered"));
//            ArrayList<Message> AL=new ArrayList<Entities.Message>();
//            AL.add(new Message("111","1123123"));
//            server.xStream.toXML(server.serversNetList,new FileOutputStream(Entities.Config.SERVERS_NET_FILE,false));

            if (new File(Config.CHAT_HISTORY_FILE).exists())
                try {
                    server.chatHistory = (ArrayList<ServerMessage>) server.xStream.fromXML(new FileInputStream(Config.CHAT_HISTORY_FILE));
                } catch (Exception e) {
                }
            else new File(Config.CHAT_HISTORY_FILE).createNewFile();

            if (new File(Config.REGISTERED_USERS_FILE).exists())
                server.registeredUsers = (HashMap<String, String>) server.xStream.fromXML(new FileInputStream(Config.REGISTERED_USERS_FILE));
            else new File(Config.REGISTERED_USERS_FILE).createNewFile();

            if (new File(Config.SERVERS_NET_FILE).exists())
                server.serversNetList = (LinkedHashSet<ServerPortIP>) server.xStream.fromXML(new FileInputStream(Config.SERVERS_NET_FILE));
            else new File(Config.SERVERS_NET_FILE).createNewFile();

            ServerSocket socketListener = new ServerSocket(Config.PORT);
            System.out.println("\nWaiting for a client...");
            for (ServerPortIP nearbyServer : server.serversNetList) {
                try {
                    Socket socket = new Socket(nearbyServer.ip, nearbyServer.port);
                    ServerNetServiceThread serverNetService = new ServerNetServiceThread(socket);
                    server.serverNetServiceThreads.add(serverNetService);
                    serverNetService.addServerMessageListener(server);
                    serverNetService.addServersDisconnectionListener(server);
                    server.xStream.toXML("SERVER", socket.getOutputStream());
                    System.out.println("connected to another Server");
                    serverNetService.start();
                } catch (IOException e) {
                }
            }

            while (true) {
                Socket client = socketListener.accept();
                boolean isServer = false;
                String type = server.xStream.fromXML(client.getInputStream()).toString(); //тип - сервер или клиент
                if(type.toUpperCase().equals("SERVER")) isServer=true;
//                for (ServerPortIP entry : server.serversNetList) {
//                    if (entry.ip.equals(client.getInetAddress().toString())) {
//                        isServer = true;
//                        break;
//                    }
//                }
                if (isServer) {
                    ServerNetServiceThread serverNetService = new ServerNetServiceThread(client);
                    server.serverNetServiceThreads.add(serverNetService);
                    serverNetService.addServerMessageListener(server);
                    serverNetService.addServersDisconnectionListener(server);
                    System.out.println("Server connected");
                    serverNetService.start();
                } else {
                    ClientServiceThread clientService = new ClientServiceThread(client);
                    clientService.addClientDisconnectionListener(server);
                    clientService.addServerMessageListener(server);
                    clientService.addServerDataControl(server);
                    server.clientServiceThreads.add(clientService);
                    System.out.println("Client connected");
                    clientService.start();
                }
            }
        } catch (SocketException e) {
            System.err.println("Socket exception");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("I/O exception");
            e.printStackTrace();
        }
    }

    @Override
    public List<ServerMessage> getChatHistory() {
        if (chatHistory.size() >= 5){
            return chatHistory.subList(chatHistory.size() - 5, chatHistory.size());
        }
        else return chatHistory;
    }

    public boolean addMessage(ServerMessage message) {
        boolean b = chatHistory.add(new ServerMessage(message));
        try {
            FileOutputStream fos = new FileOutputStream(Config.CHAT_HISTORY_FILE,false);
            xStream.toXML(chatHistory, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }


    @Override
    public ArrayList<String> getOnlineUsers() {
        ArrayList<String> loginList = new ArrayList<>();
        for (ClientServiceThread clientThread : clientServiceThreads) {
            if (clientThread.getLogin() != null)
                loginList.add(clientThread.getLogin());
        }
        return loginList;
    }


    public void addRegisteredUser(String login, String password) {
        registeredUsers.put(login, password);
        try {
            xStream.toXML(registeredUsers, new FileOutputStream(Config.REGISTERED_USERS_FILE, false));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String registration(Account account) {
        if (!account.login.equals("") && !registeredUsers.containsKey(account.login)) {
            if (!account.password.equals("")) {
                addRegisteredUser(account.login, account.password);
                return  "#Success";
            } else
                return "#incorrectPassword";

        } else
            return "#alreadyRegistered";
    }

    @Override
    public String authorization(Account account) {
        if (!account.login.equals("") || registeredUsers.containsKey(account.login)) {
            if (!account.password.equals("")) {
                if (registeredUsers.get(account.login).equals(account.login)) {
                    return  "#Success";
                } else return  "#passwordIncorrect";
            }
            else return  "#emptyPassword";
        } else return  "#notRegistered";
    }


    @Override
    public void broadcast(ServerMessage message) {
        if (!chatHistory.contains(message)) {
            addMessage(message);
            System.out.println("[" + message.getMessage().getFrom() + "]: " + message.getMessage().getMessage());
            for (ServiceMessageSender thread : clientServiceThreads) thread.sendMessage(message);
            for (ServiceMessageSender thread : serverNetServiceThreads) thread.sendMessage(message);
        }
    }

    @Override
    public void clientDisconnected(ClientServiceThread clientServiceThread) {
        clientServiceThreads.remove(clientServiceThread);
    }

    @Override
    public void serverDisconnected(ServerNetServiceThread serverNetServiceThread) {
        serverNetServiceThreads.remove(serverNetServiceThread);
    }

}
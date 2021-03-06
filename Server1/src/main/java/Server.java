import Entities.*;
import ServiceThreads.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.io.xml.XppDriver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class Server implements ServerDataControl, ServerMessageListener, ClientDisconnectionListener,
        ServersDisconnectionListener, NetClientListener, ServerManagementController, RegistrationListener {
    private ArrayList<ClientServiceThread> clientServiceThreads = new ArrayList<ClientServiceThread>();
    private ArrayList<ServerNetServiceThread> serverNetServiceThreads = new ArrayList<ServerNetServiceThread>();
    private ArrayList<ServerMessage> chatHistory = new ArrayList<>();
    private ArrayList<ServerMessage> serverNotificationsHistory = new ArrayList<>();
    private HashMap<String, String> registeredUsers = new HashMap<>();
    private LinkedHashSet<ServerPortIP> serversNetList = new LinkedHashSet();
    private XStream xStream = new XStream(new StaxDriver());
    private LinkedHashSet<String> usersOnlineInNet;

    public Server() {
        ServerManagementThread serverManagement = new ServerManagementThread();
        serverManagement.addServerManagementController(this);
        serverManagement.start();
    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            System.out.println(System.getProperty("user.dir"));
            server.usersOnlineInNet = new LinkedHashSet<>();
            if (new File(Config.CHAT_HISTORY_FILE).exists())
                try {
                    server.chatHistory = (ArrayList<ServerMessage>) server.xStream.fromXML(new FileInputStream(Config.CHAT_HISTORY_FILE));
                } catch (Exception e) {
                }
            else {
                new File(Config.CHAT_HISTORY_FILE).createNewFile();
                server.chatHistory = new ArrayList<>();
                FileOutputStream fos = new FileOutputStream(Config.CHAT_HISTORY_FILE, false);
                server.xStream.toXML(server.chatHistory, fos);
                fos.close();
            }


            if (new File(Config.REGISTERED_USERS_FILE).exists())
                server.registeredUsers = (HashMap<String, String>) server.xStream.fromXML(new FileInputStream(Config.REGISTERED_USERS_FILE));
            else {
                new File(Config.REGISTERED_USERS_FILE).createNewFile();
                server.registeredUsers = new HashMap<>();
                FileOutputStream fos = new FileOutputStream(Config.REGISTERED_USERS_FILE, false);
                server.xStream.toXML(server.registeredUsers, fos);
                fos.close();
            }

            if (new File(Config.SERVERS_NET_FILE).exists())
                server.serversNetList = (LinkedHashSet<ServerPortIP>) server.xStream.fromXML(new FileInputStream(Config.SERVERS_NET_FILE));
            else {
                server.serversNetList = new LinkedHashSet<>();
                FileOutputStream fos = new FileOutputStream(Config.SERVERS_NET_FILE, false);
                server.xStream.toXML(server.serversNetList, fos);
                fos.close();
            }

            ServerSocket socketListener = new ServerSocket(Config.PORT);
            System.out.println("\nServer started on port "+Config.PORT);
            System.out.println("\nWaiting for a client...");
            for (ServerPortIP nearbyServer : server.serversNetList) {
                try {
                    Socket socket = new Socket(nearbyServer.ip, nearbyServer.port);
                    ServerNetServiceThread serverNetService = new ServerNetServiceThread(socket);
                    server.serverNetServiceThreads.add(serverNetService);
                    serverNetService.addServerMessageListener(server);
                    serverNetService.addServersDisconnectionListener(server);
                    serverNetService.addNetClientListener(server);
                    serverNetService.addRegistrationListener(server);
                    server.xStream.toXML("SERVER", socket.getOutputStream());
                    server.registeredUsers.putAll(((HashMap<String, String>) server.xStream.fromXML(socket.getInputStream())));
                    System.out.println("connected to another Server");
                    serverNetService.start();
                } catch (IOException e) {
                }
            }

            FileOutputStream fos = new FileOutputStream(Config.REGISTERED_USERS_FILE, false);
            server.xStream.toXML(server.registeredUsers, fos);
            fos.close();

            while (true) {
                Socket client = socketListener.accept();
                boolean isServer = false;
                try {
                    String type = server.xStream.fromXML(client.getInputStream()).toString(); //тип - сервер или клиент
                    if (type.toUpperCase().equals("SERVER")) isServer = true;

                    if (isServer) {
                        ServerNetServiceThread serverNetService = new ServerNetServiceThread(client);
                        server.serverNetServiceThreads.add(serverNetService);
                        serverNetService.addServerMessageListener(server);
                        serverNetService.addServersDisconnectionListener(server);
                        serverNetService.addNetClientListener(server);
                        serverNetService.addRegistrationListener(server);
                        server.xStream.toXML(server.registeredUsers,client.getOutputStream());
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
                } catch (XStreamException e) {
                    client.close();
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
        if (chatHistory.size() >= 5) {
            return chatHistory.subList(chatHistory.size() - 5, chatHistory.size());
        } else return chatHistory;
    }

    public boolean addMessage(ServerMessage message) {
        boolean b = chatHistory.add(new ServerMessage(message));
        try {
            FileOutputStream fos = new FileOutputStream(Config.CHAT_HISTORY_FILE, false);
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
            if (clientThread.getLogin() != null) {
                loginList.add(clientThread.getLogin());
            }
        }
        for (String login : usersOnlineInNet) {
            loginList.add(login);
        }
        return loginList;
    }


    @Override
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
        if (!account.login.equals("") && !account.login.toUpperCase().equals("SERVER") && !account.login.toUpperCase().equals("NOTIFICATION")
                && !registeredUsers.containsKey(account.login)) {
            if (!account.password.equals("")) {
                addRegisteredUser(account.login, account.password);
                for (ServiceMessageSender thread : serverNetServiceThreads) {
                    ServerMessage regMessage = new ServerMessage("SERVER", "REGISTRATION:" + account.login + ":PASSWORD:" + account.password);
                    serverNotificationsHistory.add(regMessage);
                    thread.sendMessage(regMessage);
                }
                return "#Success";
            } else
                return "#incorrectPassword";

        } else
            return "#alreadyRegistered";
    }

    @Override
    public String authorization(Account account) {
        if (!account.login.equals("") && registeredUsers.containsKey(account.login)) {
            if (!account.password.equals("")) {
                if (registeredUsers.get(account.login).equals(account.password)) {
                    for (ClientServiceThread thread : clientServiceThreads) {
                        if (thread.getLogin() != null && thread.getLogin().equals(account.login))
                            return "#UserAlreadyConnected";
                    }
                    if (usersOnlineInNet.contains(account.login))
                        return "#UserAlreadyConnected";
                    return "#Success";
                } else return "#passwordIncorrect";
            } else return "#emptyPassword";
        } else return "#notRegistered";
    }


    @Override
    public boolean broadcast(ServerMessage message) {   //false if loop
        if (message.getMessage().getFrom().equals("SERVER")) {
            if (!serverNotificationsHistory.contains(message)) {
                serverNotificationsHistory.add(message);
                System.out.println("[" + message.getMessage().getFrom() + "]: " + message.getMessage().getMessage());
                for (ServiceMessageSender thread : clientServiceThreads) thread.sendMessage(message);
                for (ServiceMessageSender thread : serverNetServiceThreads) thread.sendMessage(message);
                return true;
            }
        } else if (!chatHistory.contains(message)) {
            addMessage(message);
            System.out.println("[" + message.getMessage().getFrom() + "]: " + message.getMessage().getMessage());
            for (ServiceMessageSender thread : clientServiceThreads) thread.sendMessage(message);
            for (ServiceMessageSender thread : serverNetServiceThreads) thread.sendMessage(message);
            return true;
        }
        return false;
    }

//    @Override
//    public void broadcastOnNet(ServerMessage message) {
//        for (ServiceMessageSender thread : serverNetServiceThreads) thread.sendMessage(message);
//    }

    @Override
    public void clientDisconnected(ClientServiceThread clientServiceThread) {
        clientServiceThread.disable();
        clientServiceThreads.remove(clientServiceThread);
        broadcast(new ServerMessage("SERVER", "disconnected " + clientServiceThread.getLogin()));
    }

    @Override
    public void netClientDisconnected(String login) {
        usersOnlineInNet.remove(login);
    }

    @Override
    public void netClientConnected(String login) {
        usersOnlineInNet.add(login);
    }

    @Override
    public boolean serverDisconnected(ServerNetServiceThread serverNetServiceThread) {
        serverNetServiceThread.disable();
        return serverNetServiceThreads.remove(serverNetServiceThread);
    }

    @Override
    public boolean kick(String login) {
        for (ClientServiceThread clientService : clientServiceThreads) {
            if (clientService.getLogin().equals(login)) {
                clientDisconnected(clientService);
                return true;
            }
        }
        return false;
    }

    @Override
    public void exit() {
        for (ClientServiceThread clThread : clientServiceThreads) {
            if (clThread.getLogin() != null)
                for (ServiceMessageSender thread : serverNetServiceThreads)
                    thread.sendMessage(new ServerMessage("SERVER", "disconnected " + clThread.getLogin()));
        }
    }
}

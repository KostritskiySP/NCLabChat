public interface ConnectionListener {

    void onConnectionReady(ServerListenerThread tcpConnection);

    void onReceiveMessage(ServerListenerThread сonnect, Message message);

    void onDisconnect(ServerListenerThread сonnect);

    void onException(ServerListenerThread сonnect, Exception e);

}
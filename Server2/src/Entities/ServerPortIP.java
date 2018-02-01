package Entities;

/**
 * Created by Sergio on 11.01.2018.
 */
public class ServerPortIP {

    public ServerPortIP(int port, String ip) {
        this.ip = ip;
        this.port = port;
    }

    public int port;
    public String ip;

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        ServerPortIP serverPortIP = (ServerPortIP) obj;
        return (serverPortIP.port == this.port &&
                serverPortIP.ip.equals(this.ip));

    }


}

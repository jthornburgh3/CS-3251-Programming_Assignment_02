import java.net.InetAddress;
/**
 * A grouping of client IP address and port number used by the server to keep
 * track of different clients.
 */
public class ClientInfo {
    static int pn;
    static InetAddress ip;
    static boolean hasReceivedFinRequest = false;

    public ClientInfo(InetAddress ip, int pn) {
        this.pn = pn;
        this.ip = ip;
    }

    public String toString() {
        return this.ip + ":" + this.pn;
    }
}
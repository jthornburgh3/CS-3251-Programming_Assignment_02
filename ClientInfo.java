import java.net.InetAddress;
/**
 * A grouping of client IP address and port number used by the server to keep
 * track of different clients.
 */
public class ClientInfo {
    private int pn;
    private InetAddress ip;
    private boolean hasReceivedFinRequest = false;

    public ClientInfo(InetAddress ip, int pn) {
        this.pn = pn;
        this.ip = ip;
    }
    
    public InetAddress getIP() {
        return this.ip;
    }
    
    public int getPN() {
        return this.pn;
    }
    
    public void setIP(InetAddress ip) {
        this.ip = ip;
    }
    
    public void setPN(int pn) {
        this.pn = pn;
    }

    public String toString() {
        return this.ip + ":" + this.pn;
    }
}

/**
 * Used for Connection Monitoring thread to signal a loss in connection.
 */
public class ConnectionSignal {

    protected boolean connectionIsAlive = false;
    
    public ConnectionSignal(boolean connectionStatus) {
        this.connectionIsAlive = connectionStatus;
    }
    
    public synchronized boolean isAlive() {
        return this.connectionIsAlive;
    }
    
    public synchronized void setConnectionStatus(boolean connectionStatus) {
        this.connectionIsAlive = connectionStatus;
    }
}
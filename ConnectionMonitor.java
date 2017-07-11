import java.net.*;
import java.io.IOException;

/**
 * This is a seperate thread to confirm the connections between the
 * client and server.
 * 
 */ 
public class ConnectionMonitor extends Thread {

    long lastConfirmation = System.currentTimeMillis();
    long lastSend = System.currentTimeMillis();
    
    boolean monitoring = false;
    static ConnectionSignal cs;

    static DatagramSocket socket;
    static InetAddress hostIP;
    static int portNumber;
    
    public ConnectionMonitor(DatagramSocket sckt, InetAddress ip, 
                            int portNumber, ConnectionSignal cs) {
        this.socket = sckt;
        this.hostIP = ip;
        this.portNumber = portNumber;
        this.cs = cs;
    }
    
    @Override
    public void run() {
       startMonitoring();
    }
    
    // reset timeout 
    public void receivedConfirmationPacket() {
        if (monitoring)
            lastConfirmation = System.currentTimeMillis();
    }
    
    // sends packets every 500 milliseconds
    public void startMonitoring() {
        monitoring = true;
        while (monitoring) {
            if (System.currentTimeMillis() - lastSend >= 500) {
                sendConfimationPacket();
                lastSend = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - lastConfirmation > 8000) {
                cs.setConnectionStatus(false);
                return;
            }
        }
    }
    
    public void stopMonitoring() {
        monitoring = false;
    }
    
    private void sendConfimationPacket() {
        byte[] confirmData = {(byte) 0xFF};
        DatagramPacket sendPacket = new DatagramPacket(
                confirmData, confirmData.length, hostIP, portNumber);
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
    

}


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
    // will send confirmation packets iff sender == true;
    boolean sender = false;

    static DatagramSocket socket;
    static InetAddress hostIP;
    static int portNumber;
    
    public ConnectionMonitor(boolean sender, DatagramSocket sckt,
                             InetAddress ip, int portNumber) {
        this.sender = sender;
        this.socket = sckt;
        this.hostIP = ip;
        this.portNumber = portNumber;
    }
    
    @Override
    public void run() {
       
    }
    
    // reset timeout 
    public void receivedConfirmationPacket() {
        if (monitoring)
            lastConfirmation = System.currentTimeMillis();
    }
    
    // sends packets every 500 milliseconds
    public void startMonitoring() throws ConnectionLostException {
        monitoring = true;
        while (monitoring) {
            if (sender && System.currentTimeMillis() - lastSend >= 500) {
                sendConfimationPacket();
            }
            if (System.currentTimeMillis() - lastConfirmation > 8000) {
                throw new ConnectionLostException();
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


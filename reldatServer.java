import java.io.*;
import java.net.*;
 
public class reldatServer {
    
    static int portNumber;
    static int maxWindowSize;
    
    static DatagramSocket serverSocket;
    static InetAddress clientIP;
    
    static ConnectionMonitor cm;
    
    
    public static void main(String[] args) throws SocketException {

        if (args.length != 2) {
            System.err.println("reldat-server <UDP port number> <max recieve window size>");
            return;
        }
        
        portNumber = Integer.parseInt(args[0]);
        maxWindowSize = Integer.parseInt(args[1]);
        

        System.out.println(portNumber);
        
        if (!setUpClientConnection()) {
           // listen until connection is made
        }
       
    }
    
    //
    private void interpretPacket(byte[] packet) {
        
    }
    
    /**
     * Called once the server starts running.  Listens for syncing packets from
     * a client in a 3-way handshake and stop-and-wait format.
     * 
     * @return true when a connection is set up.  false otherwise
     */
    private static boolean setUpClientConnection() throws SocketException {
        // set up socket
        serverSocket = new DatagramSocket(portNumber);
        
        System.out.println("Listening for Client ...");
        boolean receivedSyncFromClient = false;
        while (!receivedSyncFromClient) {
            DatagramPacket p = PacketIO.receivePacket(serverSocket, null);
            if (null != p && p.getData()[0] == (byte) 0x1F) {
                
                System.out.println("Received Sync Request ...");
                clientIP = p.getAddress();
                receivedSyncFromClient = true;
            }
        }
        serverSocket.setSoTimeout(2000);
        for (int j = 0; j < 10; j++) {
            byte[] syncMsg2 = {(byte) 0x2F};
            PacketIO.sendPacket(syncMsg2, serverSocket, clientIP, portNumber);
            DatagramPacket p = PacketIO.receivePacket(serverSocket, null);
            if (null == p || p.getData()[0] != (byte) 0x3F) {
                System.out.println(p.getData()[0]);
                System.out.printf("Ack Unsuccessful.  Retrying (%d) ...\n", j + 1);
                if (j == 9) {
                    System.out.println("Connection unsuccessful.\n");
                    return false;
                }
            } else if (p.getData()[0] == (byte) 0x3F) {
                System.out.println("Connection Established.\n");
                break;
            }
        }
        System.out.println("error0");

        serverSocket.setSoTimeout(0);
        
        // set up ConnectionMonitor
        cm = new ConnectionMonitor(false, serverSocket, clientIP, portNumber);
        
        return true;
    }

    
    
}
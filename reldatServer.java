import java.io.*;
import java.net.*;
 
public class reldatServer {
    
    static int serverPN;
    static int clientPN;
    static int maxWindowSize;
    
    static DatagramSocket serverSocket;
    static InetAddress clientIP;
    
    static ConnectionMonitor cm;
    static ConnectionSignal cs;
    static boolean requestedDisconnect;
    
    
    public static void main(String[] args) throws SocketException {

        if (args.length != 2) {
            System.err.println("reldat-server <UDP port number> <max recieve window size>");
            return;
        }
        
        serverPN = Integer.parseInt(args[0]);
        maxWindowSize = Integer.parseInt(args[1]);

        // listen for client connection
        if (!setUpClientConnection()) {
            return;
        }
        
        // start connection monitoring
        cm.start();

        // start receiving packets
        while (cs.isAlive()) {
            interpretPacket(PacketIO.receivePacket(serverSocket, cm));
        } 
        
        // connection lost
        if (!requestedDisconnect) {
            System.out.println("\nConnection with server has been lost.  Terminating the client.");
        }
       
    }
    
    /**
     * Used for interpreting packets after connection has been set up.
     * If an connection monitoring packet is received, will ignore.
     */
    private static void interpretPacket(DatagramPacket packet) {
        if (null == packet) {
            System.out.println("got null pakcet");
            return;
        } else if (packet.getData()[0] == (byte) 0xFF) {
            // confirm packet (ignore)
        } else if (packet.getData()[0] == (byte) 0x4F) {
            // received disconnect request
            disconnectWithClient();
        }
        //System.out.println(packet.getData()[0]);
        
    }
    
    /**
     * Called once the server starts running.  Listens for syncing packets from
     * a client in a 3-way handshake and stop-and-wait format.
     * 
     * @return true when a connection is set up.  false otherwise
     */
    private static boolean setUpClientConnection() throws SocketException {
        // set up socket
        serverSocket = new DatagramSocket(serverPN);
        
        System.out.println("Listening for Client ...");
        boolean receivedSyncFromClient = false;
        while (!receivedSyncFromClient) {
            DatagramPacket p = PacketIO.receivePacket(serverSocket, null);
            if (null != p && p.getData()[0] == (byte) 0x1F) {
                System.out.println("Received Sync Request ...");
                clientIP = p.getAddress();
                clientPN = p.getPort();
                receivedSyncFromClient = true;
            }
        }
        for (int j = 0; j < 10; j++) {
            byte[] syncMsg2 = {(byte) 0x2F};
            PacketIO.sendPacket(syncMsg2, serverSocket, clientIP, clientPN);
            DatagramPacket p = PacketIO.receivePacket(serverSocket, null);
            if (null == p || p.getData()[0] != (byte) 0x3F) {
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
        
        // set up connection monitoring
        cs = new ConnectionSignal(true);
        cm = new ConnectionMonitor(serverSocket, clientIP, clientPN, cs);


        return true;
    }

    private static void disconnectWithClient() {
        System.out.println("Received Disconnect Request ...");
        requestedDisconnect = true;
        cm.stopMonitoring();
        cs.setConnectionStatus(false);
        for (int i = 0; i< 5000; i++) {
            byte[] finMsg2 = {(byte) 0x5F};
            PacketIO.sendPacket(finMsg2, serverSocket, clientIP, clientPN);
            DatagramPacket p = PacketIO.receivePacket(serverSocket, null);
            if (p != null && p.getData()[0] == (byte) 0x6F) {
                System.out.println("\nDisconnect successful.  Closing server.\n");
                break;
            }
        }
    }
    
}
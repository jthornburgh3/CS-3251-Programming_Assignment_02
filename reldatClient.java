import java.io.*;
import java.net.*;
import java.util.Scanner;
 
public class reldatClient {
    
    static String hostName;
    static int serverPN;
    static int clientPN;
    static int maxWindowSize;
    static boolean serverIsAlive = false;
    
    static DatagramSocket clientSocket;
    static InetAddress hostIP;
    
    static ConnectionMonitor cm;
    static ConnectionSignal cs;
    
    
    public static void main(String[] args) throws IOException {
        
        if (args.length != 2) {
            System.err.println("reldat-client <IP/hostname:UDP port number> <max window size>");
            return;
        } else if (!args[0].contains(":")) {
            // if IP:port string is missing ":"
            System.err.println("invalid IP:port");
            return;
        }
        
        try {
            hostName = args[0].split(":")[0];
            serverPN = Integer.parseInt(args[0].split(":")[1]);
            maxWindowSize = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("reldat-client <IP/hostname:UDP port number> <max window size>");
        }
        
        // set up connection with 3-way-handshake
        if (!setUpConnection()) {
            return;
        }
        
        // start connection monitoring
        cm.start();
        
        // listens for command line prompts
        waitForUserInput();  
    }
    
    /**
     * Recieves a valid input from the command line 
     * inputs:
     *    -transform <file name>
     *    -disconnect
     *
     * @return the File to transform or null if the connection has been 
     *          lost or terminated
     */
    public static File waitForUserInput() {
        Scanner userInput = new Scanner(System.in);
        File fileToSend;
        
        while (cs.isAlive()) {
            String newInput = userInput.nextLine();
            
            if (newInput.contains("transform")) {
                fileToSend = new File(newInput.split(" ")[1]);
                if (fileToSend.exists()) {
                    return fileToSend;
                }
            } else if (newInput.equals("disconnect")) {
                disconnect();
                return null;
            } else {
                System.out.println("Invalid Input\n  transform <file name>\n  disconnect");
            }
        }
        userInput.close();
        return null;
    }
    
    /**
     * Transforms a file by sending it to the server
     */
    public static void transformFile(File f) {
        // TODO
        System.out.println("Transforming file");
    
    /*
        String message = ""; //data to send
        
        // send data to server
        PacketIO.sendPacket(message.getBytes(), clientSocket, hostIP, serverPN);
        
        // receive from server
        String returnedMessage = PacketIO.receivePacket(clientSocket).getData().toString();
    */

    } 
       
    /**
     * Disconnect the client from the server
     */
    public static void disconnect() {
        System.out.println("Disconnect requested: terminating connection.");
        cm.stopMonitoring();
        cs.setConnectionStatus(false);
        for (int i = 0; i < 5000; i++) {
            byte[] finMsg1 = {(byte) 0x4F};
            PacketIO.sendPacket(finMsg1, clientSocket, hostIP, serverPN);
            DatagramPacket p = PacketIO.receivePacket(clientSocket, null);
            if (p != null && p.getData()[0] == (byte) 0x5F) {
                System.out.println("Disconnect successful.  Closing client.\n");
                break;
            }
        }
        byte [] finMsg3 = {(byte) 0x6F};
        for (int j = 0; j < 5; j++) {
            PacketIO.sendPacket(finMsg3, clientSocket, hostIP, serverPN);
        }
    }
   
    /** 
     * Called once the client starts running.  
     * Sets up the connection using a 3-way-handshake and stop-and-wait format
     */
    private static boolean setUpConnection() throws SocketException {
        System.out.println("Setting up connection...");
        
        // set up socket
        try {
            clientSocket = new DatagramSocket();
            hostIP = InetAddress.getByName(hostName);
            clientPN = clientSocket.getLocalPort();
        } catch (SocketException e) {
            e.printStackTrace(System.out);
            return false;
        } catch (UnknownHostException e) {
            System.err.println("DNS lookup failed");
            return false;
        }
        
        // send handshakes
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 10; j++) {
                if (i == 0) {
                    byte[] syncMsg1 = {(byte) 0x1F};
                    PacketIO.sendPacket(syncMsg1, clientSocket, hostIP, serverPN);
                    DatagramPacket p = PacketIO.receivePacket(clientSocket, null);
                    if (null == p || p.getData()[0] != (byte) 0x2F) {
                        System.out.printf("Unsuccessful.  Retrying (%d) ...\n", j + 1);
                        if (j == 9) {
                            System.out.println("Connection unsuccessful.  Closing client.");
                            return false;
                        }
                    } else if (p.getData()[0] == (byte) 0x2F) {
                        System.out.println("Connection Established.\n");
                        break;
                    }
                } else if (i == 1) {
                    byte [] syncMsg3 = {(byte) 0x3F};
                    if (j < 3) { // fast retransmit
                        PacketIO.sendPacket(syncMsg3, clientSocket, hostIP, serverPN);
                    } else {
                        break;
                    }
                }
            }
        }
        
        // initialize ConnectionMonitor
        cs = new ConnectionSignal(true);
        cm = new ConnectionMonitor(clientSocket, hostIP, serverPN, cs);
        
        return true;        
    }

}

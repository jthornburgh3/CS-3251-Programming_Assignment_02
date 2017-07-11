import java.io.*;
import java.net.*;
import java.util.Scanner;
 
public class reldatClient {
    
    static String hostName;
    static int portNumber;
    static int maxWindowSize;
    static boolean serverIsAlive = false;
    
    static DatagramSocket clientSocket;
    static InetAddress hostIP;
    
    static ConnectionMonitor cm;
    
    
    
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
            portNumber = Integer.parseInt(args[0].split(":")[1]);
            maxWindowSize = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("reldat-client <IP/hostname:UDP port number> <max window size>");
        }
        
        System.out.println(hostName);
        System.out.println(portNumber);
        
        // set up connection with 3-way-handshake
        if (!setUpConnection()) {
            return;
        }
        
        // start connection monitoring
        try {
            cm.startMonitoring();
        } catch (ConnectionLostException e) {
            System.out.println("\nConnection with server has been lost.  Terminating the client.");
            return;
        }
        
        waitForUserInput();

        
    }
    
    /**
     * Recieves a valid input from the command line 
     * inputs:
     *    -transform <file name>
     *    -disconnect
     */
    public static void waitForUserInput() {
        Scanner userInput = new Scanner(System.in);
        File fileToSend;
        
        boolean validUserInput = false;
        while (!validUserInput) {
            String newInput = userInput.nextLine();
            
            if (newInput.contains("transform")) {
                fileToSend = new File(newInput.split(" ")[1]);
                if (fileToSend.exists()) {
                    transformFile(fileToSend);
                    validUserInput = true;
                }
            } else if (newInput.equals("disconnect")) {
                disconnect();
                validUserInput = true;
            } else {
                System.out.println("Invalid Input\n  transform <file name>\n  disconnect");
            }
        }
        
        userInput.close();
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
        PacketIO.sendPacket(message.getBytes(), clientSocket, hostIP, portNumber);
        
        // receive from server
        String returnedMessage = PacketIO.receivePacket(clientSocket).getData().toString();
    */

    } 
       
    /**
     * Disconnect the client from the server
     */
    public static void disconnect() {
        System.out.println("Disconnect requested: terminating connection.");
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
        } catch (SocketException e) {
            e.printStackTrace(System.out);
            return false;
        } catch (UnknownHostException e) {
            System.err.println("DNS lookup failed");
            return false;
        }
        
        // send handshakes
        clientSocket.setSoTimeout(2000);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 10; j++) {
                if (i == 0) {
                    byte[] syncMsg1 = {(byte) 0x1F};
                    PacketIO.sendPacket(syncMsg1, clientSocket, hostIP, portNumber);
                    DatagramPacket p = PacketIO.receivePacket(clientSocket, null);
                    if (null == p || p.getData()[0] != (byte) 0x2F) {
                        System.out.printf("Unsuccessful.  Retrying (%d) ...\n", j + 1);
                        if (j == 9) {
                            System.out.println("Connection unsuccessful.  Closing client.");
                            return false;
                        }
                    } else if (p.getData()[0] == (byte) 0x2F) {
                        System.out.println("Connection Established.\n");
                        j = 10;
                    }
                } else if (i == 1) {
                    byte [] syncMsg3 = {(byte) 0x3F};
                    // fast retransmit
                    PacketIO.sendPacket(syncMsg3, clientSocket, hostIP, portNumber);
                    PacketIO.sendPacket(syncMsg3, clientSocket, hostIP, portNumber);
                    PacketIO.sendPacket(syncMsg3, clientSocket, hostIP, portNumber);
                }
            }
        }
        clientSocket.setSoTimeout(0); // set socket to not timeout
        
        // initialize ConnectionMonitor
        cm = new ConnectionMonitor(true, clientSocket, hostIP, portNumber);
        
        return true;        
    }

}

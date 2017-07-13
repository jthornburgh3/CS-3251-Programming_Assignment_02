import java.io.*;
import java.net.*;
import java.util.Scanner;

public class reldatClient {

    static String hostName;
    static int serverPN;
    static int clientPN;
    static int maxWindowSize;
    static boolean serverIsAlive = false;
    static long rtt; // round trip time (milliseconds)

    static DatagramSocket clientSocket;
    static InetAddress hostIP;

    //static ConnectionMonitor cm;
    //static ConnectionSignal cs;


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

        /*
           1. request connection with server, repeat until reponse is received
           2. wait for user input.
                a) if transform requested, complete transform then repeat step 2
                b) if disconnect requested, perform disconnect then close client
         */

        // set up connection 
        if (!setUpConnection()) {
            return;
        }

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

        while (true) {
            // block until get a new input from user
            System.out.println("Waiting for input: ");
            String newInput = userInput.nextLine();
            if (newInput.contains("transform")) {
                try {
                   fileToSend = new File(newInput.split(" ")[1]);
                    if (fileToSend.exists()) {
                        transformFile(fileToSend);
                    } else {
                        System.out.println("File does not exist.");
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Invalid Input\n  transform <file name>\n  disconnect");
                }
            } else if (newInput.equals("disconnect")) {
                disconnect();
                userInput.close();
                return null;
            } else {
                System.out.println("Invalid Input\n  transform <file name>\n  disconnect");
            }
        }
    }

    /**
     * Transforms a file by sending it to the server
     */
    public static void transformFile(File f) {
        // TODO
        System.out.println("Transforming file");
        System.out.println("mine: "  + clientPN);
        confirmConnection(false);

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
    private static void disconnect() {
        System.out.println("Disconnect requested: terminating connection.");
        if (!confirmConnection(true))
            return;

        long curTime = System.currentTimeMillis();
        long lastSend = 0L;

        for (int i = 0; i < 10; i++) {
            byte[] finMsg1 = {(byte) 0x4F};
            PacketIO.sendPacket(finMsg1, clientSocket, hostIP, serverPN);
            DatagramPacket p = PacketIO.receivePacket(clientSocket);
            if (p != null && p.getData()[0] == (byte) 0x5F) {
                System.out.println("Disconnect successful.  Closing client.\n");
                break;
            } else {
                System.out.println(null == p ? "null" : p.getData()[0]);
            }
        }
        byte [] finMsg3 = {(byte) 0x6F};
        for (int j = 0; j < 5; j++) {
            // fast retransmit
            PacketIO.sendPacket(finMsg3, clientSocket, hostIP, serverPN);
        }
    }

    /**
     * Called once the client starts running.
     * Sets up socket.
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

        confirmConnection(true);

        return true;
    }

    private static boolean confirmConnection(boolean printOut) {
        // send handshakes
        for (int j = 0; j < 10; j++) {
            byte[] syncMsg1 = {(byte) 0x1F};
            long packetSendTime = System.currentTimeMillis();
            PacketIO.sendPacket(syncMsg1, clientSocket, hostIP, serverPN);
            DatagramPacket p = PacketIO.receivePacket(clientSocket);
            if (null == p || p.getData()[0] != (byte) 0x2F) {
                System.out.printf("No response from server.  Retrying (%d) ...\n", j + 1);
                if (j == 9) {
                    System.out.println("Connection with server has been lost.  Closing client.");
                    return false;
                }
            } else if (p.getData()[0] == (byte) 0x2F) {
                rtt = System.currentTimeMillis() - packetSendTime;
                if (printOut)
                    System.out.println("Connection Established.\n");
                break;
            }
        }
        return true;
    }

    private static boolean hasTimedOut(long timeout, long origTime) {
        return (System.currentTimeMillis() - origTime) > timeout;
    }

}

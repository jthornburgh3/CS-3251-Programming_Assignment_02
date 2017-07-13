import java.io.*;
import java.net.*;
import java.util.List;
import java.util.LinkedList;

public class reldatServer {

    static int serverPN;
    static int maxWindowSize;

    static DatagramSocket serverSocket;

    static List<ClientInfo> clientList;


    public static void main(String[] args) throws SocketException {

        if (args.length != 2) {
            System.err.println("reldat-server <UDP port number> <max recieve window size>");
            return;
        }

        serverPN = Integer.parseInt(args[0]);
        maxWindowSize = Integer.parseInt(args[1]);

        clientList = new LinkedList<ClientInfo>();
        serverSocket = new DatagramSocket(serverPN);

        // start receiving packets
        while (true) {
            interpretPacket(PacketIO.receivePacket(serverSocket));
            //System.out.println("fin: " + clientList);
        }

    }

    /**
     * Used for interpreting packets after connection has been set up.
     * If a new connection request is received, will set up new client connection.
     * If an connection monitoring packet is received, will refresh connection.
     * If a request termination packet is received, calls disconnect()
     */
    private static void interpretPacket(DatagramPacket packet) {
        
        if (null == packet) {
            //System.out.println(null == packet ? "null" : packet.getData()[0]);
            return;
        } else if (packet.getData()[0] == (byte) 0x1F) {
            // new client connection request
            setUpClientConnection(packet);
        } else if (packet.getData()[0] == (byte) 0x4F) {
            // received disconnect request
            disconnectWithClient(packet);
        } else if (packet.getData()[0] == (byte) 0x6F) {
            // received finAck from client
            processFinAckPacket(packet);
        }
        //System.out.println(packet.getData()[0]);

    }

    /**
     * Establises a connection
     *
     * @return true when a connection is set up.  false otherwise
     */
    private static boolean setUpClientConnection(DatagramPacket p) {
        ClientInfo curClient = getClient(p);
        if (null == curClient) {
            // add new client to clientList
            InetAddress receivedIP = p.getAddress();
            int receivedPN = p.getPort();
            ClientInfo newClient = new ClientInfo(receivedIP, receivedPN);
            System.out.println("Established connection with client: " + newClient.getIP()  + ":" + newClient.getPN());
            clientList.add(newClient);
            curClient = newClient;
        } else {
            //System.out.println("not null");
        }

        // if lost/corrupted, client will send another request
        byte[] syncMsg2 = {(byte) 0x2F};
        PacketIO.sendPacket(syncMsg2, serverSocket, curClient.getIP(), curClient.getPN());

        return true;
    }

    // returns false if received a packet from unknown client
    private static boolean disconnectWithClient(DatagramPacket p) {
        ClientInfo curClient = getClient(p);
        if (null == curClient) {
            return false;
        }
        byte[] finAck = {(byte) 0x5F};
        PacketIO.sendPacket(finAck, serverSocket, curClient.getIP(), curClient.getPN());
        return true;
    }

    private static void processFinAckPacket(DatagramPacket p) {
        ClientInfo curClient = getClient(p);
        if (null == curClient) {
            return;
        } else {
            System.out.println("Successfully disconnected with Client "
                    + curClient.getIP() + ":" + curClient.getPN());
            clientList.remove(curClient);
        }
    }

    private static boolean hasTimedOut(long timeout, long origTime) {
        return (System.currentTimeMillis() - origTime) > timeout;
    }

    // returns the client info of the given packet or null if not found
    private static ClientInfo getClient(DatagramPacket p) {
        for (ClientInfo c: clientList) {
            if (c.getIP().equals(p.getAddress()) && c.getPN() == p.getPort()) {
                return c;
            }
        }
        return null;
    }

}
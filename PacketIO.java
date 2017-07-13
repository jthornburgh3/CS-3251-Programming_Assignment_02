import java.io.IOException;
import java.net.*;


/**
 * PacketIO.sendPacket() to send a datagram packet
 * PacketIO.receivePacket() to receive a datagram packet
 */
public class PacketIO {
    
    /**
     * Sends a datagram packet on the specified socket.
     */
    public static void sendPacket(byte[] sendData, DatagramSocket socket,
                                  InetAddress ip, int portNumber) {
        DatagramPacket packetToSend
            = new DatagramPacket(sendData, sendData.length, ip, portNumber);
        try {
            socket.send(packetToSend);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Receives a datagram packet.
     */
    public static DatagramPacket receivePacket(DatagramSocket socket) {
        byte[] receiveData = new byte[1007]; // 7 bytes for header, 1000 for payload
        DatagramPacket incommingPacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            socket.setSoTimeout(1000);
            socket.receive(incommingPacket);
        } catch (UnknownHostException e) {
            e.printStackTrace(System.out);
        } catch (SocketTimeoutException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        return incommingPacket;
    }
}
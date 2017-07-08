import java.io.*;
import java.net.*;
import java.util.Scanner;
 
public class reldatClient {
    
    private static String hostName;
    private static int portNumber;
    
    public static void main(String[] args) throws IOException {
        
        if (args.length != 3) {
            System.err.println("reldat-client <IP/hostname:UDP port number> <max window size>");
            return;
        } else if (!args[0].contains(":")) {
            // if IP:port string is missing ":"
            System.err.println("invalid IP:port");
            return;
        }
        
        hostName = args[0].split(":")[0];
        portNumber = Integer.parseInt(args[0].split(":")[1]);
        
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
        DatagramSocket clientSocket;
        InetAddress IPAddress;
        try {
            clientSocket = new DatagramSocket();
            IPAddress = InetAddress.getByName(hostName);
        
        
            String message = ""; //data to send
            
            // send data to server
            sendToServer(message.getBytes(), clientSocket, IPAddress);
            
            // receive from server
            String returnedMessage = receiveFromServer(clientSocket).toString();
            System.out.println(returnedMessage);
            

            clientSocket.close();
        } catch (SocketException e) {
            e.printStackTrace(System.out);
        } catch (UnknownHostException e) {
            System.out.println("Bad Host Name");
        }
    }
       
    /**
     * Disconnect the client from the server
     */
    public static void disconnect() {
        
    }
    
   // sends a datagram packet to the server
    private static void sendToServer(byte[] sendData, DatagramSocket socket, InetAddress ip) {
        DatagramPacket sendPacket
            = new DatagramPacket(sendData, sendData.length, ip, portNumber);
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
    
    // receives a datagram packet from the server
    private static byte[] receiveFromServer(DatagramSocket socket) {
        //socket.setSoTimeout(2000);  // timeout after 2 seconds
        byte[] receiveData = new byte[8000];
        DatagramPacket incommingPacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            socket.receive(incommingPacket);
        } catch (UnknownHostException e) {
            e.printStackTrace(System.out);
        } catch (SocketTimeoutException e) {
            System.out.println("Timeout while waiting for packet");
        } catch (IOException e) {
            e.printStackTrace(System.out);
        } 
        return incommingPacket.getData();
    }
}

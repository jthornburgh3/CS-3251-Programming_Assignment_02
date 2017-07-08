import java.io.*;
import java.net.*;
 
public class reldatServer {
    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("reldat-server <UDP port number> <max recieve window size>");
            return;
        }
        
        int portNumber = Integer.parseInt(args[0]);
        int maxWindowSize = Integer.parseInt(args[1]);
        
       
    }
}
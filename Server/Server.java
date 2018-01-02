import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Makes a connection between the server and the client.
 * The class will run when you start up the application.
 */
public class Server {
    int portNumber = 15883;
    ServerSocket serverSoc = null;
    ArrayList<SocketManager> clients = null;
    DataManager dm;
    FileWriter file;
    BufferedWriter writeFile;
    
    /**
     * Main function where you can type in the port number of the server
     * 
     * @param args
     */
    public static void main(String[] args){
    	System.out.print("Please type in the port number the server has to act on: ");
    	
    	Scanner scan = new Scanner(System.in);
    	int port = scan.nextInt();
    	Server server = new Server(port);  
    	
    	scan.close();
    }
    
    /**
     * Starts up the server with the port number defined in the main function. Server
     * starts looking for clients and add them to the SocketManager when found. Then 
     * starts up a ConnectionHandeler and a thread.
     * 
     * @param port
     */
    public Server (int port) {
		if (port > 2048) {
			portNumber = port;
		} else {
			System.err.println("Port number too low, defaulting to 15882");
		}
        
        try{
            serverSoc = new ServerSocket(portNumber);
            clients = new ArrayList<SocketManager>();
            
            dm = new DataManager();
            
            File file = new File ("message.xml");
            
            while (true){
                System.out.println("Waiting for client");
                Socket soc = serverSoc.accept();              
                SocketManager sm = new SocketManager(soc);
                
                synchronized(clients) {
                	clients.add(sm);   
                	System.out.println("Client connected");
                }
                
                ServerConnectionHandler sch = new ServerConnectionHandler(clients, dm, file, sm, true);
                Thread schThread = new Thread(sch);
                schThread.start();
            }
        }
        catch (Exception except){
            System.err.println("Error --> " + except.getMessage());
        }
    }   
}





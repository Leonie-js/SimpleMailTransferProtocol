import java.net.*;
import java.util.Scanner;

/**
 * Sets up the Client to make a connection with the Server.
 *
 */
public class Client {

	private StateManager sm = new StateManager();

	/**
	 * Sets up the Socket and readingThread and writingThread.
	 * 
	 * @param serverIP
	 * @param portNumber
	 */
	public Client(String serverIP, int portNumber) {
		try {
			Socket soc = new Socket(serverIP, portNumber);

			ClientReader clientRead = new ClientReader(soc, sm);
			Thread clientReadThread = new Thread(clientRead);
			clientReadThread.start();

			ClientWriter clientWrite = new ClientWriter(soc, sm);
			Thread clientWriteThread = new Thread(clientWrite);
			clientWriteThread.start();
		} catch (Exception except) {
			System.out.println("No connection has been made, " + except);
			
		}

	}

	/**
	 * Allows the user type in the portNumber and serverIP and sets up the
	 * connection.
	 * 
	 */
	public static void main(String[] args) {

		Scanner scan = new Scanner(System.in);

		System.out.print("Please type in the port number: ");
		int portNumber = scan.nextInt();
		System.out.print("Please type in the server IP: ");
		String serverIP = scan.next();

		Client client = new Client(serverIP, portNumber);
	}
}

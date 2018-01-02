import java.io.DataInputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Reads the responses the Server sends through the socket. Looks if the Client
 * gets the appropriate responses.
 *
 */
public class ClientReader implements Runnable {
	Socket cwSocket = null;
	String messageServer = "";
	boolean running = true;
	private StateManager sm;
	String error = "Something went wrong, please try again";

	/**
	 * Initializes the ClientReader with the Socket and StateManager
	 * 
	 * @param inputSoc
	 * @param state
	 */
	public ClientReader(Socket inputSoc, StateManager state) {
		cwSocket = inputSoc;
		sm = state;
	}

	/**
	 * Looks if the Server sends a appropriate response for the currentState
	 */
	public void run() {
		try {
			while (running) {
				DataInputStream dataIn = new DataInputStream(cwSocket.getInputStream());

				messageServer = dataIn.readUTF();
				System.out.println(messageServer);

				if (sm.getState() == State.HELO) {
					if (messageServer.contains("250")) {
						sm.setState(State.A_HELO);
					} else {
						System.out.println(error);
					}
				} else if (sm.getState() == State.MAIL) {
					if (messageServer.contains("250")) {
						sm.setState(State.A_MAIL);
					} else {
						System.out.println(error);
					}
				} else if (sm.getState() == State.RCPT) {
					if (messageServer.contains("250")) {
						
					} else {
						System.out.println(error);
					}
				} else if (sm.getState() == State.DATA) {
					if (messageServer.contains("354")) {
						sm.setState(State.A_DATA);
					} else {
						System.out.println(error );
					}
				} else if (sm.getState() == State.MSG) {
					if (messageServer.contains("250")) {
						sm.setState(State.A_MSG);
					} else {
						System.out.println(error);
					}
				}

				if (messageServer.contains("221")) {
					running = false;
					
					System.out.println("Closing Client");
				}
			}
		} catch (SocketException e) {
			System.out.println("Server terminated the connection");
		} catch (Exception except) {
			System.out.println("Error in Reader--> " + except.getMessage());
			except.printStackTrace();
		}
	}

}
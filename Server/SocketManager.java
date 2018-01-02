import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Manager that makes the Stream for the Input and Output from the socket.
 *
 */
public class SocketManager {
	public Socket soc = null;
	public DataInputStream input = null;
	public DataOutputStream output = null;
	String name = null;

	/**
	 * Initializes the Socket Manager with the Socket and makes the DataStreams.
	 * 
	 * @param socket
	 * 
	 */
	public SocketManager(Socket socket) throws IOException {
		try {
			soc = socket;
			input = new DataInputStream(soc.getInputStream());
			output = new DataOutputStream(soc.getOutputStream());
			name = soc.getLocalAddress().getHostName();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the InputStream
	 * 
	 * @return
	 */
	synchronized public DataInputStream getInput() {
		return input;
	}

	/**
	 * Returns the OutputStream
	 * 
	 * @return
	 */
	synchronized public DataOutputStream getOutput() {
		return output;
	}

	/**
	 * Closes the Socket
	 */
	public void close() {
		try {
			input.close();
			output.close();
			soc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Allows to change the name of the Socket
	 * 
	 * @param val
	 */
	synchronized public void setName(String val) {
		name = val;
	}

	/**
	 * Returns the name of the Socket
	 * 
	 * @return
	 */
	synchronized public String getName() {
		return name;
	}

	/**
	 * Returns the HostAddress of the Socket
	 * 
	 * @return
	 */
	synchronized public String ip() {
		return soc.getInetAddress().getHostAddress();
	}

	/**
	 * Returns the Port of the Socket
	 * 
	 * @return
	 */
	synchronized public String port() {
		return Integer.toString(soc.getPort());
	}
}

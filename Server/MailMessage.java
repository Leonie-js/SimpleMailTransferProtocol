import java.util.ArrayList;

/**
 * Saves the data of a message.
 *
 */
public class MailMessage {
	public String from;
	public ArrayList<String> toList;
	public ArrayList<String> message;

	/**
	 * Initializes the MailMessage with the ArrayList for the toList and message
	 */
	public MailMessage() {
		toList = new ArrayList<String>();
		message = new ArrayList<String>();
	}
}

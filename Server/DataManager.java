import java.util.ArrayList;

/**
 * Manager that keeps track of the messages.
 *
 */
public class DataManager {
	public ArrayList<MailMessage> allMail;
	public MailMessage currentMessage; // better would have a per connection current Message

	/**
	 * Initializes the ArrayList of allMail
	 */
	public DataManager() {
		allMail = new ArrayList<MailMessage>();
	}
}

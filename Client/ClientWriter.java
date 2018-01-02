import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Ask the User for all the data the program needs to send the commands to the
 * server. Writes out the full message when made and sends it to the Server.
 * 
 */
public class ClientWriter implements Runnable {
	String ipAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
	String domainNameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";
	String emailAdressRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
	Socket cwSocket = null;
	State writeState = State.NONE;
	State sendState = State.NONE;
	String answer;
	String domain;
	String mailFrom;
	ArrayList<String> mailTo = new ArrayList<String>();
	ArrayList<String> fullMessage = new ArrayList<String>();
	String subject;
	String message = "";
	Date date;
	StateManager sm;
	boolean running = true;

	/**
	 * Initializes the Socket and the StateManager of the ClientWriter
	 * 
	 * @param outputSoc
	 * @param state
	 */
	public ClientWriter(Socket outputSoc, StateManager state) {
		cwSocket = outputSoc;
		sm = state;
	}

	/**
	 * Runs the UserInput till the Email is send
	 */
	public void run() {
		try {
			while (running) {
				UserInput();
			}
		} catch (Exception except) {
			System.out.println("Error in Writer--> " + except.getMessage());
			except.printStackTrace();
		}
	}

	/**
	 * Asks the input of the User and stores to data to show and send the email
	 */
	public void UserInput() {

		Scanner scan = new Scanner(System.in);

		if (writeState == State.NONE) {
			System.out.println("Do you want to send an email? Answer 'yes' or 'no'");

			answer = scan.nextLine();

			if (answer.toLowerCase().equals("yes")) {
				writeState = State.HELO;
			} else if (answer.toLowerCase().equals("no")) {
				System.out.println("You didn't want to send an email, closing down connection");
				scan.close();
				
				sendState = State.QUIT;
				writeState = State.QUIT;
				sm.setState(State.QUIT);
				
				SendEmail();
			} else {
				System.out.println("You didn't answer 'yes' or 'no', please try again.");
			}
		} else if (writeState == State.HELO) {
			System.out.println("From which domain do you want to send an email?");

			answer = scan.nextLine();
			if (answer.matches(ipAddressRegex) || answer.matches(domainNameRegex)) {
				domain = answer;
				System.out.println("Okay domain is set to " + domain);
				writeState = State.MAIL;
			} else {
				System.out.println("You have not typed in a valid domain, please try again.");
			}
		} else if (writeState == State.MAIL) {
			System.out.println("From which email address do you want to send an email?");

			answer = scan.nextLine();
			if (answer.matches(emailAdressRegex)) {
				mailFrom = answer;
				System.out.println("Okay the email address where the email is send from is " + mailFrom);
				writeState = State.RCPT;

			} else {
				System.out.println("You have not typed in a valid email address, please try again.");
			}
		} else if (writeState == State.RCPT) {
			System.out.println("To which email address do you want to send the email?");

			answer = scan.nextLine();
			if (answer.matches(emailAdressRegex)) {
				mailTo.add(answer);
				System.out.println("Okay the email address where the email is send to is " + mailTo);

				while (writeState != State.DATA) {
					System.out.println("Do you want to send the email to another email address? Answer 'yes' or no'");

					answer = scan.nextLine();
					if (answer.toLowerCase().equals("yes")) {
						System.out.println("Please type in the other email address: ");

						answer = scan.nextLine();
						if (answer.matches(emailAdressRegex)) {
							mailTo.add(answer);
							System.out.println("Okay the email address where the email is send to is " + mailTo);
						} else {
							System.out.println("You have not typed in a valid email address, please try again.");
						}

					} else if (answer.toLowerCase().equals("no")) {
						System.out.println("Okay now let's make the message.");
						writeState = State.DATA;
					} else {
						System.out.println("You didn't answer 'yes' or 'no', please try again.");
					}
				}
			} else {
				System.out.println("You have not typed in a valid email address, please try again.");
			}
		} else if (writeState == State.DATA) {
			System.out.println("What do you want as subject of your message?");

			answer = scan.nextLine();
			System.out.println("Do you want '" + answer + "' as your subject? Answer 'yes' or 'no'");
			subject = answer;

			answer = scan.nextLine();
			if (answer.toLowerCase().equals("yes")) {
				System.out.println("Okay " + subject + " is your subject of the message.");
				System.out.println("You can now write your message, stop with 'ENDMESSAGE'");

				answer = scan.nextLine();
				while (!answer.toUpperCase().equals("ENDMESSAGE")) {
					if (answer.equals(".")) {
						answer = "..";
					}
					
					message = message + " " + answer;

					answer = scan.nextLine();
				}

				System.out.println("Okay I have saved your message");

				writeState = State.MSG;
			} else if (answer.toLowerCase().equals("no")) {
				System.out.println("Okay I will ask again.");
			} else {
				System.out.println("You didn't answer 'yes' or 'no', please try again.");
			}
		} else if (writeState == State.MSG) {

			date = new Date();
			ShowMessage();

			System.out.println("Send the email? Answer 'yes' or 'no'");

			answer = scan.nextLine();
			if (answer.toLowerCase().equals("yes")) {

				System.out.println("Email is being send.");

				while (running) {
					SendEmail();
					
					if (sm.getState() == State.E_MSG) {
						break;
					}
				}
				
				System.out.println("Closing Client. For new message restart please");
				
				scan.close();
					
				sendState = State.QUIT;
				writeState = State.QUIT;
				sm.setState(State.QUIT);
					
				SendEmail();
				

			} else if (answer.toLowerCase().equals("no")) {
				System.out.println("You didn't want to send the email. Closing down connection");

				scan.close();
					
				sendState = State.QUIT;
				writeState = State.QUIT;
				sm.setState(State.QUIT);
					
				SendEmail();

			} else {
				System.out.println("You didn't answer 'yes' or 'no', please try again.");
			}
		}
	}

	/**
	 * Shows the Messages and saves the additional text in the message
	 */
	public void ShowMessage() {
		System.out.println("-------------EMAIL----------------------------");
		System.out.println("Date: " + date);
		System.out.println("Send from: " + mailFrom);
		System.out.println("Send to: " + mailTo);
		System.out.println("Subject: " + subject);
		System.out.println();
		System.out.println(message);
		System.out.println("-------------EMAIL----------------------------");

		fullMessage.add("Date: " + date);
		fullMessage.add("Send from: " + mailFrom);
		fullMessage.add("Send to: " + mailTo);
		fullMessage.add("Subject: " + subject);
		fullMessage.add(message);

	}

	/**
	 * Sends the email in States to the Server
	 */
	public void SendEmail() {
		try {
			DataOutputStream dataOut = new DataOutputStream(cwSocket.getOutputStream());

			while (running) {

				sendState = sm.getState();

				if (sendState == State.NONE) {
					dataOut.writeUTF("HELO " + domain);
					dataOut.flush();

					sm.setState(State.HELO);
				} else if (sendState == State.A_HELO) {
					dataOut.writeUTF("MAIL FROM:<" + mailFrom + ">");
					dataOut.flush();

					sm.setState(State.MAIL);
				} else if (sendState == State.A_MAIL) {

					for (int i = 0; i < mailTo.size(); i++) {
						dataOut.writeUTF("RCPT TO:<" + mailTo.get(i) + ">");
						dataOut.flush();

						sm.setState(State.RCPT);
					}

					sm.setState(State.A_RCPT);

				} else if (sendState == State.A_RCPT) {
					dataOut.writeUTF("DATA");
					dataOut.flush();

					sm.setState(State.DATA);
				} else if (sendState == State.A_DATA) {

					for (int o = 0; o < fullMessage.size(); o++) {
						dataOut.writeUTF(fullMessage.get(o));
						dataOut.flush();
					}

					dataOut.writeUTF(".");
					dataOut.flush();

					sm.setState(State.MSG);
				} else if (sendState == State.A_MSG) {
					System.out.println("Message delivered");
					
					sm.setState(State.E_MSG);
					break;
					
				} else if (sendState == State.QUIT) {
					dataOut.writeUTF("QUIT");
					dataOut.flush();
					
					running = false;
				}
			}
		} catch (SocketException e) {
			System.out.println("Server terminated the connection");
		} catch (IOException exception) {
			System.out.println("Error in Reader--> " + exception.getMessage());
		}

	}

}
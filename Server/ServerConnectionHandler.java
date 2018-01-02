package Assesment1;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Looks what the message says and send a correct response back.
 */
public class ServerConnectionHandler implements Runnable {
	SocketManager selfs = null;
	ArrayList<SocketManager> clients = null;
	boolean verbose = false;
	State currentState = State.NONE;
	DataManager dm;
	File file;

	/**
	 * Sets the variables to variables send through the function parameters.
	 *
	 * @param l
	 * @param dm
	 * @param file
	 * @param inSoc
	 * @param v
	 */
	public ServerConnectionHandler(ArrayList<SocketManager> l, DataManager dm, File file, SocketManager inSoc,
			boolean v) {
		selfs = inSoc;
		clients = l;
		verbose = v;
		this.dm = dm;
		this.file = file;
	}

	/**
	 * Tells the Client that the Server is ready and shows the messages that the
	 * Client has send.
	 */
	public void run() {
		try {
			Response(220);
			selfs.output.flush();

			while (clients.size() > 0) {
				String message = selfs.input.readUTF();
				System.out.println("--> " + message);

				currentState = parse(message, selfs);
				System.out.println(currentState);
			}
		} catch (SocketException exception) {
			System.out.println("Client terminated the connection");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in ServerHandler--> " + e.getMessage());
		}
	}

	/**
	 * Shows the correct response message according the the code that is send.
	 * 
	 * @param Error
	 */
	public void Response(int Error) {
		if (Error == 250) {
			try {
				selfs.output.writeUTF("250 mail action okay");
			} catch (Exception except) {
				System.err.println("Error --> " + except.getMessage());
			}
		} else if (Error == 220) {
			try {
				selfs.output.writeUTF("220 derby.ac.uk Simple Mail Transfer Service Ready");
			} catch (Exception except) {
				System.err.println("Error --> " + except.getMessage());
			}
		} else if (Error == 221) {
			try {
				selfs.output.writeUTF("221 " + clients.get(0).getName() + " Service closing transmission channel.");
			} catch (Exception except) {
				System.err.println("Error --> " + except.getMessage());
			}
		} else if (Error == 214) {
			try {
				selfs.output.writeUTF(
						"214 Command in sequence order HELO, MAIL, RCPT, DATA and '.'. QUIT and NOOP can always be said. ");
			} catch (Exception except) {
				System.err.println("Error --> " + except.getMessage());
			}
		} else if (Error == 354) {
			try {
				selfs.output.writeUTF("354 Start mail input; end with <CRLF>.<CRLF>'");
			} catch (Exception except) {
				System.err.println("Error --> " + except.getMessage());
			}
		} else if (Error == 500) {
			try {
				selfs.output.writeUTF("500 Syntax error, command unrecognized");
			} catch (Exception except) {
				System.err.println("Error --> " + except.getMessage());
			}
		} else if (Error == 501) {
			try {
				selfs.output.writeUTF("501 Syntax error in parameters or arguments");
			} catch (Exception except) {
				System.err.println("Error --> " + except.getMessage());
			}
		} else if (Error == 502) {
			try {
				selfs.output.writeUTF("502 Command not implemented");
			} catch (Exception except) {
				System.err.println("Error --> " + except.getMessage());
			}
		} else if (Error == 503) {
			try {
				selfs.output.writeUTF("503 Bad sequence of commands");
			} catch (Exception except) {
				System.err.println("Error --> " + except.getMessage());
			}
		} else if (Error == 504) {
			try {
				selfs.output.writeUTF("504 Command parameter not implemented");
			} catch (Exception except) {
				System.err.println("Error --> " + except.getMessage());
			}
		} 
	}

	/**
	 * Saves the CurrentMessage of the client and stores it in a XML file.
	 */
	public void SaveMessage() {
		Document doc;

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			DocumentBuilder db = dbf.newDocumentBuilder();
			Element root;

			if (file.exists()) {
				doc = db.parse("message.xml");

				root = doc.getDocumentElement();

			} else {
				doc = db.newDocument();

				root = doc.createElement("ALLMESSAGES");
				doc.appendChild(root);
			}

			Element message = doc.createElement("MAIL");
			root.appendChild(message);

			Element from = doc.createElement("FROM");
			from.appendChild(doc.createTextNode(dm.currentMessage.from.toString()));
			message.appendChild(from);

			for (int i = 0; i < dm.currentMessage.toList.size(); i++) {
				Element to = doc.createElement("TO");
				to.appendChild(doc.createTextNode(dm.currentMessage.toList.get(i).toString()));
				message.appendChild(to);
			}

			Element text = doc.createElement("TEXT");
			text.appendChild(doc.createTextNode(dm.currentMessage.message.toString()));
			message.appendChild(text);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("message.xml"));
			transformer.transform(source, result);

		} catch (Exception except) {
			System.err.println("Error --> " + except.getMessage());
		}
	}

	/**
	 * Checks which command the Client sends. Determines the State according to what
	 * the last command was and returns that State.
	 * 
	 * @param message
	 * @param sm
	 * @return
	 */
	private State parse(String message, SocketManager sm) {
		String[] components = message.split(" ");
		String emailAdressRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
		String ipAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
		String domainNameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";

		try {
			if (components.length > 0) {

				if (components[0].toUpperCase().equals("HELO")) {

					if (currentState == State.NONE || currentState == State.QUIT) {

						if (components.length > 1) {

							if ((components[1].matches(ipAddressRegex) || components[1].matches(domainNameRegex))
									&& components[1].length() < 65) {
								selfs.output.writeUTF("250 " + components[1]);
							} else {
								Response(501);
							}

						} else {
							Response(250);
						}
						currentState = State.HELO;

					} else {
						Response(503);
					}
				} else if (components[0].toUpperCase().contains("MAIL")) {

					if (currentState == State.HELO || currentState == State.MSG) {

						dm.currentMessage = new MailMessage();

						String[] parts = components[1].split(":");

						if (parts[0].toUpperCase().equals("FROM")) {

							String firstAngleBracket = parts[1].substring(0, 1);
							String secondAngleBracket = parts[1].substring(parts[1].length() - 1, parts[1].length());

							if (firstAngleBracket.equals("<") && secondAngleBracket.equals(">")) {
								
								String email = parts[1].substring(1, parts[1].length() - 1);
								
								if (email.matches(emailAdressRegex)) {

									dm.currentMessage.from = email;

									Response(250);

									currentState = State.MAIL;
								} else {
									Response(501);
								}
							} else {
								Response(501);
							}

						} else {
							Response(501);
						}

					} else {
						Response(503);
					}
				} else if (components[0].toUpperCase().contains("RCPT")) {

					if (currentState == State.MAIL || currentState == State.RCPT) {

						String[] parts = components[1].split(":");

						if (parts[0].toUpperCase().equals("TO")) {
							
							String firstAngleBracket = parts[1].substring(0, 1);
							String secondAngleBracket = parts[1].substring(parts[1].length() - 1, parts[1].length());

							if (firstAngleBracket.equals("<") && secondAngleBracket.equals(">")) {
								
								String rcpt = parts[1].substring(1, parts[1].length() - 1);
								
								if (rcpt.matches(emailAdressRegex)) {
									dm.currentMessage.toList.add(rcpt);

									Response(250);

									currentState = State.RCPT;
								} else {
									Response(501);
								}
								
							} else {
								Response(501);
							}
							
						} else {
							Response(501);
						}

					} else {
						Response(503);
					}
				} else if (components[0].toUpperCase().equals("DATA")) {

					if (currentState == State.RCPT) {

						if (components.length == 1) {
							Response(354);

							currentState = State.DATA;
						} else {
							Response(501);
						}

					} else {
						Response(503);
					}
				} else if (components[0].equals(".")) {

					if (currentState == State.DATA) {

						if (components.length == 1) {
							Response(250);

							currentState = State.MSG;

							dm.allMail.add(dm.currentMessage);

							SaveMessage();

							selfs.output.writeUTF("250 Message accepted for delivery");
						} else {
							dm.currentMessage.message.add(message);
							currentState = State.DATA;
						}

					} else {
						Response(503);
					}

				} else if (components[0].toUpperCase().equals("QUIT")) {

					if (components.length == 1) {
						Response(221);

						clients.get(0).close();
						clients.remove(0);

						currentState = State.QUIT;
					} else {
						Response(501);
					}

				} else if (components[0].toUpperCase().equals("RSET")) {

					if (currentState == State.MAIL || currentState == State.RCPT || currentState == State.DATA) {
						dm.currentMessage = new MailMessage();

						Response(250);
					}

				} else if (components[0].toUpperCase().equals("NOOP")) {

					Response(250);

				} else if (components[0].toUpperCase().equals("HELP")) {

					if (components.length == 1) {
						Response(214);
					} else {
						Response(504);
					}

				} else if (components[0].toUpperCase().equals("EXPN")) {

					Response(502);

				} else if (components[0].toUpperCase().equals("VRFY")) {

					Response(502);

				} else {

					if (currentState == State.DATA) {
						if (message.contains("..")) {
							String newNewMessage = message.replace("..", ".");
							dm.currentMessage.message.add(newNewMessage);
						}else {
							dm.currentMessage.message.add(message);
						}

						currentState = State.DATA;

					} else {
						Response(500);
					}
				}
			}
			selfs.output.flush();
		} catch (IOException exception) {
			System.err.println("Error --> " + exception.getMessage());
		}
		return currentState;
	}
}

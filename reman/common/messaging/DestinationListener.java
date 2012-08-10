package reman.common.messaging;

import java.util.Hashtable;
import javax.jms.MessageListener;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.jms.JMSException;
import javax.jms.Destination;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.Session;

/**
 * The Destination Listener is responsible for receiving all messages.
 * A Destination Listener simply listens for new messages in a given queue or topic in a separate thread and notifies the application once a message has been received.
 * When the Destination Listener receives a message, the  message type is examined and the correct component is invoked in order to process the message.
 * @author Will
 *
 */
public class DestinationListener extends Thread {
	private String dest_name_;
	private MessageListener handler_;
	private Context context_;
	private Connection connection_;
	private MessageConsumer receiver_;

	/**
	 * Creates a new DestinationListener, which runs in a different thread and notifies the appropriate
	 * application component upon receiving a message.
	 * @param dest The name of the destination (queue or topic) to listen to.
	 * @param ml The actual object that will handle the incoming messages.
	 * @param properties The Hashtable containing the information about the URL of the JMS server.
	 * @param fact_name The name of the ConnectionFactory to listen on.
	 */
	@SuppressWarnings("unchecked")
	public DestinationListener(String dest, MessageListener ml, Context ctx, Connection conn) {
		dest_name_ = dest;
		handler_ = ml;
		context_ = ctx;
		connection_ = conn;
	}

	/**
	 * Begins listening to the specified destination.
	 */
	public void run() {
		Destination dest = null;
		Session session = null;
		try {
			dest = (Destination) context_.lookup(dest_name_);
			session = connection_.createSession(false, Session.AUTO_ACKNOWLEDGE);
			receiver_ = session.createConsumer(dest);
			receiver_.setMessageListener(handler_);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stops the listening to the specified queue or topic. Messages published will not be received after
	 * closing the connection.
	 */
	public void stopListening() {
		if (receiver_ != null) {
			try {
				receiver_.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
}

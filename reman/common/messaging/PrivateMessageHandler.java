package reman.common.messaging;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import reman.client.app.Framework;


/**
 * PrivateMessageHandler is a MessageListener which handles messages that are
 * placed in a user's queue. Currently, this class listens for objects of type ChatMessage
 * and notifies the appropriate application component when a message has been received.
 * @author Will
 *
 */
public class PrivateMessageHandler implements MessageListener {

	/**
	 * This method gets called by the DestinationListener when a message has been
	 * placed in the queue that is being listened to. The appropriate application component
	 * is called from here to react to the message that was received.
	 */
	public void onMessage(Message message) {
		System.out.println("received message");
		if (message instanceof ObjectMessage) {
			try {
				Object o = ((ObjectMessage) message).getObject();
				if (o instanceof ChatMessage) {
					Framework.instance().distributeMesssage((ChatMessage)o);
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}

		}
	}
}

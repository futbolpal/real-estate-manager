package reman.common.messaging;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.JMSException;

import reman.client.app.Framework;
import reman.common.database.UserManager;

/**
 * ProjectMessageHandler is a MessageListener which handles messages that are
 * published to a topic. Currently, this class listens for objects of type NotificationMesssage
 * (and all sub classes of NotificationMesssage) and notifies the appropriate application component 
 * when a message has been received.
 * @author Will
 *
 */
public class ProjectMessageHandler implements MessageListener {

	/**
	 * This method gets called by the DestinationListener when a message has been
	 * published to the topic that is currently being listened to. The appropriate application component
	 * is called from here to react to the message that was received.
	 */
	public void onMessage(Message message) {
		System.out.println("received message");
		if (message instanceof ObjectMessage) {
			try {
				Object o = ((ObjectMessage) message).getObject();
				if (o instanceof NotificationMessage) {
					NotificationMessage msg = (NotificationMessage)o;
					//ignore messages processed by the current user
					if (msg.getSourceUserID() != UserManager.instance().getCurrentUserID())
						Framework.instance().distributeMessage(msg);
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}

		}
	}
}

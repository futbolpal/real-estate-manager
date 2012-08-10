package reman.common.messaging;

import java.net.MalformedURLException;
import javax.jms.JMSException;
import org.exolab.jms.administration.AdminConnectionFactory;
import org.exolab.jms.administration.JmsAdminServerIfc;

/**
 * The MessageAdmin contains definitions to interact with JMS administered object, such as creating
 * queues and topics.
 * @author Will
 *
 */
public abstract class MessageAdmin {

	/**
	 * Creates a queue or topic with the given name on the JMS server, if one of the same name
	 * does not already exist.
	 * @param config Contains information about connecting to the JMS server, such as the ip, port, user name and password needed to
	 * connect to the admin tool.
	 * @param name The name of the queue or topic.
	 * @param isQueue Whether or not the destination being created is a queue. False indicates that the destination is a topic.
	 * @return
	 */
	public static Boolean createDestIfNotExists(JMSConfig config, String name, Boolean isQueue) {
		Boolean outcome;
		try {
			JmsAdminServerIfc admin = AdminConnectionFactory.create(config.getHost(), config
					.getUserName(), config.getPassword());
			if (admin.destinationExists(name)) {
					outcome = true;
			} else {
				if (!admin.addDestination(name, isQueue)) {
					outcome = false;
				} else
					outcome = true;
			}

			admin.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
			outcome = false;
		} catch (JMSException e) {
			e.printStackTrace();
			outcome = false;
		}
		return outcome;
	}

}

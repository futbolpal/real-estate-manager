package reman.common.messaging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import reman.client.app.Framework;
import reman.common.database.MemoryManager;
import reman.common.database.OfficeProjectManager;
import reman.common.database.UserManager;

/**
 * The goal of the Message Manager is to simply send and receive all messages of the system.
 * The Message Manager is the gateway for all components of the application to send messages.
 * The Message Manager also handles registering listeners for each topic and queue that the end user needs to receive messages from.
 * The Message Manager registers listeners for the user's own queue and the user's Office Project topic.
 * For all messages that are being sent, the Message Manager determines the correct destination.
 * Messages will either enter a queue or become published to a topic.
 * @author Will
 *
 */
public class MessageManager {
	private static MessageManager this_;
	private JMSConfig config_;
	@SuppressWarnings("unchecked")
	private Hashtable properties_;
	private ArrayList<DestinationListener> listeners_;
	private Hashtable<String, DestinationInfo> destinations_;

	private ConnectionFactory factory_;
	private Connection connection_;
	private Session session_;
	private Context context_;

	/**
	 * Creates a new instance of MessageManager. The initial context for the JNDI lookup is specified, 
	 * which includes the URL of the context where the JMS administered objects exist.
	 * This constructor is private, because the MessageManager class is a singleton,
	 * only one MessageManager may be created while the application is running.
	 * @param config The JMSConfig passes in the URL for the context, along with the user name and password.
	 * This allows for multiple team members to have different installation of a JMS implementation on different machines.
	 */
	@SuppressWarnings("unchecked")
	private MessageManager(JMSConfig config) {
		listeners_ = new ArrayList<DestinationListener>();
		config_ = config;
		properties_ = new Hashtable();
		properties_.put(Context.INITIAL_CONTEXT_FACTORY, "org.exolab.jms.jndi.InitialContextFactory");
		properties_.put(Context.PROVIDER_URL, config_.getHost());
		properties_.put(Context.SECURITY_PRINCIPAL, config_.getUserName());
		properties_.put(Context.SECURITY_CREDENTIALS, config_.getPassword());
		destinations_ = new Hashtable<String, DestinationInfo>();

		initializeConnection();
	}

	/**
	 * Looks up the name of the ConnectionFactory specified in the JMSConfig using JNDI and creates
	 * a connection for the application to use when sending and receiving messages.
	 */
	private void initializeConnection() {
		if (Framework.MESSAGING_OFF)
			return;
		try {
			context_ = new InitialContext(properties_);
			factory_ = (ConnectionFactory) context_.lookup(config_.getFactoryName());
			connection_ = factory_.createConnection();
			session_ = connection_.createSession(false, Session.AUTO_ACKNOWLEDGE);
			connection_.start();
		} catch (JMSException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is not currently used, but was intended to be available incase we had server side code
	 * that needed to run in addition to the JMS service. This would allow our code to execute
	 * the startup script for JMS.
	 * @return
	 */
	public Boolean startJMSProcess() {
		System.out.println("Starting JMS Server...");
		String[] command = new String[3];
		if (config_.isWindows()) {
			command[0] = "cmd.exe";
			command[1] = "/C";
			command[2] = config_.getStartupScript();
		} else {
			//TODO: make this work on linux
			return false;
		}
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(command);
			//listen for any error messages from the process
			StreamListener errorListener = new StreamListener(proc.getErrorStream(), "Error");
			//same for other general output
			StreamListener outputListener = new StreamListener(proc.getInputStream(), "Output");
			errorListener.start();
			outputListener.start();
			int exitCode = proc.waitFor();
			System.out.println("exit code: " + exitCode);
			return (exitCode == 0);
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}

	/**
	 * Sends a ObjectMessage to the given destination, which can be the name of either a queue or a topic.
	 * @param contents The Java object that is sent to the topic or queue.
	 * @param dest_name The name of the topic or queue.
	 */
	private void send(Serializable contents, String dest_name) {
		Destination dest = null;
		MessageProducer sender = null;

		if (destinations_.contains(dest_name)) {
			DestinationInfo info = destinations_.get(dest_name);
			dest = info.getDestination();
			sender = info.getSender();
		} else {
			try {
				dest = (Destination) context_.lookup(dest_name);
				sender = session_.createProducer(dest);
				destinations_.put(dest_name, new DestinationInfo(sender, dest));
			} catch (NamingException e) {
				e.printStackTrace();
			} catch (JMSException e) {
				e.printStackTrace();
			}

		}

		try {
			//create and send the message
			ObjectMessage outgoingMsg = session_.createObjectMessage();
			outgoingMsg.setObject(contents);
			sender.send(outgoingMsg);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Places an ObjectMessage in the specified user's queue with the specified ChatMessage.
	 * @param uid The recipient user's unique id.
	 * @param msg The message object to send.
	 */
	public void send(Long uid, ChatMessage msg) {
		String dest_name = MessageManager.formatQueueName(uid);
		this.send(msg, dest_name);
	}

	/**
	 * Publishes an Object message to the appropriate topic given the NotificationMessage.
	 * A NotificationMessage belongs to an office project, which is then transfered to all users
	 * on that project.
	 * @param msg The message object to send.
	 */
	public void send(NotificationMessage msg) {
		//immediately process user generated messages
		if (!(msg instanceof ClientStatusMessage))
			Framework.instance().distributeMessage(msg);

		if (Framework.MESSAGING_OFF)
			return;
		
		String dest_name = null;
		//determine message type
		if (msg instanceof ProjectNotificationMessage) {
			ProjectNotificationMessage p = (ProjectNotificationMessage) msg;
			//make sure we're sending the notification to a valid project
			long id = p.getProjectID();
			if (id < 0)
				return;
			dest_name = MessageManager.formatTopicName(id);
		}
		this.send(msg, dest_name);

	}

	/**
	 * Calls the MessageAdmin in order to create a messaging topic for use when sending
	 * messages to the users on the project.
	 * @param id The unique id of the office project.
	 */
	public void createOfficeProject(String version_chain) {
		Long id = MemoryManager.instance().getDatabaseObjectReference(version_chain).getID();
		if (id < 0) {
			System.err.println("Illegal Project ID. version chain = " + version_chain);
			return;
		}
		String topicName = MessageManager.formatTopicName(id);
		MessageAdmin.createDestIfNotExists(config_, topicName, false);
	}

	/**
	 * Calls the MessageAdmin in order to create a messaging queue for use when sending
	 * messages to an individual user.
	 * @param id The unique id of the user.
	 */
	public void createUser(long id) {
		String queueName = MessageManager.formatQueueName(id);
		MessageAdmin.createDestIfNotExists(config_, queueName, true);
	}

	/**
	 * Looks up the current user's unique id and current project id and begins listening to the
	 * user's message queue and subscribes the user to the project's topic in order to receive messages.
	 */
	public void registerListeners() {
		//create a listener for the current user's project messages and one for his/her private messages
		String queueName = MessageManager.formatQueueName(UserManager.instance().getCurrentUserID());
		//get current project id
		String project_version = OfficeProjectManager.instance().getCurrentProject().getVersionChain();
		Long project_id = MemoryManager.instance().getDatabaseObjectReference(project_version).getID();
		if (project_id < 0) {
			System.err
					.println("Error: Current OfficeProject was not retreived before registering the messaging listeners.");
			return;
		}
		String topicName = MessageManager.formatTopicName(project_id);
		System.out.println("Listeneing to topic " + topicName + ", queue " + queueName);

		DestinationListener projectMessageListener = new DestinationListener(topicName,
				new ProjectMessageHandler(), context_, connection_);
		DestinationListener privateMessageListener = new DestinationListener(queueName,
				new PrivateMessageHandler(), context_, connection_);
		projectMessageListener.start();
		privateMessageListener.start();

		listeners_.add(projectMessageListener);
		listeners_.add(privateMessageListener);
	}

	/**
	 * Closes the connection with all queues and topics that the MessageManager
	 * is currently listening to.
	 */
	public void stopListeners() {
		for (DestinationListener d : listeners_) {
			d.stopListening();
		}
	}

	/**
	 * Closes the connection with the JMS server and closes the JNDI context 
	 * that is used to lookup JMS administered objects.
	 */
	public void close() {
		if (context_ != null) {
			try {
				context_.close();
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}
		if (connection_ != null) {
			try {
				connection_.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Given a office project id, this method will return the name of the topic that
	 * memebers of the project are subscribed to. Topic names use the following naming
	 * convention: ProjectTopic_[project id]
	 * @param project The office project's unique id, as assigned by the database.
	 * @return The name of the topic that all project memebers are subscribed to.
	 */
	public static String formatTopicName(Long project) {
		return "ProjectTopic_" + project;
	}

	/**
	 * Given a user's unique id, this method will return the name of the user's private message
	 * queue. Message queues use the following naming convention: UserQueue_[user id]
	 * @param user The user's unique id, as assigned by the database.
	 * @return The name of the queue that the given user is listeneing to.
	 */
	public static String formatQueueName(Long user) {
		return "UserQueue_" + user;
	}

	/**
	 * The MessageManager is a singleton class, meaning there is only one instance of the MessageManager for the application.
	 * This method will return the current instance or create a new one if needed.
	 * @return The current instance of the MessageManager for use by all application components.
	 */
	public static MessageManager instance() {
		if (this_ == null)
			this_ = new MessageManager(JMSConfig.WILL());
		return this_;
	}
}

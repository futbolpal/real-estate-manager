package reman.common.messaging;

/**
 * JMSConfig is a class that holds the host url, user name, and password needed to connect
 * to the JMS server.
 * @author Will
 *
 */
public class JMSConfig {
	private String host_;
	private String user_;
	private String password_;
	private String filePath_;
	private Boolean windows_;

	public JMSConfig(String host, String user, String pass, String path, Boolean isWindows) {
		host_ = host;
		user_ = user;
		password_ = pass;
		filePath_ = path;
		windows_ = isWindows;
	}

	/**
	 * Gets the name of the connection factory that is configured in the following file on the JMS server:
	 * [JMS install dir]/config/openjms.xml
	 * @return The name of the connection factory that can be used for a JNDI lookup.
	 */
	public String getFactoryName() {
		return "RemanConnection";
	}

	/**
	 * Returns the name of the queue that is preconfigured in [JMS install dir]/config/openjms.xml.
	 * This queue is intended to be used to pass information to the server portion of the application,
	 * such as handshakes, or other system messages.
	 * @return The name of the queue that can be used for a JNDI lookup.
	 */
	public String getSystemQueueName() {
		return "SystemMsg";
	}

	/**
	 * Gets the IP address/host name of the machine that runs the JMS server.
	 * @return The host address.
	 */
	public String getHost() {
		return host_;
	}

	/**
	 * Gets the user name that is preconfigured in [JMS install dir]/config/openjms.xml.
	 * @return The user name used to log in to the messaging service.
	 */
	public String getUserName() {
		return user_;
	}

	/**
	 * Gets the password that is preconfigured in [JMS install dir]/config/openjms.xml.
	 * @return The password used to log in to the messaging service.
	 */
	public String getPassword() {
		return password_;
	}

	/**
	 * Gets the file path of the install directory on the machine that is running the JMS service.
	 * This is not currently used, but was intended to be available incase we had server side code
	 * that needed to run in addition to the JMS service. This would allow our code to get the location
	 * of the startup script for JMS.
	 * @return The path to the OpenJMS bin directory 
	 */
	public String getFilePath() {
		return filePath_;
	}

	/**
	 * Gets whether or not the JMS service is running on a Windows machine or not.
	 * This is not currently used, but was intended to be available incase we had server side code
	 * that needed to run in addition to the JMS service. This would affect the code that actually
	 * calls the startup script, as there are different scripts for Windows and Unix.
	 * @return True if the server is running Windows, false otherwise.
	 */
	public Boolean isWindows() {
		return windows_;
	}

	/**
	 * Gets the file path of the script needed to start up OpenJMS. Again, this is not currently used.
	 * @return
	 */
	public String getStartupScript() {
		if (windows_) {
			return filePath_ + "openjms start";
		} else {
			//TODO
			return "";
		}
	}

	/**
	 * Creates a JMSConfig that has all of the information needed to connect to the OpenJMS
	 * service that is running on Will's computer,
	 * @return The connection information.
	 */
	public static JMSConfig WILL() {
		return new JMSConfig("tcp://137.99.178.115:3035/", "RemanUser", "Jj14nb3e!",
				"C:\\Users\\Administrator\\Documents\\Java\\openjms-0.7.7-beta-1\\bin\\", true);
	}

}

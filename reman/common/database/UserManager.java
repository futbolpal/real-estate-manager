package reman.common.database;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import reman.client.app.Framework;
import reman.client.app.OfficeProject;
import reman.client.app.listeners.ClientStatusListener;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.LoggedInException;
import reman.common.messaging.ClientStatusMessage;
import reman.common.messaging.MessageManager;
import reman.common.messaging.ClientStatusMessage.UserStatus;

/**
 * This class manages a user table for all of the users in the system.  Each user
 * is associated with an office project.  This creates a group of users that are
 * able to interact with each  other.  Furthermore, an access level and an access group
 * can be used to extend or restrict functionality.  
 * <BR><BR>
 * This class is also responsible for maintaining a list of current users that
 * are logged in and share the same project as the current user.  
 * @author jonathan
 *
 */
public class UserManager extends DatabaseObject implements ClientStatusListener {

	private static transient UserManager this_;

	/*Database Fields*/
	protected String username_;
	protected String password_;
	protected String display_name_;
	protected long access_level_;
	protected long access_group_;
	protected OfficeProject office_project_;
	protected UserStatus status_;
	protected Timestamp last_;

	/* Temporary Fields */
	private transient long current_uid_;
	private transient UserInfo current_info_;
	private transient ArrayList<UserInfo> user_table_;

	private UserManager() {
		current_uid_ = -1;
		user_table_ = new ArrayList<UserInfo>();
	}

	/**
	 * This function creates a new user in the database.  A user may be creating a new office or
	 * joining  an already existing office.  
	 * @param username - the username of the new user
	 * @param password - the plaintext password of the new user
	 * @param display - a display name that the user will use
	 * @param pcode - the project code for a new or existing office
	 * @param new_office - true if the office is new, false if the office exists
	 * @return true if the user was successfully created
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws LoggedInException
	 */
	public boolean register(String username, String password, String display, String pcode,
			boolean new_office) throws SQLException, IllegalArgumentException, IllegalAccessException,
			ClassNotFoundException, SecurityException, NoSuchFieldException, LoggedInException {
		/*Check the username */
		String check_username = "SELECT * FROM " + this.getTableName() + " WHERE username_=MD5('"
				+ username + "')";
		ResultSet rs = DatabaseManager.executeQuery(check_username);
		if (DatabaseManager.getRowCount(rs) != 0)
			return false;

		String version_chain = "0";
		if (new_office) {
			//create entry in officeprojectmanager
			OfficeProjectManager.instance().registerProject(pcode);
		} else {
			//check project code
			version_chain = OfficeProjectManager.instance().checkProjectCode(pcode);
		}

		if (version_chain == null)
			return false;

		/*Register this user to that project*/
		String register = "INSERT INTO " + this.getTableName()
				+ " (username_,password_,display_name_,status_,office_project_) VALUES (MD5('" + username
				+ "'),PASSWORD('" + username + password + "'),'" + display + "','"
				+ UserStatus.LOGGED_OUT.name() + "','" + version_chain + "')";

		ResultSet results = DatabaseManager.executeUpdate(register).getGeneratedKeys();
		/*get the newly inserted user's id to create a messaging queue*/
		results.first();
		long prim_key = results.getLong(1);
		MessageManager.instance().createUser(prim_key);
		return true;
	}

	/**
	 * This function logs in a user when they sign in.  
	 * <BR><BR>
	 * When a new project is created, it cannot be saved to the database until the user is logged in. 
	 * As a result, this function must commit a new project, or load the existing project.  When a user 
	 * logs in, the time of last login and the status of the user are updated.  
	 * @param username
	 * @param password
	 * @return
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	public boolean login(String username, String password) throws SQLException, DatabaseException {

		String sql = "SELECT * FROM " + this.getTableName() + " WHERE username_=MD5('" + username
				+ "') AND password_=PASSWORD('" + username + password + "') ";// +			"AND status_='"+ UserStatus.LOGGED_OUT.name() + "'";
		ResultSet rs = DatabaseManager.executeQuery(sql);

		if (DatabaseManager.getRowCount(rs) == 1) {
			current_uid_ = rs.getLong("ID");
			if (OfficeProjectManager.instance().isNew()) {
				/*NEW PROJECT - Save project and update project id */
				OfficeProject p = OfficeProjectManager.instance().getCurrentProject();
				p.setChangeBroadCasted(false);//prevent this commit from broadcasting, because the project id has not been determined
				p.commit();
				p.setChangeBroadCasted(true);
				rs.updateString("office_project_", p.getVersionChain());
				//create a messaging topic for the project
				MessageManager.instance().createOfficeProject(p.getVersionChain());
			} else {
				/*LOAD PROJECT */
				OfficeProjectManager.instance().loadProject(rs.getString("office_project_"));
			}

			rs.updateString("status_", UserStatus.LOGGED_IN.name());
			rs.updateTimestamp("last_", new Timestamp(new Date().getTime()));
			rs.updateRow();
			return true;
		} else {
			return false;
		}
	}

	/** 
	 * This function will log out the current user.  
	 * @throws SQLException
	 */
	public void logout() throws SQLException {
		String sql = "SELECT * FROM " + this.getTableName() + " WHERE ID=" + current_uid_;

		ResultSet rs = DatabaseManager.executeQuery(sql);
		if (DatabaseManager.getRowCount(rs) > 0) {
			rs.updateString("status_", UserStatus.LOGGED_OUT.name());
			rs.updateRow();
		}

		/* Reset variables */
		this.current_uid_ = -1;
		this.current_info_ = null;
		this.user_table_ = null;
	}

	public void setChatServer(InetAddress i, int port) {
		InetAddress[] ips;
		try {
			ips = InetAddress.getAllByName(i.getHostName());
			for (InetAddress ip : ips) {
				System.out.println(ip);
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/* update the table to include the address and port of the user for chat */
	}

	/**
	 * This function will refresh the contents of the list of active users.  
	 * @throws SQLException
	 */
	public void refreshUserTable() throws SQLException {
		this.user_table_.clear();

		String sql = "SELECT * FROM " + this.getTableName() + " WHERE status_<>'"
				+ UserStatus.LOGGED_OUT.name() + "' AND office_project_='"
				+ OfficeProjectManager.instance().getCurrentProject().getVersionChain() + "'";
		ResultSet rs = DatabaseManager.executeQuery(sql);

		if (DatabaseManager.getRowCount(rs) > 0) {
			do {
				UserInfo ui = getUserInfo(rs.getLong("ID"));
				if (ui != null) {
					this.user_table_.add(getUserInfo(rs.getLong("ID")));
				}
			} while (rs.next());
		}
	}

	/**
	 * This function returns the list of users online.  It MAY be out of date and does not 
	 * call the refreshUserTable before returning it.  To retrieve the latest, the two functions
	 * must be explicitly called one after the other.   
	 * @return ArrayList of users online
	 */
	public ArrayList<UserInfo> getUsersOnline() {
		return this.user_table_;
	}

	/**
	 * This function returns a UserInfo object for the specified user id. 
	 * @param uid - the id of the user to retrieve info for
	 * @return a UserInfo object with the users information
	 * @throws SQLException
	 */
	public UserInfo getUserInfo(long uid) throws SQLException {

		String sql = "SELECT * FROM " + this.getTableName() + " WHERE ID=" + uid;

		ResultSet rs = DatabaseManager.executeQuery(sql);

		if (DatabaseManager.getRowCount(rs) == 1) {
			UserInfo ui = new UserInfo();
			ui.id_ = rs.getLong("ID");
			ui.access_group_ = rs.getLong("access_group_");
			ui.access_level_ = rs.getLong("access_level_");
			ui.last_ = rs.getTimestamp("last_");
			ui.display_name_ = rs.getString("display_name_");
			ui.status_ = UserStatus.valueOf(rs.getString("status_"));
			return ui;
		} else
			return null;
	}

	/**
	 * Returns the ID of the current user
	 * @return
	 */
	public long getCurrentUserID() {
		return current_uid_;
	}

	/**
	 * Returns the UserInfo object for the current user
	 * @return
	 * @throws SQLException
	 */
	public UserInfo getCurrentUserInfo() throws SQLException {
		if (current_info_ == null)
			current_info_ = getUserInfo(current_uid_);
		return current_info_;
	}

	public boolean isLoggedIn() {
		return current_uid_ >= 0;
	}

	public void assertLoggedIn() throws LoggedInException {
		if (!this.isLoggedIn())
			throw new LoggedInException();
	}

	@Override
	public long commit() {
		System.err.println("Cannot commit this class to the database");
		return -1;
	}

	@Override
	public void retrieve() {
		System.err.println("Cannot retreive this from the database");
	}

	public void clientStatusChanged(ClientStatusMessage m) {
		try {
			this.refreshUserTable();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static UserManager instance() {
		if (this_ == null) {
			this_ = new UserManager();
			Framework.instance().addClientStatusListener(this_);
		}
		return this_;

	}

	/**
	 * This class is used to hold a row of information from the UserManager table. 
	 * @author jonathan
	 *
	 */
	public final class UserInfo extends UserManager {
		private Long id_;

		public Long getAccessGroup() {
			return this.access_group_;
		}

		public Long getAccessLevel() {
			return this.access_level_;
		}

		public String getDisplayName() {
			return this.display_name_;
		}

		public Timestamp getLastLogin() {
			return this.last_;
		}

		public long getID() {
			return this.id_;
		}

		public UserStatus getStatus() {
			return this.status_;
		}
	}
}

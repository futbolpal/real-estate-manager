package reman.client.app;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import reman.common.database.DatabaseManager;
import reman.common.database.DatabaseObject;
import reman.common.database.UserManager;
import reman.common.database.exceptions.DatabaseException;

/**
 * UserDatabaseObject is a DBO that is personal in the sense that no
 * other user in any project is able to access it.  They are handles differently 
 * than normal database objects and are list oriented.  Namely, all UDBOs of a certain
 * type can be retrieved via the load command and are placed in an array list.  
 * 
 * @author jonathan
 */
public abstract class UserDatabaseObject extends DatabaseObject {
	private long user_id_;

	public UserDatabaseObject() {
		user_id_ = UserManager.instance().getCurrentUserID();
		this.setChangeBroadCasted(false);
	}

	/**
	 * This function will retrieve a list of UDBOs for a class of type 'type' that are associated
	 * with the current user.  
	 * 
	 * @param type - the type of the object to retrieve
	 * @return a list of objects of a class that extends UDBO
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws DatabaseException
	 */
	public static ArrayList<? extends UserDatabaseObject> load(
			Class<? extends UserDatabaseObject> type) throws SQLException, InstantiationException,
			IllegalAccessException, DatabaseException {
		if (!DatabaseManager.isDatabaseObject(type))
			return null;

		/* Ensure database is prepared for this type */
		type.newInstance();

		/* Retrieve objects, filter old ones */
		ArrayList<Long> discards = new ArrayList<Long>();
		Hashtable<Long, UserDatabaseObject> temp_list = new Hashtable<Long, UserDatabaseObject>();
		String sql = "SELECT ID FROM " + DatabaseManager.getTableName(type);
		ResultSet rs = DatabaseManager.executeQuery(sql);
		if (DatabaseManager.getRowCount(rs) > 0) {
			do {
				long id = rs.getLong(1);
				UserDatabaseObject o = (UserDatabaseObject) type.newInstance();
				o.retrieve(type,id);
				o.retrieve();
				if (o.user_id_ == UserManager.instance().getCurrentUserID()) {
					discards.add(o.getPreviousVersionID());
					temp_list.put(o.getID(), o);
				}
			} while (rs.next());
		}

		/* Remove deprecated values */
		for (Long previous_id : discards) {
			if (previous_id < 0)
				continue;
			temp_list.remove(previous_id);
		}

		/* We must filter out old objects */
		ArrayList<UserDatabaseObject> list_ret = new ArrayList<UserDatabaseObject>();
		list_ret.addAll(temp_list.values());
		return list_ret;
	}
}

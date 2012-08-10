package reman.client.basetypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import reman.common.database.DatabaseManager;
import reman.common.database.DatabaseObject;
import reman.common.database.UserManager;
import reman.common.database.exceptions.DatabaseException;

public abstract class UserDatabaseObject extends DatabaseObject {
	private long user_id_;

	public UserDatabaseObject() {
		user_id_ = UserManager.instance().getCurrentUserID();
		this.setChangeBroadCasted(false);
	}

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
				o.setID(id);
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

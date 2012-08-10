package reman.client.app.preferences;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import reman.common.database.DatabaseManager;
import reman.common.database.DatabaseObject;
import reman.common.database.UserManager;
import reman.common.database.exceptions.DatabaseException;

/**
 * GlobalPreferences are DBOs that can be retrieved all together.  For example, consider
 * the case study of a "Category".  Categories may be added over time by users on the system
 * because they are not known at development time.  Using GlobalPreferences, Categories of a certain
 * type can easily be retrieved.  
 * <BR><BR>
 * In a sense, these are  static lists that can be added to over time.  
 * @author jonathan
 */
public abstract class GlobalPreference extends DatabaseObject {
	public GlobalPreference() {
	}

	/**
	 * This function will retrieve a list of GPs for a class of type 'type'.  
	 * @param type - the type of the object to retrieve
	 * @return a list of objects of a class that extends GP
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws DatabaseException
	 */
	public static ArrayList<? extends GlobalPreference> load(Class<? extends GlobalPreference> type)
			throws SQLException, InstantiationException, IllegalAccessException, DatabaseException {
		if (!DatabaseManager.isDatabaseObject(type))
			return null;

		/* Ensure database is prepared for this type */
		type.newInstance();

		/* Retrieve objects, filter old ones */
		Hashtable<Long, GlobalPreference> list = new Hashtable<Long, GlobalPreference>();
		String sql = "SELECT ID FROM " + DatabaseManager.getTableName(type);
		ResultSet rs = DatabaseManager.executeQuery(sql);
		if (DatabaseManager.getRowCount(rs) > 0) {
			do {
				long id = rs.getLong(1);
				GlobalPreference o = (GlobalPreference) type.newInstance();
				o.retrieve(type, id);
				o.retrieve();
				list.put(o.getID(), o);
			} while (rs.next());
		}

		/* We must filter out old objects */
		ArrayList<GlobalPreference> list_ret = new ArrayList<GlobalPreference>();
		list_ret.addAll(list.values());
		return list_ret;
	}
}

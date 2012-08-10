package reman.common.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import reman.common.database.exceptions.LoggedInException;

/**
 * This class performs the hard work in locking and unlocking objects.  It is separated from 
 * DatabaseObject because it is "added" functionality on original DBOs.  
 * <BR><BR>
 * The default lock time is currently 5 minutes.  After this time, the lock is expired.  
 * <BR><BR>
 * This class manages a separate table to handle locks.  There are 4 columns.  2 columns are used
 * to identify the object being locked.  One column is used to determine who created the lock.  The last
 * column is the time the lock was acquired.    
 * @author jonathan
 *
 */
public final class LockManager extends DatabaseObject {
	private transient static LockManager this_;

	private transient static final int LOCKTIME_MINUTES = 5;

	/* Table Fields */
	private String class_;
	private String chain_id_;
	private long user_id_;
	private Timestamp acquired_;

	private LockManager() {
		// clean lock table?
	}

	/**
	 * This function determines whether the object is locked
	 * @param o The object to be tested
	 * @return
	 * True if the object is locked by another user
	 * False if the object is not locked OR the current owner owns the lock
	 * @throws SQLException
	 * @throws LoggedInException 
	 */
	public boolean isLocked(DatabaseObject o) throws SQLException, LoggedInException {
		String sql = "SELECT * FROM " + this.getTableName() + " WHERE class_='" + o.getTableName()
				+ "' AND chain_id_='" + o.getVersionChain() + "' AND DATE_SUB(NOW(),INTERVAL "
				+ LOCKTIME_MINUTES + " MINUTE) < acquired_ AND user_id_ <> "
				+ UserManager.instance().getCurrentUserID();
		ResultSet rs = DatabaseManager.executeQuery(sql);
		if (DatabaseManager.getRowCount(rs) > 0)
			return true;
		return false;
	}

	/**
	 * This function unlocks the object 
	 * @param o - object to unlock
	 * @return
	 * True if, when the method completes, the object is unlocked
	 * False if, when the method completes, the object is locked
	 * @throws SQLException
	 * @throws LoggedInException 
	 */
	public boolean unlock(DatabaseObject o) throws SQLException, LoggedInException {
		String delete_lock = "DELETE FROM " + this.getTableName() + " WHERE class_='"
				+ o.getTableName() + "' AND chain_id_='" + o.getVersionChain() + "' AND user_id_='"
				+ UserManager.instance().getCurrentUserID() + "'";
		DatabaseManager.executeUpdate(delete_lock).getUpdateCount();
		return !isLocked(o);
	}

	/**
	 * This function tries to place a lock on an object
	 * @param o - object to lock
	 * @return
	 * True if, when the method completes, the object is locked by this user
	 * False if, when the method completes, the object is not locked by this user
	 * @throws SQLException
	 * @throws LoggedInException 
	 */
	public boolean lock(DatabaseObject o) throws SQLException, LoggedInException {
		/* Check that the user is logged in */
		if (!UserManager.instance().isLoggedIn()) {
			throw new LoggedInException();
		}

		if (!isLocked(o)) {
			String fields = "DEFAULT,-1,'" + o.getTableName() + "','" + o.getVersionChain() + "',"
					+ UserManager.instance().getCurrentUserID() + ",NOW()";
			String sql = "INSERT INTO " + this.getTableName() + " values (" + fields + ")";
			DatabaseManager.executeUpdate(sql);
			return true;
		} else
			return false;
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

	@Override
	public boolean lock() {
		System.err.println("Cannot lock this object");
		return false;
	}

	@Override
	public boolean isLocked() {
		System.err.println("Cannot lock this object");
		return false;
	}

	@Override
	public boolean unlock() {
		System.err.println("Cannot lock this object");
		return false;
	}

	public static LockManager instance() {
		if (this_ == null)
			this_ = new LockManager();
		return this_;
	}
}

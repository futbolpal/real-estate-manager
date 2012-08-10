package reman.common.database;

import java.util.Hashtable;

/**
 * This Manager was created to avoid every managed class maintaining its own independent managed field list.  There should
 * only be one central managed field list per dbo Class type.
 * @author Scott
 *
 */
public class ManagedClassManager {

	private static transient ManagedClassManager this_;

	/**
	 * Indexed by class name
	 */
	private Hashtable<String, ManagedClassRecord> managed_classes_;

	private ManagedClassManager() {
		this.managed_classes_ = new Hashtable<String, ManagedClassRecord>();

		/*Equation e = new Equation("x");
		Account a = new Account(null,"",null,AcctType.ASSET,);*/
	}
	

	public boolean isSingleton() {
		return true;
	}

	public static ManagedClassManager instance() {
		if (this_ == null)
			this_ = new ManagedClassManager();
		return this_;
	}

	/**
	 * Will obtain the managed class record for the param 'dbo_class'.  Will return null if the class is invalid.
	 * @param dbo_class
	 * @return
	 */
	public ManagedClassRecord getManagedClassRecord(Class<? extends DatabaseObject> dbo_class) {
		if (this.managed_classes_.containsKey(dbo_class.getName())
				&& this.managed_classes_.get(dbo_class.getName()).isValidRecord())
			return this.managed_classes_.get(dbo_class.getName());

		ManagedClassRecord mcr = new ManagedClassRecord(dbo_class);
		if (mcr.isValidRecord()) {
			this.managed_classes_.put(dbo_class.getName(), mcr);
			return mcr;
		}

		return null;
	}

	/**
	 * Manually provided recursive lock, can't use ManagedDatabaseObject because ManagedDatabaseObject depends upon this class
	 * @throws LoggedInException 
	 */
	/*@Override
	public boolean lock() throws SQLException, LoggedInException {
		boolean valid_lock = true;
		for (ManagedClassRecord mcr : this.managed_classes_.values()) {
			if (!mcr.lock()) {
				valid_lock = false;
				break;
			}
		}

		if (valid_lock && super.lock())
			return true;

		this.unlock();
		return false;
	}*/

	/**
	 * Manually provided recursive unlock, can't use ManagedDatabaseObject because ManagedDatabaseObject depends upon this class
	 * @throws LoggedInException 
	 */
	/*@Override
	public boolean unlock() throws SQLException, LoggedInException {
		boolean valid_unlock = true;
		for (ManagedClassRecord mcr : this.managed_classes_.values()) {
			if (!mcr.unlock())
				valid_unlock = false;
		}

		if (super.unlock() && valid_unlock)
			return true;
		return false;
	}*/

	/**
	 * Manually provided recursive retrieve, can't use ManagedDatabaseObject because ManagedDatabaseObject depends upon this class
	 * @throws DatabaseException 
	 */
	/*@Override
	public void retrieve() throws SQLException, DatabaseException {
		for (ManagedClassRecord mcr : this.managed_classes_.values())
			mcr.retrieve();
		super.retrieve();
	}*/

	/**
	 * Manually provided recursive commit, can't use ManagedDatabaseObject because ManagedDatabaseObject depends upon this class
	 * @throws DatabaseException 
	 */
	/*@Override
	public long commit() throws SQLException, DatabaseException {
		for (ManagedClassRecord mcr : this.managed_classes_.values())
			if (mcr.commit() < 0)
				return -1;
		return super.commit();
	}*/
}

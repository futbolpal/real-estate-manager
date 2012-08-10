package reman.common.database;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctBalanceSystem;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.LoggedInException;
import reman.common.database.exceptions.NotPersistedException;

/**
 * Attempt to automate retrieve/lock/commit/unlock process for DatabaseObjects which contain other DatabaseObjects
 * and must be treated atomically with respect to database actions.  Also support to automate collections/maps of DatabaseObjects.
 * 
 * IMPORTANT: This class can only be used for one level of ManagedDatabaseObject per hierarchy. Example: If a class B inherits from 
 * a ManagedDatabaseObject inheriting class A, class B can not have managed fields via ManagedDatabaseObject.
 * @author Scott
 *
 */
public abstract class ManagedDatabaseObject extends DatabaseObject {

	/**
	 * DatabaseObject use only
	 */
	protected ManagedDatabaseObject() {
	}

	/**
	 * Each field name (if found) will be managed and considered atomic with the object they reside in (with respect to the database).
	 * @param managed_field_names Each field must refer to an object that extends DatabaseObject, a collection that contains objects that
	 * 													extends DatabaseObject, or a map where the value type extends DatabaseObject.  All other fields will be ignored.
	 */
	public ManagedDatabaseObject(String[] managed_field_names) {
		ManagedClassRecord mcr = ManagedClassManager.instance().getManagedClassRecord(this.getClass());
		if (mcr != null) {
			for (String s : managed_field_names) {
				Field f = null;
				try {
					f = this.getManagedClass().getDeclaredField(s);
				} catch (SecurityException e) {
					/*permission shouldn't be set*/
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					/*invalid field name passed, don't manage this field*/
					f = null;
					e.printStackTrace();
				}
				if (f != null)
					mcr.addManagedField(f);

			}
		}
	}

	/**
	 * This is to solve problem if a class inherits from a class that inherits from ManagedDatabaseObject (or ManagedLCDatabaseObject)
	 * @return
	 */
	private Class<? extends ManagedDatabaseObject> getManagedClass() {
		Class curr_class = this.getClass();
		while (curr_class.getSuperclass() != ManagedLCDatabaseObject.class
				&& curr_class.getSuperclass() != ManagedDatabaseObject.class) {
			curr_class = curr_class.getSuperclass();
		}
		return curr_class;
	}

	private ManagedDatabaseObject getManagedInstance() {
		return this.getManagedClass().cast(this);
	}

	private Object getFieldObject(Field f) throws IllegalAccessException {
		try {
			f.setAccessible(true);
			return f.get(this.getManagedInstance());
		} catch (IllegalArgumentException e) {
			/*TODO: this field could be removed from the class, remove from the list*/
			this.removeManagedField(f);
			System.err.println("Field '" + f + "' not found in this class '" + this.getManagedClass()
					+ "', and was removed from managed list.");
			e.printStackTrace();
		}
		return null;
	}

	private boolean lock(DatabaseObject dbo) throws SQLException, LoggedInException {
		if (dbo != null)
			return dbo.lock();
		return true;
	}

	private boolean removeManagedField(Field f) {
		ManagedClassRecord mcr = ManagedClassManager.instance().getManagedClassRecord(
				this.getManagedClass());
		if (mcr == null)
			return false;
		return mcr.removeMangedField(f);
	}

	private ArrayList<Field> getManagedFields() {
		ManagedClassRecord mcr = ManagedClassManager.instance().getManagedClassRecord(
				this.getManagedClass());
		if (mcr == null)
			return null;
		return mcr.getManagedFields();
	}

	/**
	 * Managed lock. Will acquire a lock on all managed fields, or none.
	 * @return True if all fields were successfully locked.
	 * @throws SQLException
	 * @throws LoggedInException 
	 */
	@Override
	public boolean lock() throws SQLException, LoggedInException {
		boolean valid_lock = true;

		try {
			ArrayList<Field> managed_fields = this.getManagedFields();
			for (Field f : managed_fields) {
				if (DatabaseManager.isDatabaseObject(f.getType())) {

					if (!this.lock((DatabaseObject) this.getFieldObject(f))) {
						valid_lock = false;
						break;
					}

				} else if (DatabaseManager.isCollection(f.getType())) {
					Collection col = (Collection) this.getFieldObject(f);
					if (col != null) {
						Class list_type = DatabaseManager.getGenericClasses(f)[0];
						if (DatabaseManager.isDatabaseObject(list_type)) {
							Collection<DatabaseObject> dbo_col = (Collection<DatabaseObject>) col;
							for (DatabaseObject dbo : dbo_col) {
								if (!this.lock(dbo)) {
									valid_lock = false;
									break;
								}
							}
						}
					}
				} else if (DatabaseManager.isMap(f.getType())) {
					Map map = (Map) this.getFieldObject(f);
					if (map != null) {
						Class[] types = DatabaseManager.getGenericClasses(f);
						Class keyType = types[0];
						Class valueType = types[1];
						if (DatabaseManager.isDatabaseObject(valueType)) {
							Map<Object, DatabaseObject> dbo_map = (Map<Object, DatabaseObject>) map;
							for (DatabaseObject dbo : dbo_map.values()) {
								if (!this.lock(dbo)) {
									valid_lock = false;
									break;
								}
							}
						}
					}
				} else {
					/*TODO: unsupported type*/
					this.removeManagedField(f);
				}
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (valid_lock && super.lock())
			return true;

		this.unlock();/*TODO: is it ok in general to unlock after a failure to lock?*/
		return false;
	}

	private boolean unlock(DatabaseObject dbo) throws SQLException, LoggedInException {
		if (dbo != null)
			return dbo.unlock();
		return true;
	}

	/**
	 * Managed unlock.  Unlock all managed fields.
	 * @return True if all fields were successfully unlocked.
	 * @throws SQLException
	 * @throws LoggedInException 
	 */
	@Override
	public boolean unlock() throws SQLException, LoggedInException {
		boolean valid_unlock = true;

		try {
			ArrayList<Field> managed_fields = this.getManagedFields();
			for (Field f : managed_fields) {
				if (DatabaseManager.isDatabaseObject(f.getType())) {
					if (!this.unlock((DatabaseObject) this.getFieldObject(f))) {
						valid_unlock = false;
					}
				} else if (DatabaseManager.isCollection(f.getType())) {
					Collection col = (Collection) this.getFieldObject(f);
					if (col != null) {
						Class list_type = DatabaseManager.getGenericClasses(f)[0];
						if (DatabaseManager.isDatabaseObject(list_type)) {
							Collection<DatabaseObject> dbo_col = (Collection<DatabaseObject>) col;
							for (DatabaseObject dbo : dbo_col) {
								if (!this.unlock(dbo))
									valid_unlock = false;
							}
						}
					}
				} else if (DatabaseManager.isMap(f.getType())) {
					Map map = (Map) this.getFieldObject(f);
					if (map != null) {
						Class[] types = DatabaseManager.getGenericClasses(f);
						Class keyType = types[0];
						Class valueType = types[1];
						if (DatabaseManager.isDatabaseObject(valueType)) {
							Map<Object, DatabaseObject> dbo_map = (Map<Object, DatabaseObject>) map;
							for (DatabaseObject dbo : dbo_map.values()) {
								if (!this.unlock(dbo))
									valid_unlock = false;
							}
						}
					}
				} else {
					/*TODO: unsupported type*/
					this.removeManagedField(f);
				}
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (super.unlock() && valid_unlock)
			return true;
		return false;
	}

	private void retrieve(DatabaseObject dbo) throws DatabaseException, SQLException {
		if (dbo != null)
			dbo.retrieve();
	}

	/**
	 * Managed retrieve.  Attempt to retrieve all managed fields.
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	@Override
	public void retrieve() throws SQLException, DatabaseException {

		try {
			ArrayList<Field> managed_fields = this.getManagedFields();
			for (Field f : managed_fields) {
				if (DatabaseManager.isDatabaseObject(f.getType())) {
					try {
						this.retrieve((DatabaseObject) this.getFieldObject(f));
					} catch (NotPersistedException e) {
						/*one item not persisted should not stop the entire managed dbo from being retrieved*/
					}
				} else if (DatabaseManager.isCollection(f.getType())) {
					Collection col = (Collection) this.getFieldObject(f);
					if (col != null) {
						Class list_type = DatabaseManager.getGenericClasses(f)[0];
						if (DatabaseManager.isDatabaseObject(list_type)) {
							Collection<DatabaseObject> dbo_col = (Collection<DatabaseObject>) col;
							for (DatabaseObject dbo : dbo_col) {
								try {
									this.retrieve(dbo);
								} catch (NotPersistedException e) {
									/*one item not persisted should not stop the entire managed dbo from being retrieved*/
								}
							}
						}
					}
				} else if (DatabaseManager.isMap(f.getType())) {
					Map map = (Map) this.getFieldObject(f);
					if (map != null) {
						Class[] types = DatabaseManager.getGenericClasses(f);
						Class keyType = types[0];
						Class valueType = types[1];
						if (DatabaseManager.isDatabaseObject(valueType)) {
							Map<Object, DatabaseObject> dbo_map = (Map<Object, DatabaseObject>) map;
							for (DatabaseObject dbo : dbo_map.values()) {
								try {
									this.retrieve(dbo);
								} catch (NotPersistedException e) {
									/*one item not persisted should not stop the entire managed dbo from being retrieved*/
								}
							}
						}
					}
				} else {
					/*TODO: unsupported type*/
					this.removeManagedField(f);
				}
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		super.retrieve();
	}

	private long commitInternal(DatabaseObject dbo) throws SQLException, DatabaseException {
		if (dbo != null)
			return dbo.commit();
		return 0;/*TODO: if a managed element is null, is that assumed as success (>=0)?*/
	}

	/**
	 * Managed commit.  Attempt to commit all managed fields.
	 * @return (>=0) key of this object if all fields successfully committed.  (<0) if any field is not successfully committed.
	 */
	@Override
	public long commit() throws SQLException, DatabaseException {
		/*TODO: should one failure to commit stop the rest of the commits from occurring?*/
		//System.err.println("Begining of commit for class '"+this.getManagedClass()+"'.");
		try {
			ArrayList<Field> managed_fields = this.getManagedFields();
			for (Field f : managed_fields) {

				//System.err.println("Committing field '"+f+"' from class '"+this.getManagedClass()+"'.");
				if (DatabaseManager.isDatabaseObject(f.getType())) {
					if (this.commitInternal((DatabaseObject) this.getFieldObject(f)) < 0)
						return -1;
				} else if (DatabaseManager.isCollection(f.getType())) {
					Collection col = (Collection) this.getFieldObject(f);
					if (col != null) {
						Class list_type = DatabaseManager.getGenericClasses(f)[0];
						if (DatabaseManager.isDatabaseObject(list_type)) {
							Collection<DatabaseObject> dbo_col = (Collection<DatabaseObject>) col;
							for (DatabaseObject dbo : dbo_col) {
								if (this.commitInternal(dbo) < 0)
									return -1;
							}
						}
					}
				} else if (DatabaseManager.isMap(f.getType())) {
					Map map = (Map) this.getFieldObject(f);
					if (map != null) {
						Class[] types = DatabaseManager.getGenericClasses(f);
						Class keyType = types[0];
						Class valueType = types[1];
						if (DatabaseManager.isDatabaseObject(valueType)) {
							Map<Object, DatabaseObject> dbo_map = (Map<Object, DatabaseObject>) map;
							for (DatabaseObject dbo : dbo_map.values()) {
								if (this.commitInternal(dbo) < 0)
									return -1;
							}
						}
					}
				} else {
					/*TODO: unsupported type*/
					this.removeManagedField(f);
				}
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return super.commit();
	}
}

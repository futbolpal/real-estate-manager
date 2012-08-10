package reman.common.database;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import reman.client.app.Framework;
import reman.common.database.UserManager.UserInfo;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.DatabaseObjectException;
import reman.common.database.exceptions.LockedException;
import reman.common.database.exceptions.LoggedInException;
import reman.common.database.exceptions.NotFoundException;
import reman.common.database.exceptions.NotPersistedException;
import reman.common.messaging.DboChangedMessage;
import reman.common.messaging.MessageManager;

/**
 * DatabaseObject (DBO) is the core class for all objects the wish to be 
 * persisted into the database.  This class offers a number of functionalities.
 * <BR><BR>
 * A DBO supports locking and unlocking to facilitate multiple users and prevent 
 * RAW and WAW errors.  When the GUI determines a user will be manipulating an object,
 * the object should be locked.  Locking prevents other users from editing the same object
 * at the same time.  For more information on locking, see the LockingManager.
 * <BR><BR>
 * When an object is done being edited, it should be committed to the database. 
 * The lock must still be explicitly released via the unlock function.  
 * <BR><BR>
 * The DBO supports complete version history and allows for the possibility of
 * versioning tools to be added down the line.  
 * <BR><BR>
 * Changes to DBOs are optionally broadcasted to all users within the same project. 
 *
 */
public abstract class DatabaseObject implements Persistable, Serializable {
	private transient long id_;
	private transient boolean broadcast_changes_;

	private String declared_class_;
	private int version_;
	private long previous_version_id_;
	private String version_chain_;
	private long latest_version_id_; //not used but necessary for lookup
	private long modifier_id_;
	private Timestamp modified_last_;
	
	
	protected String name_;
	protected String description_;

	public DatabaseObject() {
		/* Test generics */
		if (this.getClass().getTypeParameters().length > 0) {
			try {
				throw new Exception("DatabaseObject cannot use generics");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		id_ = -1;
		latest_version_id_ = -1;
		previous_version_id_ = -1;
		version_chain_ = null;
		version_ = 0;
		broadcast_changes_ = true;
		declared_class_ = this.getClass().getName();
		DatabaseManager.registerClass(this.getClass());
	}

	/**
	 * Returns information for the user that last modified this object.
	 * 
	 * @return UserInfo - users information
	 * @throws SQLException
	 */
	public UserInfo getLastModifier() throws SQLException {
		return UserManager.instance().getUserInfo(modifier_id_);
	}

	/**
	 * Returns the timestamp of the last modification
	 * 
	 * @return
	 */
	public Timestamp getLastModified() {
		return modified_last_;
	}

	/**
	 * This function returns whether or not the changes made to this DBO are
	 * broadcasted
	 * 
	 * @return
	 */
	public boolean isChangeBroadcasted() {
		return broadcast_changes_;
	}

	/**
	 * This function sets the broadcast flag. When true, changes in this dbo
	 * are broadcasted to all other clients
	 * 
	 * @param f
	 */
	protected void setChangeBroadCasted(boolean f) {
		this.broadcast_changes_ = f;
	}

	/**
	 * Returns the objects ID as it exists in the extending classes table
	 * @return long - objects primary key
	 */
	public long getID() {
		return id_;
	}

	/**
	 * Returns the version chain associated with this object and its version history
	 * The version chain allows easy object lookup and relationship management
	 * @return String to identify the chain of versions 
	 */
	public String getVersionChain() {
		return version_chain_;
	}

	/**
	 * This method should be treated like the setID function regarding its protection
	 * modifiers.  This function is required for retrieval operations
	 * @param chain This chain value will be applied to the object 
	 */
	protected void setVersionChain(String chain) {
		version_chain_ = chain;
	}

	/**
	 * Returns the extended table ID of the previous version for easy lookup
	 * @return long - Previous versions extended table ID
	 */
	public long getPreviousVersionID() {
		return previous_version_id_;
	}

	/**
	 * Returns the meta name of the object
	 * @return
	 */
	public String getName() {
		return name_;
	}

	/**
	 * Sets the name associated with an object
	 * This is sort of like meta data associated with objects
	 * @return
	 */
	public void setName(String name) {
		name_ = name;
	}

	/**
	 * Returns the meta description of the object
	 * @return
	 */
	public String getDescription() {
		return description_;
	}

	/** 
	 * Sets the meta description associated with the object
	 * @param desc
	 */
	public void setDescription(String desc) {
		description_ = desc;
	}

	/** 
	 * This function returns the extended name of the table for the implementing class
	 * @return String to identify the table name
	 */
	public String getTableName() {
		return DatabaseManager.getTableName(this.getClass());
	}

	public int getVersion() {
		return this.version_;
	}

	/** 
	 * This utility method determines whether an object is stored in the database
	 * or is a new object that has yet to be persisted
	 * @return true if it contains an ID and a version chain
	 */
	public boolean existsInDatabase() {
		return !(id_ < 0) || version_chain_ != null;
	}

	/**
	 * Determines if an object is outdated, that is, there is a later version of this object
	 * with the same version chain
	 * @return true if this version is older than the latest version
	 * @throws DatabaseObjectException
	 * @throws SQLException
	 * @throws LoggedInException 
	 */
	public boolean isVersionOutdated() throws DatabaseObjectException, SQLException,
			LoggedInException {
		return this.version_ < this.getLatestVersionNumber();
	}

	/**
	 * Returns a string representation of the object
	 */
	public String toString() {
		return this.getName();
	}

	/**
	 * This function will lock the object for editing
	 * It is vital to unlock the object when editing is done
	 * @return true if the lock was successfully created for the object
	 * @throws SQLException
	 * @throws LoggedInException 
	 */
	public boolean lock() throws SQLException, LoggedInException {
		return LockManager.instance().lock(this);
	}

	/**
	 * This function will unlock the object for editing
	 * It is vital to unlock the object when it is done being edited
	 * @return true if the unlock operation was successful
	 * @throws SQLException
	 * @throws LoggedInException 
	 */
	public boolean unlock() throws SQLException, LoggedInException {
		return LockManager.instance().unlock(this);
	}

	/**
	 * This function determines if there is a lock on the object
	 * @return true if the object is locked. 
	 * @throws SQLException
	 * @throws LoggedInException 
	 */
	public boolean isLocked() throws SQLException, LoggedInException {
		return LockManager.instance().isLocked(this);
	}

	/**
	 * Returns true if the implementation is a singleton.  This should be overwritten by classes
	 * that are  singletons.  
	 * @return true if the object is a singleton
	 */
	public boolean isSingleton() {
		return DatabaseManager.isSingleton(this.getClass());
	}

	public void rollback() throws DatabaseException, SQLException {
		retrieve(this.getPreviousVersionID());
	}

	public void rollforward() {

	}

	/**
	 * This function retrieves an object from the database and constructs its Java 
	 * representation.  
	 * <BR><BR>
	 * This function handles out of date objects by automatically updating them and 
	 * will always retrieve the latest version of the object
	 * 
	 * @throws LoggedInException if the user is not logged in
	 * @throws NotPersistedException if the object does not exist in the database
	 * @throws SQLException if there is a SQL syntax error
	 * 
	 */
	public void retrieve() throws DatabaseException, SQLException {
		retrieve(this.getLatestVersionID());
	}

	public void retrieve(long id) throws DatabaseException, SQLException {
		if (!UserManager.instance().isLoggedIn()) {
			throw new LoggedInException();
		}
		if (version_chain_ == null) {
			throw new NotPersistedException(this);
		}

		MemoryManager.instance().registerDatabaseObject(this);

		id_ = id;
		retrieve(this.getClass(), id_);
	}

	/**
	 * This is a helper function for the public retrieve method.  The public retrieve
	 * serves as an entry point for the retrieval process.  This function is recursively called
	 * until the object is completely retrieved.  
	 * <BR><BR>
	 * Retrievals are recursive.  As a result, all of the data for the app is retreived at launch
	 * and updated on an as needed basis.  
	 * <BR><BR>
	 * The retrieval of maps and collections are delegated to the retrieveMap and retrieveCollection
	 * functions respectively 
	 * @param c - The name of the class in the objects class hierarchy
	 * @param this_id -  The primary key to be used in retreiving the object
	 * @throws SQLException 
	 * @throws NotPersistedException 
	 * @throws LoggedInException 
	 */
	protected void retrieve(Class c, long this_id) throws SQLException, DatabaseException,
			NotPersistedException {

		String sql = "SELECT * FROM " + DatabaseManager.getTableName(c) + " WHERE ID=" + this_id;
		ResultSet rs = DatabaseManager.executeQuery(sql);

		long parent_id = -1;
		// get info from resultset
		if (DatabaseManager.getRowCount(rs) == 1) {
			rs.first();
			Field[] fields = c.getDeclaredFields();
			for (Field f : fields) {
				try {
					/* if the field is marked transient, then skip it */
					if (!DatabaseManager.isPersisted(f))
						continue;
					/* Allow access */
					f.setAccessible(true);

					if (DatabaseManager.isCollection(f.getType())) {
						this.retrieveCollection(c, f);
					} else if (DatabaseManager.isMap(f.getType())) {
						this.retrieveMap(c, f);
					} else {
						int cidx = rs.findColumn(f.getName());
						if (cidx < 0) {
							System.err.println("Couldn't find column for field " + f.getName());
							continue;
						}
						Object data = rs.getObject(cidx);
						if (data != null) {
							if (DatabaseManager.isEnum(f.getType())) {
								Object[] constants = f.getType().getEnumConstants();
								for (Object o : constants) {
									Enum e = (Enum) o;
									if (e.name().equalsIgnoreCase(data.toString())) {
										f.set(this, e);
										break;
									}
								}
							} else if (DatabaseManager.isSQLType(f.getType())) {
								f.set(this, data);
							} else if (DatabaseManager.isDatabaseObject(f.getType())) {
								DatabaseObject t = DatabaseObject.getObjectFromVersionChain((String) data);
								f.set(this, t);
							}
						}
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			parent_id = rs.getLong("PID");
		} else {
			throw new NotPersistedException(this);
		}

		/* Get parent info */
		Class parent = c.getSuperclass();
		if (parent != null && !parent.equals(Object.class)) {
			retrieve(parent, parent_id);
		}
	}

	/**
	 * This is a method for retrieving the contents of a Collection. 
	 * <BR><BR>
	 * In order for this method to work, if the contents of the list are implementations
	 * of DBO, they must have a default constructor.  
	 * @param c - The owning class (the class the field belongs to.)
	 * @param f - the field the collection will be saved to.  
	 * @throws SQLException
	 * @throws DatabaseException
	 * @throws NotPersistedException
	 */
	private void retrieveCollection(Class c, Field f) throws SQLException, DatabaseException,
			NotPersistedException {

		Collection coll = null;
		try {
			coll = (Collection) f.getType().newInstance();
		} catch (Exception e1) {
			//TODO: Log this error
			e1.printStackTrace();
		}

		/* Populate Collection */
		if (coll == null)
			return;

		// get the name of the collection table
		// list_[collectiontype]
		Class<?> list_type = DatabaseManager.getGenericClasses(f)[0];
		String list_table = DatabaseManager.getTableName(f);

		// select all entries in the collection table WHERE CLASS =
		// c.getSimpleType() AND OID = this.id_
		String query = "SELECT * FROM " + list_table + " WHERE CLASS='" + this.getTableName()
				+ "' AND OID='" + this.version_chain_ + "' AND FIELD='" + f.getName() + "' AND VERSION="
				+ this.getLatestVersionNumber();
		ResultSet list_rs = DatabaseManager.executeQuery(query);

		// extract data fields from result set
		if (DatabaseManager.getRowCount(list_rs) > 0) {
			do {
				Object data = list_rs.getObject("DATA");
				if (DatabaseManager.isSQLType(list_type)) {
					coll.add(data);
				} else {
					try {
						coll.add(DatabaseObject.getObjectFromVersionChain((String) data));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} while (list_rs.next());
		}

		/* Apply the collection */
		try {
			f.set(this, coll);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function is used to rebuild a saved map from the database.  Both the key and the value 
	 * must implement a default constructor if they are database objects.  
	 * @param c - The class the map belongs to 
	 * @param f - the field the map will be assigned to
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	private void retrieveMap(Class c, Field f) throws SQLException, DatabaseException {

		f.setAccessible(true);
		Map map = null;
		try {
			map = (Map) f.getType().newInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* Populate the map */
		if (map == null)
			return;
		map.clear();

		// get the name of the map table: map_[key class]_[value
		// class]
		Class[] types = DatabaseManager.getGenericClasses(f);
		Class keyType = types[0];
		Class valueType = types[1];
		String mapTable = DatabaseManager.getTableName(f);

		// select all the entries in the mapTable WHERE CLASS =
		// c.getSimpleName() AND OID = this.id_
		String query = "SELECT * FROM " + mapTable + " WHERE CLASS='" + this.getTableName()
				+ "' AND OID='" + this.version_chain_ + "' AND FIELD='" + f.getName() + "' AND VERSION="
				+ this.getLatestVersionNumber();
		ResultSet map_rs = DatabaseManager.executeQuery(query);

		// extract key and value fields from result set
		if (map_rs.first()) {
			do {
				try {
					Object key = map_rs.getObject("obj_key");
					Object value = map_rs.getObject("obj_val");

					DatabaseObject keyItem = null;
					DatabaseObject valueItem = null;
					/* if the key type is complex then build it from the db */
					if (!DatabaseManager.isSQLType(keyType)) {
						keyItem = DatabaseObject.getObjectFromVersionChain((String) key);
					}
					/* if the value type is complex then build from the db */
					if (!DatabaseManager.isSQLType(valueType)) {
						valueItem = DatabaseObject.getObjectFromVersionChain((String) value);
					}

					/*
					 * if a complex key or value was encountered then use the complex object
					 * else use the simple object
					 */
					map.put(((keyItem == null) ? key : keyItem), ((valueItem == null) ? value : valueItem));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} while (map_rs.next());
		}

		try {
			f.set(this, map);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public long commit() throws SQLException, DatabaseException {
		return commit(true);
	}

	/**
	 * This function is for composite objects.  If an internal dbo object is changed,
	 * a message will be sent to the server that the external dbo (the wrapper) also changed.
	 *	<BR><BR>
	 * For example, if there is a Meeting object and the location changes, the location object 
	 * (which is internal to the meeting object) will be committed.  In addition, a message will
	 * be distributed indicating that the meeting object changed as well. 
	 *
	 * @param external
	 * @return
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	public long commit(DatabaseObject external) throws SQLException, DatabaseException {
		long id = this.commit(true);

		// notify server
		if (external.id_ >= 0 && external.broadcast_changes_ && !Framework.MESSAGING_OFF) {
			//MessageManager.instance().send(new DboChangedMessage(external));
		}
		return id;
	}

	/**
	 * This function is the entry point for the committing of an object.  
	 * <BR><BR>
	 * Automatically updated are the version, the last modified time stamp,
	 * the previous id, and the current id.  
	 * <BR><BR>
	 * When an object is committed, the change is optionally broadcasted to all other users
	 * within the project.  
	 * @throws LoggedInException  thrown when a commit happens and there is no valid user logged in.
	 * As a result, one must be logged in to commit changes to the database.
	 * @throws SQLException occurs when there is a SQL syntax error
	 * @throws LockedException occurs when the object is locked by another user in the project
	 */
	public long commit(boolean distribute) throws SQLException, DatabaseException {

		if (!UserManager.instance().isLoggedIn()) {
			throw new LoggedInException();
		}

		if (this.existsInDatabase()) {
			if (this.isLocked()) {
				throw new LockedException(this);
			}
		}

		// update the last modified date
		this.modified_last_ = new Timestamp(new Date().getTime());

		// update the version
		this.version_++;

		// update modifier
		this.modifier_id_ = UserManager.instance().getCurrentUserID();

		// update the previous version id
		this.previous_version_id_ = this.id_;

		// create version chain if necessary
		if (version_chain_ == null)
			this.version_chain_ = VersionChainManager.instance().getUniqueVersionChain();

		// do the commit
		this.id_ = commit(this.getClass());

		// update version chain
		this.updateVersionChain();

		// register in memory
		MemoryManager.instance().registerDatabaseObject(this);

		// notify server
		if (id_ >= 0 && broadcast_changes_ && distribute && !Framework.MESSAGING_OFF) {
			MessageManager.instance().send(new DboChangedMessage(this));
		}

		return this.id_;
	}

	/**
	 * This function is a helper function which calls the commit for each stage in the class hierarchy.
	 * @param c - The name of the current class in the hierarchy
	 * @return the primary key of the newly committed class for use down the commit chain
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	private long commit(Class c) throws DatabaseException, SQLException {
		long pid = -1;
		if (!c.getSuperclass().equals(Object.class)) {
			pid = commit(c.getSuperclass());
		}

		/* Perform save of "this class" */
		pid = commit(c, pid);

		return pid;/* the ID column for this class */
	}

	/**
	 * This function handles the actual database interaction surrounding a commit operation.  Collections and
	 * maps are delegated to the commitCollection and commitMap respectively.  
	 * <BR><BR>
	 * This function iterates through each field in the passed in class and determines how 
	 * the field should be committed to the database.  It uses the utility methods in DatabaseManager
	 * to construct syntactically correct SQL statements to persist the updates to the database.
	 * @param c
	 * @param parent_id
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	private long commit(Class c, long parent_id) throws DatabaseException, SQLException {

		String columns = new String();
		String values = new String();
		Field[] fields = c.getDeclaredFields();
		ArrayList<Field> collection_fields = new ArrayList<Field>();
		ArrayList<Field> map_fields = new ArrayList<Field>();
		try {
			for (Field f : fields) {

				if (!DatabaseManager.isPersisted(f))
					continue;

				f.setAccessible(true);
				Object field_val = f.get(this);
				if (field_val == null)
					continue;

				/* Delegate complex and collections objects */
				if (!DatabaseManager.isSQLType(f.getType())) {
					if (DatabaseManager.isCollection(f.getType())) {
						collection_fields.add(f);
						continue;
					} else if (DatabaseManager.isMap(f.getType())) {
						map_fields.add(f);
						continue;
					} else if (DatabaseManager.isDatabaseObject(f.getType())) {
						DatabaseObject dbo = (DatabaseObject) field_val;
						if (!dbo.existsInDatabase()) {
							dbo.commit(false);
						}
						columns += "," + f.getName();
						values += ",'" + dbo.getVersionChain() + "'";

					} else {
						/* TODO: what do we do here? */
						// probably skip
						continue;
					}
				} else {
					/* Construct strings */
					columns += "," + f.getName();
					if (f.getType().isPrimitive()) {
						values += "," + field_val;
					} else {
						values += ",'" + field_val + "'";
					}
				}
			}
		} catch (IllegalAccessException e) {
		}

		long prim_key = this.id_;
		/* Commit 'this' object data to the database */
		String sql = "INSERT INTO " + DatabaseManager.getTableName(c) + " (ID,PID" + columns
				+ ") values(DEFAULT," + parent_id + values + ")";
		ResultSet rs = DatabaseManager.executeUpdate(sql).getGeneratedKeys();
		rs.first();
		prim_key = rs.getLong(1);

		commitCollection(collection_fields);

		commitMap(map_fields);

		return prim_key;
	}

	/**
	 * Collections are handled in a special manner.  For collections, we are not committing the items in the 
	 * collections, but instead it is necessary to store a means for recalling the items.  If the list stores
	 * database objects, the ID of the object is stored in the DATA field.  It is also sometimes necessary to store
	 * the declared class for generic lists.  For example if class A is abstract and B extends A.  A list of A's cannot
	 * be reconstructed unless it is known that the object is of type B.  As a result, the implementing class is stored
	 * with the data if necessary.
	 * <BR><BR>
	 * The items are not committed to the database unless they are not currently in the database.  It is NOT a problem
	 * that the database will be storing out of date IDs of objects.  When the object is retrieved, the latest
	 * version will be automatically determined and retrieved.  
	 * @param collection_fields - a list of fields that need to be committed
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	private void commitCollection(ArrayList<Field> collection_fields) throws DatabaseException,
			SQLException {
		/* We can now commit the list fields */
		/* pkey = OID = 'this' id */
		try {
			for (Field f : collection_fields) {
				f.setAccessible(true);
				Collection<?> coll = (Collection<?>) f.get(this);

				if (coll == null)
					continue;
				String table_name = DatabaseManager.getTableName(f);

				Statement batch = null;
				for (Object o : coll) {
					Class collection_type = o.getClass();
					String batchEntry = null;
					if (DatabaseManager.isSQLType(collection_type)) {
						batchEntry = "INSERT INTO " + table_name + " values(DEFAULT,'" + this.getVersionChain()
								+ "'," + this.getVersion() + ",'" + this.getTableName() + "','" + f.getName()
								+ "','','" + o + "');";
					} else if (DatabaseManager.isDatabaseObject(collection_type)) {
						DatabaseObject dbo = (DatabaseObject) o;
						String item_vchain = null;
						if (!dbo.existsInDatabase())
							dbo.commit(false);
						item_vchain = dbo.getVersionChain();
						batchEntry = "INSERT INTO " + table_name + " values(DEFAULT,'" + this.getVersionChain()
								+ "'," + this.getVersion() + ",'" + this.getTableName() + "','" + f.getName()
								+ "','" + collection_type + "','" + item_vchain + "');";
					} else {
						System.err.println("List contains unpersistable objects");
					}
					batch = DatabaseManager.addBatch(batchEntry, batch);
				}
				if (batch != null)
					DatabaseManager.executeBatch(batch);
			}
		} catch (IllegalAccessException e) {

		}
	}

	/**
	 * Maps are stored in a similar way to lists.  The implementing class of both the key and the value
	 * are critical for the restoration process.  Map tables store only enough information to rebuild the list.  
	 * This function will not commit the keys and values unless they are not already committed.  It should be noted that
	 * although the IDs for both keys and values could be out-dated (the object is updated and receives a new ID),
	 * the key or value will be automatically updated by the retrieve process.  
	 * @param map_fields - A list of fields that need to be saved to the database
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	private void commitMap(ArrayList<Field> map_fields) throws DatabaseException, SQLException {
		/* now commit the map fields */
		/* pkey = OID */
		try {
			for (Field f : map_fields) {
				f.setAccessible(true);
				Map<?, ?> map = (Map<?, ?>) f.get(this);

				/* Delete original map */
				Class[] maptypes = DatabaseManager.getGenericClasses(f);
				Class keyClass = maptypes[0];
				Class valueClass = maptypes[1];
				String tableName = DatabaseManager.getTableName(f);

				Statement batch = null;
				for (Map.Entry<?, ?> entry : map.entrySet()) {
					String key_chain = null;
					String value_chain = null;

					/* If the key is a complex type, delegate commit to obtain key */
					if (!DatabaseManager.isSQLType(keyClass)) {
						DatabaseObject dbo = (DatabaseObject) entry.getKey();
						if (!dbo.existsInDatabase())
							dbo.commit(false);
						key_chain = dbo.getVersionChain();
					}

					/* If the value is a complex type, delegate commit to obtain key */
					if (!DatabaseManager.isSQLType(valueClass)) {
						DatabaseObject dbo = (DatabaseObject) entry.getValue();
						if (!dbo.existsInDatabase())
							dbo.commit(false);
						value_chain = dbo.getVersionChain();
					}

					/*
					 * if a complex key or value exists then enter the fkey for the entry
					 * else enter the object value
					 */
					String sqlBatchEntry = "INSERT INTO " + tableName + " values(DEFAULT,'"
							+ this.getVersionChain() + "'," + this.getVersion() + ",'" + this.getTableName()
							+ "','" + f.getName() + "','" + entry.getKey().getClass() + "','"
							+ entry.getValue().getClass() + "','"
							+ ((key_chain == null) ? entry.getKey() : key_chain) + "','"
							+ ((value_chain == null) ? entry.getValue() : value_chain) + "');";
					batch = DatabaseManager.addBatch(sqlBatchEntry, batch);
				}
				if (batch != null)
					DatabaseManager.executeBatch(batch);
			}
		} catch (IllegalAccessException e) {
			//TODO: Log this error
		}
	}

	/**
	 * This function uses the version chain to determine the latest version of the object.  The 
	 * object must have a  version chain for this function to work properly.  
	 * @return An integer indicated the version of the object.  
	 * @throws DatabaseObjectException occurs if the object is not in the database yet
	 * @throws SQLException occurs if there is a SQL syntax error.  
	 * @throws LoggedInException occurs if the user is not logged in
	 */
	protected int getLatestVersionNumber() throws DatabaseObjectException, SQLException,
			LoggedInException {
		if (this.version_chain_ == null)
			throw new NotPersistedException(this);
		String latest_version_sql = "SELECT max(version_) FROM "
				+ DatabaseManager.getTableName(DatabaseObject.class) + " WHERE version_chain_='"
				+ this.version_chain_ + "'";

		ResultSet rs = DatabaseManager.executeQuery(latest_version_sql);
		if (DatabaseManager.getRowCount(rs) <= 0)
			throw new NotFoundException(this);
		return rs.getInt(1);

	}

	/**
	 * This function updates the entire version chain 
	 * <BR><BR>
	 * All items sharing the same version chain are updated with the ID of the latest version
	 * during a commit.  
	 * @throws SQLException
	 * @throws LoggedInException
	 */
	protected void updateVersionChain() throws SQLException, LoggedInException {
		String update_sql = "UPDATE " + DatabaseManager.getTableName(DatabaseObject.class)
				+ " SET latest_version_id_='" + this.getID() + "' WHERE version_chain_='"
				+ this.version_chain_ + "'";
		DatabaseManager.executeUpdate(update_sql);
	}

	/**
	 * This function returns  the ID of the latest version in a version chain.
	 * @return
	 * @throws SQLException
	 * @throws LoggedInException
	 */
	protected long getLatestVersionID() throws SQLException, LoggedInException {
		String latest_sql = "SELECT latest_version_id_ FROM "
				+ DatabaseManager.getTableName(DatabaseObject.class) + " WHERE version_chain_='"
				+ this.version_chain_ + "' LIMIT 1";
		ResultSet rs = DatabaseManager.executeQuery(latest_sql);
		if (DatabaseManager.getRowCount(rs) == 0)
			return id_;
		return rs.getLong(1);
	}

	public static DatabaseObject getObjectFromVersionChain(String version_chain) throws SQLException,
			DatabaseException {
		DatabaseObject inMem = MemoryManager.instance().getDatabaseObjectReference(version_chain);
		if (inMem != null) {
			return inMem;
		}

		try {
			String lowData = "SELECT * FROM " + DatabaseManager.getTableName(DatabaseObject.class)
					+ " WHERE version_chain_='" + version_chain + "' LIMIT 1";
			ResultSet rs = DatabaseManager.executeQuery(lowData);
			if (DatabaseManager.getRowCount(rs) != 1)
				return null;
			String declared_class = rs.getString("declared_class_");

			Class decl_class = Class.forName(declared_class);
			Constructor constr = decl_class.getDeclaredConstructor(null);
			constr.setAccessible(true);
			DatabaseObject dbo = (DatabaseObject) constr.newInstance(null);
			dbo.setVersionChain(version_chain);
			dbo.retrieve();
			return dbo;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
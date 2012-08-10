package reman.common.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import reman.client.app.Framework;
import reman.client.app.listeners.DboChangedListener;
import reman.common.database.exceptions.DatabaseException;
import reman.common.messaging.DboChangedMessage;

public class MemoryManager implements DboChangedListener {
	private static MemoryManager this_;

	private Hashtable<String, DatabaseObject> memory_manager_;
	private Hashtable<Class<? extends DatabaseObject>, ArrayList<DatabaseObject>> objects_by_class_;
	private boolean dynamic_;

	public MemoryManager() {
		dynamic_ = true;
		memory_manager_ = new Hashtable<String, DatabaseObject>();
		objects_by_class_ = new Hashtable<Class<? extends DatabaseObject>, ArrayList<DatabaseObject>>();
		Framework.instance().addDboChangedListener(this);
	}

	/**
	 * Retrieves the amount of memory used in bytes
	 * @return
	 */
	public long getRuntimeMemory() {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	/**
	 * Retrieves the amount of memory allocated to the program
	 * @return
	 */
	public long getAllocatedMemory() {
		return Runtime.getRuntime().totalMemory();
	}

	public ArrayList<DatabaseObject> getDatabaseObjectsByClass(Class<?> c) {
		return objects_by_class_.get(c);
	}

	/**
	 * If a new object is created it must be registered to maintain pointers
	 * @param o
	 */
	public void registerDatabaseObject(DatabaseObject o) {
		memory_manager_.put(o.getVersionChain(), o);

		Class<? extends DatabaseObject> c = o.getClass();
		ArrayList<DatabaseObject> dbos = objects_by_class_.get(c);
		if (dbos == null) {
			dbos = new ArrayList<DatabaseObject>();
			objects_by_class_.put(c, dbos);
		}
		dbos.add(o);
	}

	/**
	 * This function returns the reference to the databaseobject based on the version
	 * chain. 
	 * @param version_chain
	 * @return
	 */
	public DatabaseObject getDatabaseObjectReference(String version_chain) {
		return memory_manager_.get(version_chain);
	}
	

	/**
	 * This function determines if the object has already been loaded in memory.
	 * If it has, then whoever is calling this function should request a pointer 
	 * to the object instead of making a new one.  
	 * @param o
	 * @return
	 */
	public boolean isDatabaseObjectInMemory(DatabaseObject o) {
		return memory_manager_.containsKey(o.getVersionChain());
	}

	/**
	 * Dynamic mode means that a retrieve is automatically performed when an
	 * event is received.
	 * @return
	 */
	public boolean isDynamic() {
		return dynamic_;
	}

	/**
	 * Set the mode to dynamic or not
	 * @param flag - true will turn dynamic mode on.
	 */
	public void setDynamicMode(boolean flag) {
		dynamic_ = flag;
	}

	public void dboChangedEvent(DboChangedMessage m) {
		try {
			m.getDatabaseObject().retrieve();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static MemoryManager instance() {
		if (this_ == null)
			this_ = new MemoryManager();
		return this_;
	}
}

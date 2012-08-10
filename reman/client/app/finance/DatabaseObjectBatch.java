package reman.client.app.finance;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.LoggedInException;

/**
 * If DatabaseObjects must be treated in batch fashion (attempt at all contained or best effort) with respect to database actions.
 * This class gives best effort functionality, meaning it will always attempt to provide the invocation database action to all
 * DatabaseObjects contained within the batch, regardless if any throw exceptions along the way.  Each batch database action
 * provides a collection return value which consists of the DatabaseObjects which threw exceptions while attempting the desired database action.
 * @author Scott
 *
 */
public class DatabaseObjectBatch<T extends DatabaseObject> {
	private ArrayList<T> dbos_;

	/**
	 * Instantiate an empty DatabaseObjectBatch.
	 */
	public DatabaseObjectBatch() {
		this.dbos_ = new ArrayList<T>();
	}

	/**
	 * Add the <code>dbo</code> to this DatabaseObjectBatch.
	 * @param dbo
	 * @return True if <code>dbo</code> was added to this DatabaseObjectBatch.
	 */
	public boolean addToBatch(T dbo) {
		return this.dbos_.add(dbo);
	}

	/**
	 * Add all objects in <code>dbos</code> to this DatabaseObjectBatch.
	 * @param dbos
	 * @return True if this DatabaseObjectBatch changed as a result.
	 */
	public boolean addToBatch(Collection<? extends T> dbos) {
		return this.dbos_.addAll(dbos);
	}

	/**
	 * Remove <code>dbo</code> from this DatabaseObjectBatch.
	 * @param dbo
	 * @return True if <code>dbo</code> was removed, false otherwise.
	 */
	public boolean removeFromBatch(DatabaseObject dbo) {
		return this.dbos_.remove(dbo);
	}

	/**
	 * Obtain the number of DatabaseObjects in this DatabaseObjectBatch.
	 * @return
	 */
	public int getBatchSize() {
		return this.dbos_.size();
	}

	public ArrayList<T> getBatch() {
		return new ArrayList<T>(this.dbos_);
	}

	/**
	 * Obtain the number of DatabaseObjects in this DatabaseObjectBatch which currently are locked.
	 * @return
	 */
	public int getLockCount() {
		int count = 0;
		for (DatabaseObject dbo : this.dbos_) {
			try {
				if (dbo.isLocked())
					count++;
			} catch (LoggedInException e) {
			} catch (SQLException e) {
			}
		}
		return count;
	}

	/**
	 * Attempt to lock all DatabaseObjects in this DatabaseObjectBatch.
	 * @return A collection of database objects in the batch which failed to lock
	 */
	public Collection<DatabaseObject> lockBatch() {
		ArrayList<DatabaseObject> failed_locks = new ArrayList<DatabaseObject>();
		for (DatabaseObject dbo : this.dbos_) {
			try {
				if (!dbo.lock())
					failed_locks.add(dbo);
			} catch (LoggedInException e) {
				failed_locks.add(dbo);
			} catch (SQLException e) {
				failed_locks.add(dbo);
			}
		}
		return failed_locks;
	}

	/**
	 * Attempt to commit all DatabaseObjects in this DatabaseObjectBatch.
	 * @return A collection of database objects in the batch which failed to commit
	 */
	public Collection<DatabaseObject> commitBatch() {
		ArrayList<DatabaseObject> failed_commits = new ArrayList<DatabaseObject>();
		for (DatabaseObject dbo : this.dbos_) {
			try {
				if (dbo.commit() < 0)
					failed_commits.add(dbo);
			} catch (SQLException e) {
				failed_commits.add(dbo);
			} catch (DatabaseException e) {
				failed_commits.add(dbo);
			}
		}
		return failed_commits;
	}

	/**
	 * Attempt to unlock all DatabaseObjects in this DatabaseObjectBatch.
	 * @return A collection of database object in the batch which failed to unlock
	 */
	public Collection<DatabaseObject> unlockBatch() {
		ArrayList<DatabaseObject> failed_unlocks = new ArrayList<DatabaseObject>();

		for (DatabaseObject dbo : this.dbos_) {
			try {
				if (!dbo.unlock())
					failed_unlocks.add(dbo);
			} catch (LoggedInException e) {
				failed_unlocks.add(dbo);
			} catch (SQLException e) {
				failed_unlocks.add(dbo);
			}
		}

		return failed_unlocks;
	}

	/**
	 * Attempt to retrieve each DatabaseObject in this DatabaseObjectBatch.
	 * @return A collection of database objects in the batch which failed to retrieve
	 */
	public Collection<DatabaseObject> retrieveBatch() {
		ArrayList<DatabaseObject> failed_retrieve = new ArrayList<DatabaseObject>();

		for (DatabaseObject dbo : this.dbos_) {
			try {
				dbo.retrieve();
			} catch (DatabaseException e) {
				failed_retrieve.add(dbo);
			} catch (SQLException e) {
				failed_retrieve.add(dbo);
			}
		}

		return failed_retrieve;
	}

	/**
	 * This method is used for printing failure DatabaseObject names.
	 * @param dbos
	 * @return String consisting of the concatenation of each DatabaseObject in <code>dbos</code>.
	 */
	public static String getDatabaseObjectNames(Collection<? extends DatabaseObject> dbos) {
		String failed_acct_names = "";
		for (DatabaseObject dbo : dbos) {
			failed_acct_names += dbo.getName() + ", ";
		}
		failed_acct_names = failed_acct_names.replaceAll("[,]\\s\\z", "");
		return failed_acct_names;
	}
}

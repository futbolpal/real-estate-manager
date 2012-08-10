package reman.common.database;

import java.sql.SQLException;

import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.DatabaseObjectException;
import reman.common.database.exceptions.ExceedMaxCommitException;
import reman.common.database.exceptions.LoggedInException;
import reman.common.database.exceptions.NotPersistedException;

/**
 * Managed Limited Commit DatabaseObject.  This provides a layer on top of the managed database object and provides a limit on
 * the number of allowed commits.
 * @author Scott
 *
 */
public abstract class ManagedLCDatabaseObject extends ManagedDatabaseObject {

	private int max_commits_allowed_;

	/**
	 * DatabaseObject use only
	 */
	protected ManagedLCDatabaseObject() {
	}

	public ManagedLCDatabaseObject(String[] managed_field_names, int max_commits_allowed) {
		super(managed_field_names);
		this.max_commits_allowed_ = max_commits_allowed;
	}

	/**
	 * Managed Limited Commit.  Attempt to commit all managed objects, if the current database version is less than the
	 * max commit limit.
	 * @throws DatabaseException 
	 */
	@Override
	public long commit() throws SQLException, DatabaseException {
		/*find the MAX version of the current version chain*/
		if (allowedMoreCommits())
			return super.commit();
		throw new ExceedMaxCommitException(this,
				"Attempt to commit more times then allowed. Current max version is '"
						+ super.getLatestVersionNumber() + "'. Max allowed version is '"
						+ this.max_commits_allowed_ + "'.");
	}

	public boolean allowedMoreCommits() throws LoggedInException, DatabaseObjectException,
			SQLException {
		int curr_version = 0;
		try {
			curr_version = super.getLatestVersionNumber();
		} catch (NotPersistedException e) {
			curr_version = 0;
		}
		if (this.max_commits_allowed_ > curr_version)
			return true;
		return false;
	}

	public int getMaxAllowedCommits() {
		return this.max_commits_allowed_;
	}
}

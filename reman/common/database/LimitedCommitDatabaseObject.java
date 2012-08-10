package reman.common.database;

import java.sql.SQLException;

import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.ExceedMaxCommitException;
import reman.common.database.exceptions.NotPersistedException;

/**
 * Once this object is committed to the database it will not be allowed to be
 * re-committed.
 * 
 * @author Scott
 * 
 */
public class LimitedCommitDatabaseObject extends DatabaseObject {
	private int max_allowed_commits_;

	/**
	 * For DatabaseObject use only
	 */
	protected LimitedCommitDatabaseObject() {

	}

	public LimitedCommitDatabaseObject(int max_allowed_commits) {
		this.max_allowed_commits_ = max_allowed_commits;
	}

	public long commit() throws SQLException, DatabaseException {

		/* find the MAX version of the current version chain */
		int current_max_version = super.getLatestVersionNumber();
		if (this.max_allowed_commits_ > current_max_version) {
			int curr_version = 0;
			try {
				curr_version = super.getLatestVersionNumber();
			} catch (NotPersistedException e) {
				curr_version = 0;
			}
			if (this.max_allowed_commits_ >= curr_version)
				return super.commit();
			throw new ExceedMaxCommitException(this,
					"Attempt to commit more times then allowed. Current max version is '" + curr_version
							+ "'. Max allowed version is '" + this.max_allowed_commits_ + "'.");
		}
		return -1;
	}

	protected int getMaxAllowedCommits() {
		return this.max_allowed_commits_;
	}
}

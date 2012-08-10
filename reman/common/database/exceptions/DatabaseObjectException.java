package reman.common.database.exceptions;

import reman.common.database.DatabaseObject;

public class DatabaseObjectException extends DatabaseException {
	private DatabaseObject dbo_;

	public DatabaseObjectException(DatabaseObject dbo, String message) {
		super(message);
		dbo_ = dbo;
	}

	public DatabaseObject getDatabaseObject() {
		return dbo_;
	}
}

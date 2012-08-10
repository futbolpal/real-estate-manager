package reman.common.database.exceptions;

import reman.common.database.DatabaseObject;

public class ExceedMaxCommitException extends DatabaseObjectException {
	public ExceedMaxCommitException(DatabaseObject dbo, String message) {
		super(dbo, message);
	}
}

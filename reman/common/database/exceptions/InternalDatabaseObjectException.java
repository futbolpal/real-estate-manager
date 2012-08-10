package reman.common.database.exceptions;

import reman.common.database.DatabaseObject;

/**
 * To organize errors that arise as a result of programmatic mistakes
 * @author Scott
 *
 */
public class InternalDatabaseObjectException extends DatabaseObjectException {
	public InternalDatabaseObjectException(DatabaseObject dbo, String message) {
		super(dbo, message);
	}
}

package reman.common.database.exceptions;

import reman.common.database.DatabaseObject;

public class NotPersistedException extends DatabaseObjectException {
	public NotPersistedException(DatabaseObject o) {
		super(o, "This object is not persisted in the database: " + o.getClass());
	}

}

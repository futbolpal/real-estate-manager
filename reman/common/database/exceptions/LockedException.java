package reman.common.database.exceptions;

import reman.common.database.DatabaseObject;

public class LockedException extends DatabaseObjectException {
	public LockedException(DatabaseObject locked) {
		super(locked, "This database object is locked by another user.");
	}

}

package reman.common.database.exceptions;

import reman.common.database.DatabaseObject;

public class NotFoundException extends DatabaseObjectException {
	public NotFoundException(DatabaseObject o) {
		super(o, "Database object could not be found");
	}
}

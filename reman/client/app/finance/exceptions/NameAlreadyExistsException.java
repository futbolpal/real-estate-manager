package reman.client.app.finance.exceptions;

import reman.common.database.DatabaseObject;

/**
 * Thrown when a new DatabaseObject is introduced in such a way where an existing DatabaseObject's name conflicts.
 * @author Scott
 *
 */
public class NameAlreadyExistsException extends FinanceException {
	/**
	 * Retain the new DatabaseObject which creates the name conflict.
	 * @param obj
	 */
	public NameAlreadyExistsException(DatabaseObject obj) {
		super(obj, "DatabaseObject with name '" + obj.getName() + "' already exists.");
	}
}

package reman.client.app.finance.exceptions;

import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseObjectException;

/**
 * Provides a base class which all other FinancialExceptions inherit from.
 * @author Scott
 *
 */
public class FinanceException extends DatabaseObjectException {
	public FinanceException(DatabaseObject financeObject, String message) {
		super(financeObject, message);
	}
}

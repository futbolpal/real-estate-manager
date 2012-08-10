package reman.client.app.finance.exceptions;

import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.InternalDatabaseObjectException;

/**
 * Used for internal debugging to assist in mitigating deviations in expected results from computations.
 * @author Scott
 *
 */
public class InternalFinanceException extends InternalDatabaseObjectException{
	public InternalFinanceException(DatabaseObject dbo, String message){
		super(dbo, message);
	}
}

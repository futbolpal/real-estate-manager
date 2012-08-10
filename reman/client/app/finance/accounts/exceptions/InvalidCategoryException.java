package reman.client.app.finance.accounts.exceptions;

import reman.client.app.finance.accounts.AcctActionCategory;
import reman.client.app.finance.exceptions.FinanceException;

/**
 * This exception is thrown when an invalid (not included in an account's category tree) category is associated with an account, typically
 * in a journal entry.
 * @author Scott
 *
 */
public class InvalidCategoryException extends FinanceException{
	public InvalidCategoryException(AcctActionCategory category, String message){
		super(category, message);
	}
}

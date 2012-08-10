package reman.client.app.finance.accounts.exceptions;

import reman.client.app.finance.exceptions.FinanceException;
import reman.common.database.DatabaseObject;

/**
 * This is thrown when an action on an item maintained in a tree is not in that tree.
 * (AccountManager throws this if account is not part of the Account tree)
 * @author Scott
 *
 */
public class UnknownNodeException extends FinanceException{
	public UnknownNodeException(DatabaseObject node, String message){
		super(node, message);
	}
}

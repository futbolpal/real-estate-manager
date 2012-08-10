package reman.client.app.finance.accounts.exceptions;

import reman.client.app.finance.exceptions.FinanceException;
import reman.common.database.DatabaseObject;

/**
 * This is thrown from the constructor of a class maintained in a tree (Account, AcctActionCategory).  When a parent
 * has a balance and the first child is made, the balance must be transfered to the first child.  It doesn't make sense
 * to have balances on the inner nodes.  This will be thrown to notify that AcctAmount transfer action was taken.
 * @author Scott
 *
 */
public class FirstChildofParentWithBalance extends FinanceException {

	private DatabaseObject parent_;

	public FirstChildofParentWithBalance(DatabaseObject child, DatabaseObject parent, String message) {
		super(child, message);
		this.parent_ = parent;
	}

	public DatabaseObject getParent() {
		return this.parent_;
	}
}

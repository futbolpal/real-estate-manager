package reman.client.app.finance.accounts.exceptions;

import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.finance.accounts.AcctAmount;

/**
 * This exception is raised when an addition or subtraction of an AcctAmount change the transaction type unexpectedly.
 * For example, a journal entry is not allowed to make an account balance go negative (change transaction type) and so this
 * exception is thrown to alert caller that the attempted amount is invalid.
 * @author Scott
 *
 */
public class InvalidAmountException extends FinanceException {
	public InvalidAmountException(AcctAmount amt, String message) {
		super(amt, message);
	}
}

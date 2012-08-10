package reman.client.app.finance.journals.exceptions;

import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.finance.journals.JournalEntry;

/**
 * Thrown if a journal entry results in an invalid financial action.  For example a journal entry is not allowed to apply an
 * amount which will make an Account balance system negative.
 * @author Scott
 *
 */
public class InvalidJournalEntryException extends FinanceException {
	/**
	 * 
	 * @param je Subjective journal entry which is invalid.
	 * @param message
	 */
	public InvalidJournalEntryException(JournalEntry je, String message) {
		super(je, message);
	}
}

package reman.client.app.finance.ledger;

import java.util.ArrayList;
import java.util.Collection;

import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.journals.JournalEntry;
import reman.client.app.finance.journals.JournalEntryLineItem;
import reman.common.database.DatabaseObject;

/**
 * Each Account will have an associated Ledger.  A Ledger will maintain all JournalEntryLineItem objects which involve the Account
 * this Ledger is associated with.
 * @author Scott
 *
 */
public class Ledger extends DatabaseObject {

	private Account acct_;

	/**
	 * Only line items pertaining to acct_
	 */
	private ArrayList<JournalEntryLineItem> line_items_;

	/**
	 * DatabaseObject use only
	 */
	private Ledger() {
	}

	/**
	 * Initialize this Ledger to be associated with <code>acct</code>.
	 * @param acct
	 */
	public Ledger(Account acct) {
		this.acct_ = acct;
		this.name_ = acct.getName() + " Ledger";
		this.line_items_ = new ArrayList<JournalEntryLineItem>();
	}

	/**
	 * Obtain all JournalEntryLineItem objects associated with the Account object within this Ledger.
	 * @return
	 */
	public Collection<JournalEntryLineItem> getLineItems() {
		return new ArrayList<JournalEntryLineItem>(this.line_items_);
	}

	/**
	 * 
	 * @param index
	 * @return The JournalEntry object corresponding to <code>index</code>
	 */
	public JournalEntryLineItem getLineItem(int index) {
		if (index >= 0 && index < this.line_items_.size())
			return this.line_items_.get(index);
		return null;
	}

	/**
	 * Parses the <code>je</code> and retains all JournalEntryLineItem objects pertaining to this Ledger's account
	 * @param je
	 * @return line items that were accounted for
	 */
	public Collection<JournalEntryLineItem> track(JournalEntry je) {
		ArrayList<JournalEntryLineItem> lines_accounted_for = new ArrayList<JournalEntryLineItem>();
		for (JournalEntryLineItem jeli : je.getLineItems().values()) {
			if (this.addLineItem(jeli))
				lines_accounted_for.add(jeli);
		}
		return lines_accounted_for;
	}

	/**
	 * Add a JournalEntryLineItem to this Ledger.  The Account within <code>jeli</code> must pertain to the Account for this Ledger.
	 * @param jeli
	 * @return
	 */
	public boolean addLineItem(JournalEntryLineItem jeli) {
		if (jeli.getAccount() != this.acct_)
			return false;
		if (this.line_items_.contains(jeli))
			return false;
		return this.line_items_.add(jeli);
	}
}
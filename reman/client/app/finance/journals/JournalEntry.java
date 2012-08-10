package reman.client.app.finance.journals;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.exceptions.InvalidAmountException;
import reman.client.app.finance.journals.exceptions.InvalidJournalEntryException;
import reman.common.database.ManagedLCDatabaseObject;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.DatabaseObjectException;
import reman.common.database.exceptions.ExceedMaxCommitException;
import reman.common.database.exceptions.NotPersistedException;

/**
 * A JournalEntry is designed to regulate the transfer of balances between Account objects.  The JournalEntry also accommodates for
 * categorizing each transfer by incorporating AcctActionCategory in the validation process.
 * @author Scott
 *
 */
public class JournalEntry extends ManagedLCDatabaseObject {/*MUST BE MANAGED TO LOCK ALL ACCOUNTS BEFORE AMOUNTS APPLIED*/

	/**
	 * When this journal entry actually occurred
	 */
	private Timestamp occurred_ts_;

	/**
	 * true when this JournalEntry has been successfully validated
	 * and applied
	 */
	private boolean is_valid_;

	/**
	 * store each line item with associated key for reference later
	 */
	private Hashtable<Integer, JournalEntryLineItem> line_items_;

	/**
	 * For DatabaseObject use only
	 */
	private JournalEntry() {
		super(new String[] { "line_items_" }, 1);//line items MUST BE MANAGED because all accounts must be locked to apply amounts
	}

	/**
	 * 
	 * @param desciprtion A real world description of the relevance to this JournalEntry.
	 * @param occurred When the journal entry actually occurred in business world. If null it is taken as now.
	 */
	public JournalEntry(String desciprtion, Timestamp occurred) {
		this();
		super.description_ = desciprtion;
		this.occurred_ts_ = (occurred == null) ? new Timestamp(new Date().getTime()) : occurred;
		this.line_items_ = new Hashtable<Integer, JournalEntryLineItem>();
		this.is_valid_ = false;
	}

	/**
	 * Add a line item to this JournalEntry.
	 * @param jeli
	 * @return Null if key already exists. Other wise the id in the hash table where the line item was placed
	 */
	public Integer addLineItem(JournalEntryLineItem jeli) {
		if (!this.existsInDatabase()) {
			Integer key = this.getNextKey();
			if (this.line_items_.containsKey(key))
				return null;

			jeli.setTime(this.occurred_ts_);
			this.line_items_.put(key, jeli);
			return key;
		}
		return null;
	}

	/**
	 * Add the line item collection and return the keys (same order as provided) of the line items in this journal entry
	 * @param jeli_col
	 * @return
	 */
	public Collection<Integer> addLineItems(Collection<JournalEntryLineItem> jeli_col) {
		ArrayList<Integer> keys = new ArrayList<Integer>();
		for (JournalEntryLineItem jeli : jeli_col)
			keys.add(this.addLineItem(jeli));
		return keys;
	}

	/**
	 * Remove the line item with the corresponding <code>key</code> from this JournalEntry.
	 * @param key
	 * @return the value to which the key had been mapped in this hashtable, or null if the key did not have a mapping
	 */
	public JournalEntryLineItem removeLineItem(Integer key) {
		JournalEntryLineItem result = null;
		if (!this.existsInDatabase()) {
			result = this.line_items_.remove(key);
			if (result != null) {
				result.setTime(null);
			}
		}
		return result;
	}

	private Integer getNextKey() {
		return this.line_items_.size();
	}

	/**
	 * Ensures that all conditions of this JournalEntry are valid with respect to journal entry requirements from the financial accounting domain.
	 * This method will also apply line item amounts to respective Accounts if validation succeeds.
	 * @throws DatabaseException 
	 * @throws SQLException 
	 * @throws SQLException 
	 */
	private void validate() throws DatabaseException, InvalidJournalEntryException, SQLException {
		/*check the following conditions:
		 * 1) no line item can make its respective account balance negative
		 * 2) credit column sum must equal debit column sum
		 * 3) (Asset = Liability + Equity) must hold (consider contra accounts)*/

		/*if previously validated and applied, return true*/
		if (this.isValid())
			return;

		/*assume exception occurs until successfully passed all checks*/
		boolean exception_occured = true;

		/*keep a running sum of amounts seen*/
		double debit_normal_balance_amt_ = 0, credit_normal_balance_amt_ = 0;
		double credit_amt_ = 0, debit_amt_ = 0;

		try {
			/*parse the journal entries in order*/
			ArrayList<Integer> all_keys = new ArrayList<Integer>(this.line_items_.keySet());
			Collections.sort(all_keys);
			for (Integer curr_key : all_keys) {
				JournalEntryLineItem curr_item = this.line_items_.get(curr_key);

				try {
					curr_item.addAmount();
				} catch (InvalidAmountException ex) {
					throw new InvalidJournalEntryException(this, "Journal Entry Line Item with key '"
							+ curr_key + "' (" + curr_item + "') applied an invalid amount of '"
							+ curr_item.getAmount() + "' to Account balance '"
							+ curr_item.getAccount().getBalanceSystem() + "'.");
				}
				/*track the total high level normal balance amount (credit normal/debit normal)*/
				if (curr_item.getAccountNormalBalance() == TransactionType.CREDIT) {
					if (curr_item.getAccountNormalBalance() == curr_item.getAmount().getTransactionType())
						credit_normal_balance_amt_ += curr_item.getAmount().getAmount();
					else
						credit_normal_balance_amt_ -= curr_item.getAmount().getAmount();
				} else {
					if (curr_item.getAccountNormalBalance() == curr_item.getAmount().getTransactionType())
						debit_normal_balance_amt_ += curr_item.getAmount().getAmount();
					else
						debit_normal_balance_amt_ -= curr_item.getAmount().getAmount();
				}

				/*track the transaction amount(credit,debit)*/
				if (curr_item.getAmount().getTransactionType() == TransactionType.CREDIT)
					credit_amt_ += curr_item.getAmount().getAmount();
				else
					debit_amt_ += curr_item.getAmount().getAmount();
			}

			/*NOTE: Sum of debit normal balance account line items != Sum of credit normal balance account line items*/
			/*			same as Asset = Liabilities + Equity*/
			if (debit_normal_balance_amt_ != credit_normal_balance_amt_) {
				throw new InvalidJournalEntryException(this,
						"(Asset=Libilities+Owners equity) accounting equation not satisfied.");
				/*Using (debit normal balance amount != credit normal balance amount) implementation.*/
			}
			if (credit_amt_ != debit_amt_) {
				throw new InvalidJournalEntryException(this,
						"Total credit column must equat total debit column condition not satisfied");
			}

			exception_occured = false;

		} catch (InvalidJournalEntryException e) {
			throw e;
		} finally {
			/*curr_item failed to be applied, all that were applied before it will need to be undone*/
			if (exception_occured)
				this.retrieve();
		}

		/*all checks passed*/
		this.is_valid_ = true;
	}

	/**
	 * @return True if this JournalEntry has been validated and amounts applied.
	 */
	public boolean isValid() {
		return this.is_valid_;
	}

	/**
	 * Obtain a map of all line items contained in this JournalEntry.
	 * @return
	 */
	public Hashtable<Integer, JournalEntryLineItem> getLineItems() {
		return new Hashtable<Integer, JournalEntryLineItem>(this.line_items_);
	}

	/**
	 * Obtain the line item at the corresponding <code>index</code>
	 * @param index
	 * @return
	 */
	public JournalEntryLineItem getLineItem(int index) {
		return this.line_items_.get(index);
	}

	public Timestamp getOccurredTime() {
		return this.occurred_ts_;
	}

	public void setOccurredTime(Timestamp occurred) {
		this.occurred_ts_ = occurred;
	}

	@Override
	public void retrieve() throws SQLException, DatabaseException {
		try {
			/*all accounts must be up to date before applying amounts*/
			super.retrieve();
		} catch (NotPersistedException e) {
			/*this retrieve is only for the managed objects within.  This object will never have the */
		}
	}

	/**
	 * This JournalEntry will be validated, and if successful then committed along with all line items and affected DatabaseObjects within
	 * each line item.
	 * @throws DatabaseException 
	 * @throws SQLException 
	 * @throws InvalidJournalEntryException Result of invalid amount, debit total not equal to credit total, or debit normal balance not equal to credit normal balance total.
	 * @throws ExceedMaxCommitException JournalEntry objects are only allowed to be committed once, in accordance with financial accounting practices.
	 * @throws  
	 */
	@Override
	public long commit() throws SQLException, DatabaseException, InvalidJournalEntryException,
			ExceedMaxCommitException {

		try {
			/*all accounts must be up to date before applying amounts*/
			this.retrieve();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DatabaseObjectException(this,
					"Failed to retrieve Journal Entry and update Account balance's before computation.");
		}

		/*check valid constraints, and apply amounts*/
		this.validate();
		if (this.is_valid_)
			return super.commit();
		return -1;
	}
}

package reman.client.app.finance.accounts;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.journals.Journal;

/**
 * Temporary accounts are accounts which typically represent temporary expenditures on the income statement.  Temporary Accounts
 * will automatically be closed upon being encountered in a statement calculation.  Some example accounts are: expenses (cost of goods sold..),
 * revenues, income distribution (dividends)...
 * 
 * @author Scott
 *
 */
public class TemporaryAccount extends Account {

	/**
	 * The account that this account will be closed into (Income Summary)
	 */
	private Account close_to_acct_;

	/**
	 * For automated temporary account closings. The category to associate the close_to_acct_ journal entry line item
	 * receiving this accounts balance
	 */
	private AcctActionCategory close_to_category_;

	/**
	 * For automated temporary account closings. The category associated with this accounts closing journal entry line item
	 */
	private AcctActionCategory close_from_category_;

	/**
	 * This is the journal where resulting journal entries from closing this account will go
	 */
	private Journal close_to_journal_;

	/**
	 * DatabaseObject use only
	 */
	private TemporaryAccount() {
	}

	/**
	 * Used during the initialization phase of the accounts only.
	 * @param name
	 * @param parent_acct
	 * @param balance
	 * @param close_to_account The account this temporary account will be closed to
	 * @param acct_type
	 * @param normal_balance
	 * @param close_to_category For automated temporary account closings. Must be valid with respect to close_to_accout at time of account closing (statement generation).
	 * 								The category to associate the close_to_acct_ journal entry line item receiving this accounts balance
	 * @param close_from_category For automated temporary account closings. Must be valid with respect to this account's categories at time of closing (statement generation).
	 * 								The category associated with this accounts closing journal entry line item
	 */
	public TemporaryAccount(Integer acct_id, String name, Account parent_acct, double balance,
			Account close_to_acct, AcctType acct_type, TransactionType normal_balance,
			Journal close_to_journal, AcctActionCategory close_to_category,
			AcctActionCategory close_from_category, CashCategory cash_category) {
		super(acct_id, name, parent_acct, acct_type, balance, normal_balance, null, cash_category);

		this.close_to_acct_ = close_to_acct;
		this.close_to_journal_ = close_to_journal;
		this.close_from_category_ = close_from_category;
		this.setCloseToCategory(close_to_category);
	}

	/**
	 * Used during non-initialization phase of accounts.
	 * @param name
	 * @param parent_acct
	 * @param close_to_account The account this temporary account will be closed to
	 * @param acct_type
	 * @param normal_balance
	 * @param close_to_category For automated temporary account closings. Must be valid with respect to close_to_accout at time of account closing (statement generation).
	 * 								The category to associate the close_to_acct_ journal entry line item receiving this accounts balance
	 * @param close_from_category For automated temporary account closings. Must be valid with respect to this account's categories at time of closing (statement generation).
	 * 								The category associated with this accounts closing journal entry line item
	 */
	public TemporaryAccount(Integer acct_id, String name, Account parent_acct, Account close_to_acct,
			AcctType acct_type, TransactionType normal_balance, Journal close_to_journal,
			AcctActionCategory close_to_category, AcctActionCategory close_from_category,
			CashCategory cash_category) {
		this(acct_id, name, parent_acct, 0, close_to_acct, acct_type, normal_balance, close_to_journal,
				close_to_category, close_from_category, cash_category);
	}

	public Account getCloseToAccount() {
		return this.close_to_acct_;
	}

	public Journal getCloseToJournal() {
		return this.close_to_journal_;
	}

	public AcctActionCategory getCloseToCategory() {
		return this.close_to_category_;
	}

	public AcctActionCategory getCloseFromCategory() {
		return this.close_from_category_;
	}

	/**
	 * For automated temporary account closings. Must be valid with respect to close_to_accout at time of account closing (statement generation).
	 * The category to associate the close_to_acct_ journal entry line item receiving this accounts balance
	 * @param category Must be valid with respect to close_to_accout at time of account closing (statement generation).
	 */
	public void setCloseToCategory(AcctActionCategory category) {
		this.close_to_category_ = category;
	}

	/**
	 * For automated temporary account closings. Must be valid with respect to this account's categories at time of closing (statement generation).
	 * The category associated with this accounts closing journal entry line item
	 * @param category Must be valid with respect to this account's categories at time of closing (statement generation).
	 */
	public void setCloseFromCategory(AcctActionCategory category) {
		this.close_from_category_ = category;
	}
}

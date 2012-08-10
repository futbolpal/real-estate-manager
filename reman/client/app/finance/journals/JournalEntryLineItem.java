package reman.client.app.finance.journals;

import java.sql.SQLException;
import java.sql.Timestamp;

import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctActionCategory;
import reman.client.app.finance.accounts.AcctAmount;
import reman.client.app.finance.accounts.exceptions.InvalidAmountException;
import reman.client.app.finance.accounts.exceptions.InvalidCategoryException;
import reman.client.app.finance.gui.JournalEntryLineItemFormPanel;
import reman.client.app.finance.ledger.Ledger;
import reman.client.gui.forms.DboFormPanel;
import reman.common.database.DatabaseObject;
import reman.common.database.ManagedLCDatabaseObject;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.ExceedMaxCommitException;
import reman.common.database.exceptions.LoggedInException;

/**
 * Specific Account objects effected within a JournalEntry, and the amount to apply to the account.  This class also supports
 * AcctActionCategory of the amount applied to further specify each individual action within a JournalEntry.
 * @author Scott
 *
 */
public class JournalEntryLineItem extends ManagedLCDatabaseObject {

	/**
	 * When amount is applied this is true, if inverse applied this becomes false
	 */
	private boolean amount_applied_;

	private Account account_affected_;
	private AcctAmount amount_to_apply_;

	/**
	 * This is the category associated with this line item.
	 * If an account has AcctActionCategories associated with it, this will represent the particular
	 * category from that tree this line item pertains to.
	 */
	private AcctActionCategory category_;

	/**
	 * When this JournalEntryLineItem occurred, set by the JournalEntry containing this JournalEntryLineItem
	 */
	private Timestamp occurred_;

	/**
	 * In order for commit to succeed the amount contained within this JournalEntryLineItem must be applied to the Account contained within this JournalEntryLineItem.
	 * This method will also commit the Account, AcctAmount, and AcctActionCategory upon success.  The Ledger corresponding to the
	 * Account effected will also be updated with this JournalEntryLineItem upon success.
	 * @throws DatabaseException 
	 * @throws SQLException 
	 * @throws InvalidAmountException 
	 * @throws InvalidAmountException
	 */
	@Override
	public long commit() throws InvalidAmountException, ExceedMaxCommitException, SQLException,
			DatabaseException {
		/*amount must be applied for this line item to be committed*/
		if (!this.amount_applied_)
			this.addAmount();

		long return_id = -1;
		Ledger record_ledger = this.account_affected_.getLedger();

		try {
			if (record_ledger.lock()) {
				/*keep the account's ledger up to date*/
				record_ledger.addLineItem(this);

				if ((return_id = super.commit()) >= 0)
					record_ledger.commit();
			}
		} catch (LoggedInException e) {
			// TODO Auto-generated catch block
			record_ledger.retrieve();
			e.printStackTrace();
			throw e;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			record_ledger.retrieve();
			e.printStackTrace();
			throw e;
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			record_ledger.retrieve();
			e.printStackTrace();
			throw e;
		} finally {
			record_ledger.unlock();
		}
		return return_id;
	}

	/**
	 * DatabaseObject use only
	 */
	private JournalEntryLineItem() {
		super(new String[] { "amount_to_apply_", "account_affected_", "category_" }, 1);
	}

	/**
	 * A JournalEntryLineItem must take an <code>amount</code> and apply it to an <code>account</code>, and if AcctActionCategory objects
	 * are present in the <code>account</code> a <code>category</code> must be specified.
	 * @param account Must be managed by the AccountManager.
	 * @param amount The amount to apply to the <code>account</code> once committed.
	 * @param description
	 * @param category This category must be a category in <code>account</code>, or null if <code>account</code> contains no AcctActionCategory objects.
	 * @throws InvalidCategoryException If category is not a valid category of <code>account</code> 
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public JournalEntryLineItem(Account account, AcctAmount amount, String description,
			AcctActionCategory category) throws InvalidCategoryException, SQLException, DatabaseException {
		this();
		this.amount_applied_ = false;
		if (account != null) {
			if (!FinanceManager.instance().getAccountManager().isAccountRegistered(account))
				throw new IllegalArgumentException(
						"Parameter 'acct' of type Account is invalid.  It must be registered in the AccountManager.");
			this.setAccount(account);
			this.setCategory(category);
		}

		this.setAmount(amount);
		super.description_ = description;
	}

	/**
	 * Set the Account object this JournalEntryLineItem will apply the contained AcctAmount to.  The AcctAmount must not already be applied for
	 * this set to succeed.
	 * @param acct
	 */
	public void setAccount(Account acct) {
		if (this.amount_applied_)
			return;
		this.account_affected_ = acct;
	}

	/**
	 * Set the AcctAmount object this JournalEntryLineItem will apply to the contained Account.  The current amount must not already be applied for
	 * this set to succeed.
	 * @param amt
	 */
	public void setAmount(AcctAmount amt) {
		if (this.amount_applied_)
			return;
		this.amount_to_apply_ = amt;
	}

	/**
	 * Set the AcctActionCategory object this JournalEntryLineItem will apply the contained AcctAmount to.  The amount must not already be applied for
	 * this set to succeed.
	 * @param category Must be contained within the Account within this JournalEntryLineItem
	 * @throws InvalidCategoryException If <code>category</code> is not a valid category for the Account within this JournalEntryLineItem
	 * @throws InvalidAmountException
	 */
	public void setCategory(AcctActionCategory category) throws InvalidCategoryException,
			InvalidAmountException {

		if (this.amount_applied_)
			return;

		this.validateCategory(category);

		this.category_ = category;
	}

	/**
	 * If the affected Account contains some categories, then <code>category</code> must be one of those categories.  If there is no categories,
	 * then the category must be null to be valid.
	 * @param acct
	 * @param category
	 * @return
	 * @throws InvalidCategoryException 
	 */
	private void validateCategory(AcctActionCategory category) throws InvalidCategoryException {
		/*if the the account has categories, the category must be in a leaf in the tree
		 *if the account doesn't have categories, the category must be null*/
		if (this.account_affected_.getRootActionCategories().size() > 0) {
			if (!account_affected_.isValidCategory(category)) {
				throw new InvalidCategoryException(category, "The category '" + category
						+ "' is not valid in the Account '" + this.account_affected_.getName() + "'.");
			}
		} else if (category != null) {
			throw new InvalidCategoryException(category, "The Account '"
					+ this.account_affected_.getName() + "' has no categories.");
		}
	}

	/**
	 * Will add the AcctAmount to the Account and AcctActionCategory (if present) contained within this JournalEntryLineItem.
	 * @returns True if the AcctAmount contained in this was applied to the Account contained in this JournalEntryLineItem.
	 * @throws InvalidAmountException If AcctAmount makes this JournalEntryLine item's Account balance negative.
	 */
	public boolean addAmount() throws InvalidAmountException {
		if (!this.amount_applied_) {
			this.account_affected_.getBalanceSystem().addAmount(this.amount_to_apply_);
			if (this.category_ != null) {
				this.category_.getBalanceSystem().addAmount(this.amount_to_apply_, true);
			}
			this.amount_applied_ = true;
		}
		return this.amount_applied_;
	}

	/**
	 * Obtain the normal balance of the Account contained within this JournalEntryLineItem.
	 * @return
	 */
	public TransactionType getAccountNormalBalance() {
		return this.account_affected_.getBalanceSystem().getNormalBalance();
	}

	public double getValue() {
		boolean negative_amt = this.account_affected_.getBalanceSystem().getNormalBalance() != this.amount_to_apply_
				.getTransactionType();
		int mult_factor = (negative_amt) ? -1 : 1;
		return this.amount_to_apply_.getAmount() * mult_factor;
	}

	public String toString() {
		return this.getAccount() + " <-> " + this.getValue();
	}

	/**
	 * Obtain a copy of the AcctAmount contained within this JournalEntryLineItem.
	 * @return
	 */
	public AcctAmount getAmount() {
		return new AcctAmount(this.amount_to_apply_);
	}

	/**
	 * This is the category associated with this JournalEntryLineItem.
	 * If an account has AcctActionCategories associated with it, this will represent the particular
	 * category from that tree this JournalEntryLineItem's AcctAmount pertains to.
	 * @return
	 */
	public AcctActionCategory getCategory() {
		return this.category_;
	}

	/**
	 * Obtain a reference to the Account contained within this JournalEntryLineItem.
	 * @return
	 */
	public Account getAccount() {
		return this.account_affected_;
	}

	/**
	 * Used by the JournalEntry object that contains this JournalEntryLineItem
	 * @param occurred
	 */
	void setTime(Timestamp occurred) {
		this.occurred_ = occurred;
	}

	/**
	 * Get the time which this journal entry occurred.
	 * @return The time which this journal entry occurred.
	 */
	public Timestamp getTime() {
		return this.occurred_;
	}

	/*GUI SUPPORT*/
	public DboFormPanel<JournalEntryLineItem> getFormPanel(String name,
			DboFormPanel<? extends DatabaseObject> parent, boolean read_only) throws Exception {
		return new JournalEntryLineItemFormPanel(name, this, parent, read_only);
	}
	/*GUI SUPPORT*/
}

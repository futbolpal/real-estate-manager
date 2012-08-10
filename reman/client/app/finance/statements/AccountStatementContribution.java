package reman.client.app.finance.statements;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctAmount;
import reman.client.app.finance.accounts.CashCategory;
import reman.client.app.finance.accounts.TemporaryAccount;
import reman.client.app.finance.accounts.exceptions.InvalidAmountException;
import reman.client.app.finance.accounts.exceptions.InvalidCategoryException;
import reman.client.app.finance.accounts.exceptions.UnknownNodeException;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.finance.journals.exceptions.InvalidJournalEntryException;
import reman.common.database.exceptions.DatabaseException;

/**
 * Consists of a collection of AccountStatementLineItem objects.  Calculation of this AccountStatementContribution will tabulate contributions
 * from leaf nodes back up to this node, accumulating the final contribution along the way.
 * @author Scott
 *
 */
public class AccountStatementContribution extends StatementContribution {

	/**
	 * DatabaseObject use only
	 */
	private AccountStatementContribution() {
	}

	/**
	 * 
	 * @param contribution_name A name for display and identification purposes.
	 * @param result_description Information as to what the resulting calculated result represents.
	 * @param normal_contribution What transaction type the final calculated result will be viewed as.
	 * @param parent
	 */
	public AccountStatementContribution(String contribution_name, String result_description,
			TransactionType normal_contribution, AccountStatementContribution parent) {
		super(contribution_name, result_description, normal_contribution, parent);
	}

	/**
	 * Add an AccountStatementLineItem to this AccountStatementContribution.
	 * @param line_item Must be of type AccountStatementLineItem.
	 * @return True if <code>line_item</code> was added.
	 * @throws SQLException 
	 * @throws DatabaseException 
	 * @throws FinanceException 
	 */
	@Override
	public boolean addLineItem(StatementLineItem line_item) throws FinanceException,
			DatabaseException, SQLException {
		if (!(line_item instanceof AccountStatementLineItem))
			return false;
		return super.addLineItem(line_item);
	}

	/**
	 * Accounts are the only factors in this calculation, they are calculated() in a leaf to root manner.  Each AccountStatementContribution's calculated contribution will
	 * be the result of all children AccountStatementContribution and all directly contained AccountStatementLineItem objects.
	 * <br/>The summation of all contained Account balances makes the contribution of an AccountStatementContribution which is then factored in at the next level (toward root).
	 * <br/>TemporaryAccounts <strong>will</strong> be closed as they are encountered, and so all relative members (Close to Account, Close to Journal, Close to Category, Close from Category) in each TemporaryAccount <strong>must</strong> be set before this method executes.
	 * @return  The final calculated tabulated amount this contribution offers to the containing Statement.
	 * @throws SQLException 
	 * @throws DatabaseException 
	 * @throws UnknownNodeException 
	 * @throws InvalidCategoryException 
	 * @throws InvalidAmountException 
	 * @throws InvalidJournalEntryException 
	 * @throws InvalidAmountException 
	 */
	@Override
	AcctAmount calculate(boolean end_fiscal_year) throws InvalidJournalEntryException,
			InvalidAmountException, InvalidCategoryException, UnknownNodeException, DatabaseException,
			SQLException {
		this.balance_system_.zeroAmount();

		/*account for each child contribution*/
		for (StatementContribution controbution : this.getChildren()) {
			this.balance_system_.addAmount(controbution.calculate(end_fiscal_year), true);
		}

		/*take all accounts within this contribution into account*/
		for (StatementLineItem line_item : this.line_items_) {
			AccountStatementLineItem acct_line_item = (AccountStatementLineItem) line_item;
			Account curr_acct = acct_line_item.getAccount();
			this.balance_system_.addAmount(curr_acct.getBalanceSystem().getAcctAmount(), true);
			if (curr_acct instanceof TemporaryAccount && end_fiscal_year) {
				TemporaryAccount temp_acct = (TemporaryAccount) curr_acct;
				FinanceManager.instance().getAccountManager().closeAccount(temp_acct);
			}
		}

		return this.balance_system_.getAcctAmount();
	}

	public Collection<AccountStatementLineItem> getLineItems(CashCategory cash) {
		Collection<AccountStatementLineItem> col = new ArrayList<AccountStatementLineItem>();

		for (StatementContribution cont : this.getChildren()) {
			AccountStatementContribution a_cont = (AccountStatementContribution) cont;
			col.addAll(a_cont.getLineItems(cash));
		}

		for (StatementLineItem line_item : this.line_items_) {
			AccountStatementLineItem a_item = (AccountStatementLineItem) line_item;
			if (a_item.getAccount().getCashCategory() == cash)
				col.add(a_item);
		}

		return col;
	}
}

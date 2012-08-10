package reman.client.app.finance.statements;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctTimeScale;
import reman.client.app.finance.accounts.AcctType;
import reman.client.app.finance.accounts.CashCategory;
import reman.common.database.exceptions.DatabaseException;

/**
 * The Balance Sheet is one of 4 fundamental statements required by the GAAP.  The Balance Statement is a view at all Accounts grouped by
 * AcctType (Asset,Liability,Equity) and AcctTimeScale (Long,Short).  The total final calculated amount of the Balance Sheet should
 * always be 0.
 * @author Scott
 *
 */
public class BalanceSheet extends AccountStatement {

	private AccountStatementContribution st_asset_cont_;
	private AccountStatementContribution lt_asset_cont_;

	private AccountStatementContribution st_liability_cont_;
	private AccountStatementContribution lt_liability_cont_;

	private AccountStatementContribution equity_cont_;

	protected BalanceSheet() {
	}

	/**
		 * This will automatically construct a Statement which reflects all Account objects registered with this financial engine.
		 * @throws DatabaseException
		 * @throws SQLException
		 */
	public BalanceSheet(Timestamp end_time) throws DatabaseException, SQLException {
		super("Balance Sheet", TransactionType.DEBIT, end_time);/*transaction type doesn't matter because balance is not used (should be 0)*/

		AccountStatementContribution asset_cont = new AccountStatementContribution("Assets",
				"Total Assets", TransactionType.DEBIT, null);
		st_asset_cont_ = new AccountStatementContribution("Current Assets", "Total Current Assets",
				TransactionType.DEBIT, asset_cont);
		lt_asset_cont_ = new AccountStatementContribution("Fixed Assets", "Total Fixed Assets",
				TransactionType.DEBIT, asset_cont);

		AccountStatementContribution liability_equity_cont = new AccountStatementContribution(
				"Liabilities & Equity", "Total Liabilities & Equity", TransactionType.CREDIT, null);

		AccountStatementContribution liability_cont = new AccountStatementContribution("Liabilities",
				"Total Liabilities", TransactionType.CREDIT, liability_equity_cont);
		st_liability_cont_ = new AccountStatementContribution("Current Liabilities",
				"Total Current Liabilities", TransactionType.CREDIT, liability_cont);
		lt_liability_cont_ = new AccountStatementContribution("Long-Term Liabilities",
				"Total Long-Term Liabilities", TransactionType.CREDIT, liability_cont);

		/*equity_cont_ = new AccountStatementContribution("Owner's Equity", "Total Owner's Equity",
				TransactionType.CREDIT, liability_equity_cont); TODO:handle escape characters in DatabaseObject commit*/
		equity_cont_ = new AccountStatementContribution("Owners Equity", "Total Owners Equity",
				TransactionType.CREDIT, liability_equity_cont);

		Collection<Account> root_asset_accts = FinanceManager.instance().getAccountManager()
				.getRootAcctsOfSubType(AcctType.ASSET);
		Collection<Account> root_liability_accts = FinanceManager.instance().getAccountManager()
				.getRootAcctsOfSubType(AcctType.LIABILITY);
		Collection<Account> root_equity_accts = FinanceManager.instance().getAccountManager()
				.getRootAcctsOfSubType(AcctType.EQUITY);

		/*populate each contribution with all corresponding accounts*/
		AccountStatementHelper.buildRootContribution(root_asset_accts, st_asset_cont_,
				AcctTimeScale.SHORT_TERM);
		AccountStatementHelper.buildRootContribution(root_asset_accts, lt_asset_cont_,
				AcctTimeScale.LONG_TERM);

		AccountStatementHelper.buildRootContribution(root_liability_accts, st_liability_cont_,
				AcctTimeScale.SHORT_TERM);
		AccountStatementHelper.buildRootContribution(root_liability_accts, lt_liability_cont_,
				AcctTimeScale.LONG_TERM);

		AccountStatementHelper.buildRootContribution(root_equity_accts, equity_cont_, null);

		/*add filled contributions to the statement*/
		super.addContribution(asset_cont);
		super.addContribution(st_asset_cont_);
		super.addContribution(lt_asset_cont_);

		super.addContribution(liability_equity_cont);

		super.addContribution(liability_cont);
		super.addContribution(st_liability_cont_);
		super.addContribution(lt_liability_cont_);

		super.addContribution(equity_cont_);
	}

	public AccountStatementContribution getContribution(AcctType type, AcctTimeScale t_scale) {
		if (type == AcctType.ASSET) {
			if (t_scale == AcctTimeScale.SHORT_TERM) {
				return this.st_asset_cont_;
			} else if (t_scale == AcctTimeScale.LONG_TERM) {
				return this.lt_asset_cont_;
			}
		} else if (type == AcctType.LIABILITY) {
			if (t_scale == AcctTimeScale.SHORT_TERM) {
				return this.st_liability_cont_;
			} else if (t_scale == AcctTimeScale.LONG_TERM) {
				return this.lt_liability_cont_;
			}
		} else if (type == AcctType.EQUITY) {
			return this.equity_cont_;
		}
		return null;
	}

	public Collection<AccountStatementLineItem> getLineItems(CashCategory cash) {
		Collection<AccountStatementLineItem> col = new ArrayList<AccountStatementLineItem>();
		for (StatementContribution cont : this.root_contributions_) {
			AccountStatementContribution a_cont = (AccountStatementContribution) cont;
			col.addAll(a_cont.getLineItems(cash));
		}
		return col;
	}

	/**
	 * This method will not add a contribution, the contributions are automatically generated based on BalanceSheet properties
	 * upon instantiation.
	 */
	@Override
	public boolean addContribution(StatementContribution sc) {
		return false;
	}
}

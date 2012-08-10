package reman.client.app.finance.statements;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctBalanceSystem;
import reman.client.app.finance.exceptions.FinanceException;
import reman.common.database.exceptions.DatabaseException;

public class RetainedEarningsStatement extends Statement {

	protected RetainedEarningsStatement() {
	}

	public RetainedEarningsStatement(AcctBalanceSystem net_income, Timestamp end_time)
			throws DatabaseException, SQLException {
		super("Statement of Retained Earnings", TransactionType.CREDIT, end_time);
		this.description_ = "Ending Retained Earnings";
		/*Ending Retained Earnings = Beginning Retained Earnings - Investments - Dividends Paid + Net Income
		*Depreciation Expense -> Income Summary
		*Expenses and Revenues -> Income Summary
		*Dividends Paid -> Ending Retained Earnings
		*Income Summary -> Ending Retained Earnings*/
		Account retained_earnings = FinanceManager.instance().getAccountManager().getAccount(
				"Retained Earnings");
		Account dividends_paid_root = FinanceManager.instance().getAccountManager().getAccount(
				"Dividends Paid");

		if (retained_earnings == null || dividends_paid_root == null)
			throw new FinanceException(this,
					"Accounts 'Retained Earnings','Dividends Paid' must be registered.");

		BalanceSystemContribution re_cont = new BalanceSystemContribution("Begining Retained Earnings",
				"Begining Retained Earnings", TransactionType.CREDIT, null);
		BalanceSystemContribution div_cont = new BalanceSystemContribution("Dividends Paid",
				"Total Dividends Paid", TransactionType.DEBIT, null);
		BalanceSystemContribution net_inc_cont = new BalanceSystemContribution("Net Income",
				"Net Income", TransactionType.CREDIT, null);

		re_cont.addLineItem(new BalanceSystemLineItem(retained_earnings.toString(), retained_earnings
				.getBalanceSystem()));
		net_inc_cont.addLineItem(new BalanceSystemLineItem("Net Income", net_income));

		Collection<Account> dummy_list = new ArrayList<Account>();
		dummy_list.add(dividends_paid_root);

		AccountStatementHelper.buildRootContribution(dummy_list, div_cont, null);
		super.addContribution(re_cont);
		super.addContribution(net_inc_cont);
		super.addContribution(div_cont);
	}

	/**
	 * This method will not add a contribution, the contributions are automatically generated based on RetainedEarningsStatement properties
	 * upon instantiation.
	 */
	@Override
	public boolean addContribution(StatementContribution sc) {
		return false;
	}

	@Override
	protected float[] getColumnWidths() {
		final float[] column_widths = { 85f, 15f };
		return column_widths;
	}

	@Override
	protected String[] getColumnNames() {
		final String[] column_names = new String[] { "Category", "Amount" };
		return column_names;
	}
}

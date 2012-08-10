package reman.client.app.finance.statements;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;

import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctType;
import reman.common.database.exceptions.DatabaseException;

/**
 * Statement which reports Revenues-Expenditures which produces net income
 * @author Scott
 *
 */
public class IncomeStatement extends AccountStatement {

	protected IncomeStatement() {
	}

	public IncomeStatement(Timestamp end_time) throws DatabaseException, SQLException {
		super("Income Statement", TransactionType.CREDIT, end_time);
		this.description_ = "Net Income";

		AccountStatementContribution commission_cont = new AccountStatementContribution("Commission",
				"Total Commission", TransactionType.CREDIT, null);
		AccountStatementContribution other_cont = new AccountStatementContribution("Other Income",
				"Total Other Income", TransactionType.CREDIT, null);

		AccountStatementContribution rev_cont = new AccountStatementContribution("Revenue",
				"Total Revenue", TransactionType.CREDIT, null);
		AccountStatementContribution exp_cont = new AccountStatementContribution("Expenses",
				"Total Expenses", TransactionType.DEBIT, null);
		AccountStatementContribution gains_cont = new AccountStatementContribution("Gains",
				"Total Gains", TransactionType.CREDIT, null);
		AccountStatementContribution losses_cont = new AccountStatementContribution("Losses",
				"Total Losses", TransactionType.DEBIT, null);

		Collection<Account> root_comm_accts = FinanceManager.instance().getAccountManager()
				.getRootAcctsOfSubType(AcctType.COMMISSION);
		Collection<Account> root_sales_accts = FinanceManager.instance().getAccountManager()
		.getRootAcctsOfSubType(AcctType.SALES);
		Collection<Account> root_other_accts = FinanceManager.instance().getAccountManager()
				.getRootAcctsOfSubType(AcctType.OTHER_INCOME);
		Collection<Account> root_rev_accts = FinanceManager.instance().getAccountManager()
				.getRootAcctsOfSubType(AcctType.REVENUE);
		Collection<Account> root_exp_accts = FinanceManager.instance().getAccountManager()
				.getRootAcctsOfSubType(AcctType.EXPENSE);
		Collection<Account> root_gain_accts = FinanceManager.instance().getAccountManager()
				.getRootAcctsOfSubType(AcctType.GAIN);
		Collection<Account> root_loss_accts = FinanceManager.instance().getAccountManager()
				.getRootAcctsOfSubType(AcctType.LOSS);

		/*populate each contribution with all corresponding accounts*/
		AccountStatementHelper.buildRootContribution(root_comm_accts, commission_cont, null);
		AccountStatementHelper.buildRootContribution(root_other_accts, other_cont, null);
		AccountStatementHelper.buildRootContribution(root_sales_accts, other_cont, null);
		AccountStatementHelper.buildRootContribution(root_rev_accts, rev_cont, null);
		AccountStatementHelper.buildRootContribution(root_exp_accts, exp_cont, null);
		AccountStatementHelper.buildRootContribution(root_gain_accts, gains_cont, null);
		AccountStatementHelper.buildRootContribution(root_loss_accts, losses_cont, null);

		/*add filled contributions to the statement*/
		super.addContribution(commission_cont);
		super.addContribution(other_cont);
		super.addContribution(rev_cont);
		super.addContribution(exp_cont);
		super.addContribution(gains_cont);
		super.addContribution(losses_cont);
	}

	/**
	 * This method will not add a contribution, the contributions are automatically generated based on IncomeStatment properties
	 * upon instantiation.
	 */
	@Override
	public boolean addContribution(StatementContribution sc) {
		return false;
	}
}

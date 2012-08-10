package reman.client.app.finance.statements;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctBalanceSystem;
import reman.client.app.finance.accounts.TemporaryAccount;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.finance.timer.TimeEvent;
import reman.client.app.finance.timer.TimeEventManager;
import reman.client.app.finance.timer.TimeListener;
import reman.client.app.finance.timer.TimeNotificationEntry;
import reman.common.database.DatabaseObject;
import reman.common.database.OfficeProjectManager;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.NotPersistedException;

/**
 * This class provides maintains a collection of Statement objects.  This class provides functionality to register a Statement and in this
 * registration process attempt to patch the Statement chain (by maintaining the preceding/succeeding Statement of the same type).
 * @author Scott
 *
 */
public class StatementManager extends DatabaseObject implements TimeListener {

	/**
	 * Contains statement list
	 */
	private ArrayList<Statement> statements_;

	public StatementManager() throws DatabaseException, SQLException {
		this.statements_ = new ArrayList<Statement>();

		TimeNotificationEntry end_of_year = new TimeNotificationEntry(this, new Timestamp(
				TimeEventManager.getLastOfYear().getTimeInMillis()));
		OfficeProjectManager.instance().getCurrentProject().getTimeEventManager().register(end_of_year);
	}

	/**
	 * Register <code>statement</code> with this StatementManager.  This will automatically commit and finalize the <code>statement</code>.
	 * @param statement
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public Statement registerStatement(Statement statement) throws DatabaseException, SQLException {
		this.retrieve();

		if (FinanceManager.instance().isInitializationPhase())
			throw new FinanceException(this,
					"The finance engine can not register a statement while in initialization phase.");

		/*do not manage this statement if it is already managed*/
		if (this.isManagedStatement(statement))
			return statement;

		Statement preceding_statement = null;
		StatementManager preceding_statement_manager = null;
		try {
			if (this.lock() && statement.lock()) {
				/*Search for most recent same statement within this statement manager*/
				preceding_statement = FinanceManager.instance().getPrecedingStatement(statement,
						preceding_statement_manager);

				/*preserve the statement chain*/
				if (preceding_statement != null && preceding_statement.lock()) {
					preceding_statement.setSucceedingStatement(statement);
					preceding_statement.commit();

					/*if preceding statement is not from this year, then the preceding_statement_manager need to be committed*/
					if (preceding_statement_manager != null && preceding_statement_manager != this
							&& preceding_statement_manager.lock()) {
						preceding_statement_manager.commit();
					}
				}

				statement.setPrecedingStatement(preceding_statement);
				statement.setFinialized(true);
				try {
					statement.commit();
				} catch (DatabaseException e) {
					/*if any error occurs then this statement should not be finalized*/
					statement.setFinialized(false);
					throw e;
				} catch (SQLException e) {
					statement.setFinialized(false);
					throw e;
				}

				this.statements_.add(statement);
				this.commit();

				return statement;
			}
		} catch (DatabaseException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		} finally {
			if (preceding_statement != null) {
				preceding_statement.unlock();
				if (preceding_statement_manager != null && preceding_statement_manager != this)
					preceding_statement_manager.unlock();
			}
			statement.unlock();
			this.unlock();
		}

		return null;
	}

	/**
	 * If all information is provided a statement will be generated, and registered with this StatementManager.  The result of this statement
	 * can then be obtained through the methods provided in the AccountStatement return value. The end time is assumed as now.
	 * @param statement_name Used to identify the previous statement
	 * @param root_contributions
	 * @param statement_norm_balance
	 * @param begin_time
	 * @return The resulting AccountStatement which is registered with this StatementManager.
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	public AccountStatement createAccountStatement(String statement_name,
			Collection<AccountStatementContribution> root_contributions,
			TransactionType statement_norm_balance, Timestamp end_time) throws DatabaseException,
			SQLException {

		AccountStatement acct_state = new AccountStatement(statement_name, statement_norm_balance,
				end_time);

		for (AccountStatementContribution contribution : root_contributions)
			acct_state.addContribution(contribution);

		return (AccountStatement) this.registerStatement(acct_state);
	}

	/*TODO: build statement generation methods for:
	 * Income Statement (revenue-expenses),
	 * Retained Earnings Statement(ending re = begin+net income-dividends-loss),
	 * Balance Sheet (Assets: Long term then Short term, Liabilities Long term then Short term, Equity*/

	/**
	 * This will generate a category statement based of the AcctActionCategory objects contained within <code>acct_with_categories</code>.  The end time is assumed as now.
	 * @param statement_name
	 * @param acct_with_categories Account which contains AcctActionCategory tree structure to generate a Statement based off of.
	 * @return
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	public CategoryStatement createCategoryStatement(String statement_name,
			Account acct_with_categories, Timestamp end_time) throws DatabaseException, SQLException {

		CategoryStatement cat_state = new CategoryStatement(statement_name, acct_with_categories,
				end_time);

		return (CategoryStatement) this.registerStatement(cat_state);
	}

	/**
	 * Get the most recent (with respect to time) Statement in statement_list with same Statement type as <code>statement</code>.
	 * @param statement
	 * @return The most recent (with respect to time) Statement with matching type, null if none found.
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	public Statement getMostRecent(Statement statement) throws DatabaseException, SQLException {
		this.retrieve();

		for (Statement s : this.statements_) {
			/*if the run time classes are compatible, i.e. only AccountStatements precede AccountStatements */
			if (s.getClass().isInstance(statement)) {
				if (s.getName() == statement.getName()) {
					/*the statement types are compatible, follow the successor statement until the most recent statement found*/
					Statement most_recent = s;
					while (most_recent.getSucceedingStatement() != null) {
						most_recent = most_recent.getSucceedingStatement();
					}
					return most_recent;
				}
			}
		}
		return null;
	}

	/**
	 * Obtain a collection of this StatementManager's Statements.
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public ArrayList<Statement> getStatements() throws DatabaseException, SQLException {
		this.retrieve();

		return new ArrayList<Statement>(this.statements_);
	}

	/**
	 * Obtain a Statement named <code>statement_name</code> with begin time of <code>begin_time</code>
	 * @param statement_name
	 * @param begin_time
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public Statement getStatement(String statement_name, Timestamp begin_time)
			throws DatabaseException, SQLException {
		this.retrieve();

		for (Statement s : this.statements_) {
			if (s.getName() == statement_name && s.getBeginTime() == begin_time)
				return s;
		}
		return null;
	}

	/**
	 * Determine if <code>s<code> is known about and registered with this StatementManager.
	 * @param s
	 * @return
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	public boolean isManagedStatement(Statement s) throws DatabaseException, SQLException {
		if (s == null)
			return false;

		this.retrieve();

		for (Statement curr_statement : this.statements_) {
			if (curr_statement == s)
				return true;
		}

		return false;
	}

	/**
	 * Required accounts: Income Summary, Retained Earnings, Dividends Paid
	 * @param end_time
	 * @param end_fiscal_year
	 * @throws FinanceException
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public void generateDefaultStatements(Timestamp end_time) throws FinanceException,
			DatabaseException, SQLException {
		this.generateDefaultStatements(end_time, false);
	}

	/**
	 * Required accounts: Income Summary, Retained Earnings, Dividends Paid
	 * @param end_time
	 * @param end_fiscal_year
	 * @throws FinanceException
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public void generateDefaultStatements(Timestamp end_time, boolean end_fiscal_year)
			throws FinanceException, DatabaseException, SQLException {

		try {
			if (this.lock()) {
				IncomeStatement is = new IncomeStatement(end_time);
				is.calculate(end_fiscal_year);
				AcctBalanceSystem net_income = is.getFinalAmount();

				if (end_fiscal_year) {
					/*close income_summary to RE*/
					TemporaryAccount income_summary = (TemporaryAccount) FinanceManager.instance()
							.getAccountManager().getAccount("Income Summary");
					FinanceManager.instance().getAccountManager().closeAccount(income_summary);
				}

				RetainedEarningsStatement res = new RetainedEarningsStatement(net_income, end_time);

				BalanceSheet bs = new BalanceSheet(end_time);
				bs.calculate(end_fiscal_year);

				BalanceSheet p_bs = (BalanceSheet) this.getMostRecent(bs);
				CashFlowStatement cfs = new CashFlowStatement(net_income, p_bs, bs, end_time);

				this.registerStatement(is);
				this.registerStatement(res);
				this.registerStatement(bs);
				this.registerStatement(cfs);

				try {
					is.exportToPdf();
					res.exportToPdf();
					bs.exportToPdf();
					cfs.exportToPdf();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		} catch (DatabaseException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		} finally {
			this.unlock();
		}
	}

	@Override
	public void retrieve() throws DatabaseException, SQLException {
		try {
			super.retrieve();
		} catch (NotPersistedException e) {
			/*because this manager will automate a retrieve, disregard a not in database exception*/
		}
	}

	@Override
	public void timeEventOccurred(TimeEvent e) {
		try {
			this.generateDefaultStatements(e.getEntry().getNotifyTime(), true);
		} catch (FinanceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}

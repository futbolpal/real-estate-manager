package reman.client.app.finance;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;

import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AccountManager;
import reman.client.app.finance.accounts.AcctAmount;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.finance.exceptions.NameAlreadyExistsException;
import reman.client.app.finance.journals.Journal;
import reman.client.app.finance.journals.JournalManager;
import reman.client.app.finance.ledger.LedgerManager;
import reman.client.app.finance.statements.BalanceSheet;
import reman.client.app.finance.statements.Statement;
import reman.client.app.finance.statements.StatementManager;
import reman.common.database.DatabaseObject;
import reman.common.database.OfficeProjectManager;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.LoggedInException;
import reman.common.database.exceptions.NotPersistedException;

/**
 * This is the entry point into the financial engine.
 * <br/>This class maintains and AccountManager and a LedgerManager.
 * <br/>This class maintains maps (indexed by fiscal year) of JournalManager and StatementManager.
 * @author Scott
 *
 */
public class FinanceManager extends DatabaseObject {
	/**
	 * if this is true, then account balances are allowed to be specified upon creating a new account
	 */
	private boolean init_phase_;

	/**
	 * Indexed by the year
	 */
	private Hashtable<Integer, JournalManager> journal_managers_;
	/**
	 * The account manager persists throughout the years
	 */
	private AccountManager account_manager_;

	/**
	 * Indexed managers by year
	 */
	private Hashtable<Integer, LedgerManager> ledger_managers_;
	/**
	 * Indexed by the year
	 */
	private Hashtable<Integer, StatementManager> statement_managers_;

	/**
	 * Initialize a fresh FinanceManager.
	 */
	public FinanceManager() {
		this.journal_managers_ = new Hashtable<Integer, JournalManager>();
		this.statement_managers_ = new Hashtable<Integer, StatementManager>();
		this.ledger_managers_ = new Hashtable<Integer, LedgerManager>();
		this.account_manager_ = new AccountManager();
		init_phase_ = true;
	}

	/**
	 * Obtain the singleton instance of the FinanceManager.
	 * @return
	 */
	public static FinanceManager instance() {
		/*TODO: HACK...change all references to this function to OfficeProjectManager.instance().getCurrentProject().getFinanceManager()*/
		return OfficeProjectManager.instance().getCurrentProject().getFinanceManager();
	}

	/**
	 * Get the JournalManager for the current year.
	 * @return
	 * @throws DatabaseException 
	 * @throws SQLException 
	 */
	public JournalManager getJournalManager() throws SQLException, DatabaseException {
		return this.getJournalManager(Calendar.getInstance().get(Calendar.YEAR));
	}

	/**
	 * Get the JournalManager for the corresponding year.
	 * @param year
	 * @return
	 * @throws DatabaseException 
	 * @throws SQLException 
	 */
	public JournalManager getJournalManager(Integer year) throws SQLException, DatabaseException {
		this.retrieve();

		if (this.journal_managers_.get(year) == null
				&& year == Calendar.getInstance().get(Calendar.YEAR))
			this.createNewManagers();

		return this.journal_managers_.get(year);
	}

	/**
	 * Create new non-singleton managers for the current year.
	 * @throws DatabaseException 
	 * @throws SQLException 
	 */
	private void createNewManagers() throws SQLException, DatabaseException {
		this.createNewManagers(Calendar.getInstance().get(Calendar.YEAR));
	}

	/**
	 * Create new JournalManager and StatementManager.
	 * The new JournalManager will inherit all of the journals from the previous year's JournalManager
	 * @param year
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	private void createNewManagers(Integer year) throws SQLException, DatabaseException {

		this.retrieve();

		try {
			if (this.lock()) {
				if (this.ledger_managers_.get(year) == null) {
					LedgerManager new_manager = new LedgerManager();

					LedgerManager previous_manager = this.ledger_managers_.get(year - 1);
					if (previous_manager != null) {
						/*populate current manager with same ledgers as from previous year*/
						try {
							if (new_manager.lock()) {
								for (Account acct : this.account_manager_.getAllAccounts()) {
									try {
										new_manager.createLedger(acct, false);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}

								new_manager.commit();
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							new_manager.unlock();
						}
					}
					this.ledger_managers_.put(year, new_manager);
				}

				if (this.journal_managers_.get(year) == null) {
					JournalManager new_manager = new JournalManager();

					/*populate the new manager with the same Journals that the previous JournalManager had*/
					JournalManager previous_manager = this.journal_managers_.get(year - 1);
					if (previous_manager != null) {
						Hashtable<String, Journal> previous_journals;
						try {
							previous_journals = previous_manager.getJournals();
							for (Journal j : previous_journals.values()) {
								try {
									new_manager.createJournal(j.getName());
								} catch (NameAlreadyExistsException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (LoggedInException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (DatabaseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} catch (DatabaseException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					this.journal_managers_.put(year, new_manager);
				}
				if (this.statement_managers_.get(year) == null) {
					StatementManager sm = new StatementManager();
					this.statement_managers_.put(year, sm);
				}
			}
			this.commit();
		} catch (LoggedInException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} finally {
			this.unlock();
		}
	}

	/**
	 * Get the AccountManager
	 * @return
	 */
	public AccountManager getAccountManager() {
		try {
			this.retrieve();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return this.account_manager_;
	}

	/**
	 * Get the LedgerManager
	 * @return
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	public LedgerManager getLedgerManager() throws DatabaseException, SQLException {
		return this.getLedgerManager(Calendar.getInstance().get(Calendar.YEAR));
	}

	/**
	 * Get the ledger manager for the <code>year</code>
	 * @param year
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public LedgerManager getLedgerManager(Integer year) throws DatabaseException, SQLException {
		this.retrieve();

		if (this.ledger_managers_.get(year) == null
				&& year == Calendar.getInstance().get(Calendar.YEAR))
			this.createNewManagers();

		return this.ledger_managers_.get(year);
	}

	/**
	 * Get the StatementManager for the current year
	 * @return
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	public StatementManager getStatementManager() throws DatabaseException, SQLException {
		return this.getStatementManager(Calendar.getInstance().get(Calendar.YEAR));
	}

	/**
	 * Get the StatementManager for the corresponding <code>year</code>.
	 * @param year
	 * @return
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	public StatementManager getStatementManager(Integer year) throws DatabaseException, SQLException {
		this.retrieve();

		if (this.statement_managers_.get(year) == null
				&& year == Calendar.getInstance().get(Calendar.YEAR))
			this.createNewManagers();

		return this.statement_managers_.get(year);
	}

	/**
	 * Finds the statement which immediately precedes <code>curr_statement</code> in time.
	 * @param curr_statement The statement for which to find a preceding statement for.
	 * @param contained_manager  The statement manager for which the preceding statement resides in, this will be set by this method
	 * 							upon finding the preceding statement return value.
	 * @return The preceding statement to <code>curr_statement</code> or null if not found.
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public Statement getPrecedingStatement(Statement curr_statement,
			StatementManager contained_manager) throws DatabaseException, SQLException {
		this.retrieve();

		ArrayList<Integer> all_s_manager_keys = new ArrayList<Integer>(this.statement_managers_
				.keySet());
		/*sort in such a way that most recent years are first*/
		Collections.sort(all_s_manager_keys, Collections.reverseOrder());

		Statement preceding_statement = null;
		/*search all managers, until the most recent (preceding to curr_statement) statement is found*/
		for (Integer curr_year : all_s_manager_keys) {
			StatementManager curr_manager = this.statement_managers_.get(curr_year);
			preceding_statement = curr_manager.getMostRecent(curr_statement);
			if (preceding_statement != null) {
				contained_manager = curr_manager;
				break;
			}
		}

		return preceding_statement;
	}

	/**
	 * If the financial engine is in the initialization phase or not.  While in the initialization phase Journals can not accumulated JournalEntry objects,
	 * and Account balances can be specified upon creation.
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public boolean isInitializationPhase() throws DatabaseException, SQLException {
		this.retrieve();
		return this.init_phase_;
	}

	/**
	 * Turn off (and commit) initialization phase for the finance manager.  Balance sheet final amount must sum to 0.
	 * @throws DatabaseException 
	 * @throws SQLException 
	*/
	public boolean turnOffInitializationPhase() throws SQLException, DatabaseException {
		this.retrieve();
		if (!this.init_phase_)
			return true;
		/*ensure the balance sheet is balanced before initialization phase can be turned off*/
		try {
			if (this.lock()) {
				BalanceSheet bs = new BalanceSheet(null);
				AcctAmount result_bs = bs.getFinalAmount().getAcctAmount();
				if (result_bs.getAmount() == 0)
					this.init_phase_ = false;
				else
					throw new FinanceException(this, "The accounts must initially be balanced.");

				this.commit();
				return true;
			}
		} catch (LoggedInException e) {
			// TODO Auto-generated catch block
			this.init_phase_ = true;
			e.printStackTrace();
			throw e;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			this.init_phase_ = true;
			e.printStackTrace();
			throw e;
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			this.init_phase_ = true;
			e.printStackTrace();
			throw e;
		} finally {
			this.unlock();
		}
		return false;
	}

	@Override
	public void retrieve() throws DatabaseException, SQLException {
		try {
			super.retrieve();
		} catch (NotPersistedException e) {
			/*because this manager will automate a retrieve, disregard a not in database exception*/
		}
	}
}

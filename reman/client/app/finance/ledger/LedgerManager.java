package reman.client.app.finance.ledger;

import java.sql.SQLException;
import java.util.Hashtable;

import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.accounts.Account;
import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.LoggedInException;
import reman.common.database.exceptions.NotPersistedException;

/**
 * This class will manage one Ledger for each Account.  This class is necessary because Ledgers can not be directly contained within
 * their associated Accounts because that would create a recursive commit situation (Ledger contains JournalEntryLineItems and JournalEntryLineItems contains an Account which contain Ledgers).
 * @author Scott
 *
 */
public class LedgerManager extends DatabaseObject {

	private Hashtable<Account, Ledger> ledgers_;

	public LedgerManager() {
		this.ledgers_ = new Hashtable<Account, Ledger>();
	}

	public Ledger createLedger(Account acct) throws DatabaseException, SQLException {
		return this.createLedger(acct, true);
	}

	/**
	 * This creates and registers a Ledger for <code>acct</code>.
	 * @param acct
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public Ledger createLedger(Account acct, boolean automate_commit) throws DatabaseException, SQLException {

		Ledger acct_ledger = this.getLedger(acct);
		if (acct_ledger == null) {
			try {
				if (!automate_commit || this.lock()) {
					acct_ledger = new Ledger(acct);
					acct_ledger.commit();

					this.ledgers_.put(acct, acct_ledger);

					if (automate_commit)
						this.commit();
				}
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
				if (automate_commit)
					this.unlock();
			}
		}

		return acct_ledger;
	}

	/**
	 * This method should only be used by the AccountManager.  This will remove the Ledger associated with <code>acct</code> from this LedgerManager.
	 * This method only works during initialization phase.
	 * @param acct
	 * @return
	 * @throws LoggedInException
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public Ledger removeLedger(Account acct) throws LoggedInException, DatabaseException,
			SQLException {
		Ledger removed_ledger = null;
		if (FinanceManager.instance().isInitializationPhase()) {
			/*the account manager must not have account 'acct' registered*/
			if (FinanceManager.instance().getAccountManager().isAccountRegistered(acct))
				return null;

			this.retrieve();

			try {
				if (this.lock()) {
					removed_ledger = this.ledgers_.remove(acct);
					this.commit();
				}
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

		return removed_ledger;
	}

	/**
	 * Obtain the ledger assocaited with <code>acct</code>
	 * @param acct
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public Ledger getLedger(Account acct) throws DatabaseException, SQLException {
		this.retrieve();

		return this.ledgers_.get(acct);
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

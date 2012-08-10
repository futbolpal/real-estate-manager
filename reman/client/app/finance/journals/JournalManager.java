package reman.client.app.finance.journals;

import java.sql.SQLException;
import java.util.Hashtable;

import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.finance.exceptions.NameAlreadyExistsException;
import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.ExceedMaxCommitException;
import reman.common.database.exceptions.LoggedInException;
import reman.common.database.exceptions.NotPersistedException;

/**
 * Controls the creation/access of Journal objects within the financial engine. Journals are uniquely identified by name.
 * @author Scott
 *
 */
public class JournalManager extends DatabaseObject {
	/**
	 * Journal indexed by their name
	 */
	private Hashtable<String, Journal> journals_;

	public JournalManager() {
		journals_ = new Hashtable<String, Journal>();
	}

	/**
	 * Remove the journal from being managed by this JournalManager. Finance engine must be in initialization phase.
	 * @param journal
	 * @return The journal that was removed. null if error.
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public Journal unRegisterJournal(Journal journal) throws DatabaseException, SQLException {
		if (FinanceManager.instance().isInitializationPhase()) {
			this.retrieve();

			if (!this.isJournalRegistered(journal))
				return null;

			try {
				if (this.lock()) {
					Journal removed = null;
					removed = this.journals_.remove(journal.getName());
					this.commit();

					return removed;
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
		return null;
	}

	/**
	 * Unregister the journal with <code>journal_name</code>. Finance engine must be in initialization phase.
	 * @param journal_name
	 * @return The journal that was removed. null otherwise.
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public Journal unRegisterJournal(String journal_name) throws DatabaseException, SQLException {
		return this.unRegisterJournal(this.getJournal(journal_name));
	}

	/**
	 * Registers the journal with this manager.  Journal names must be unique.
	 * @param journal
	 * @return
	 * @throws SQLException 
	 * @throws DatabaseException 
	 * @throws NameAlreadyExistsException
	 * @throws LoggedInException
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	public Journal registerJournal(Journal journal) throws NameAlreadyExistsException,
			LoggedInException, SQLException, DatabaseException {

		this.retrieve();//update journal map

		if (this.isJournalRegistered(journal))
			return journal;

		try {
			if (this.lock()) {
				if (this.journals_.containsKey(journal.getName())) {
					throw new NameAlreadyExistsException(this.journals_.get(journal.getName()));
				}

				/*must be able to commit the journal to the database to register the journal*/
				if (journal.lock()) {
					journal.commit();
					this.journals_.put(journal.getName(), journal);

					this.commit();
					return journal;
				}
			}
		} catch (NameAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
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
			journal.unlock();
			this.unlock();
		}
		return null;
	}

	/**
	 * Create a new journal, and registers it with this manager.
	 * @param journal_name
	 * @return The journal that was created.  Null if not successfully created.
	 * @throws NameAlreadyExistsException
	 * @throws DatabaseException 
	 * @throws SQLException 
	 * @throws LoggedInException 

	 */
	public Journal createJournal(String journal_name) throws NameAlreadyExistsException,
			LoggedInException, SQLException, DatabaseException {

		return this.registerJournal(new Journal(journal_name));
	}

	/**
	 * If the journal is known and managed by this journal manager
	 * @param j
	 * @return
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	public boolean isJournalRegistered(Journal j) throws DatabaseException, SQLException {
		if (j == null)
			return false;

		return this.getJournal(j.getName()) != null;
	}

	/**
	 * Get a map (keyed by name) of all journals managed within this JournalManager.
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public Hashtable<String, Journal> getJournals() throws DatabaseException, SQLException {
		this.retrieve();

		return new Hashtable<String, Journal>(this.journals_);
	}

	/**
	 * 
	 * @param journal_name
	 * @return The journal corresponding to <code>journal_name</code> if it exists or null if no matching name account found 
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	public Journal getJournal(String journal_name) throws DatabaseException, SQLException {

		this.retrieve();//update journal map

		return this.journals_.get(journal_name);
	}

	@Override
	public void retrieve() throws DatabaseException, SQLException {
		try {
			super.retrieve();
		} catch (NotPersistedException e) {
			/*because this manager will automate a retrieve, disregard a not in database exception*/
		}
	}

	/**
	 * Add journal entry <code>je</code> to <code>journal</code>.  Where <code>journal</code> must be a registered Journal in this JournalManager
	 * and <code>je</code> must be successfully validated.
	 * @param journal
	 * @param je
	 * @return null if Journal is not registered or other error occurs. Other wise the key of the journal entry in the journal.
	 * @throws SQLException 
	 * @throws DatabaseException 
	 * @throws FinanceException If the financial engine is in the initialization phase, Journals are not allowed to acquire JournalEntry objects.
	 */
	public Integer addJournalEntry(Journal journal, JournalEntry je) throws FinanceException,
			DatabaseException, SQLException {

		if (FinanceManager.instance().isInitializationPhase())
			throw new FinanceException(this,
					"The finance engine can not accumulate journals while in initialization phase.");

		this.retrieve();

		if (!this.isJournalRegistered(journal))
			return null;

		Integer result_key = null;
		try {
			if (journal.lock()) {
				result_key = journal.addJournalEntry(je);

				if (result_key != null) {
					journal.commit();
				}
				return result_key;
			}
		} catch (LoggedInException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (ExceedMaxCommitException e) {
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
			journal.unlock();
		}
		return null;
	}

	/**
	 * Add a <code>je</code> to the journal registered with this JournalManager identified by <code>journal_name</code>.
	 * @param journal_name
	 * @param je
	 * @return The key in the corresponding journal that this entry was added under. null if failed
	 * @throws SQLException 
	 * @throws DatabaseException 

	 */
	public Integer addJournalEntry(String journal_name, JournalEntry je) throws DatabaseException,
			SQLException {

		return this.addJournalEntry(this.getJournal(journal_name), je);
	}
}

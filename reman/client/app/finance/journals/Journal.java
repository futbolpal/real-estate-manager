package reman.client.app.finance.journals;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

import reman.client.app.finance.exceptions.FinanceException;
import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.ExceedMaxCommitException;
import reman.common.database.exceptions.LoggedInException;

/**
 * Journals provide storage for JournalEntry objects.  Each general JournalEntry must be validated, and AcctAmounts applied to respective
 * Account objects to be logged in a Journal.
 * <br/>Example: General Journal
 * @author Scott
 *
 */
public class Journal extends DatabaseObject {

	private Hashtable<Integer, JournalEntry> journal_entries_;

	/**
	 * For DatabaseObject use only
	 */
	private Journal() {
	}

	/**
	 * Journals are uniquely identified by their name.
	 * @param journalName
	 */
	public Journal(String journalName) {
		super.name_ = journalName;
		this.journal_entries_ = new Hashtable<Integer, JournalEntry>();
	}

	/**
	 * Parameter <code>je</code> will be validated and committed to the database. <code>je</code> must pass validation constraints in order to be successfully
	 * added to this Journal.
	 * 
	 * @param je
	 * @return The key of the journal entry. 'null' if unsuccessful
	 * @throws ExceedMaxCommitException  Journal entries are only allowed to be committed once, in accordance with accounting practices.
	 * @throws DatabaseException Attempt to apply invalid amount to an Account...
	 * @throws SQLException 
	 */
	public Integer addJournalEntry(JournalEntry je) throws ExceedMaxCommitException,
			DatabaseException, SQLException {
		if (!je.isValid()) {
			/*don't need to lock 'je' because journal entries can only be committed once successfully*/
			if (je.commit() < 0)
				return null;/*should never get here....exception should be thrown*/
		}

		Integer key = null;
		try {
			if (this.lock()) {
				if (this.journal_entries_.contains(je))
					throw new FinanceException(je, "This Journal already contains the Journal Entry '" + je
							+ "'.");

				key = this.getNextKey();
				if (this.journal_entries_.containsKey(key))
					throw new FinanceException(this, "This journal can not accept any more Journal Entries.");

				this.journal_entries_.put(key, je);

				/*keep journal entry map up to date*/
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

		return key;
	}

	/**
	 * Obtain the JournalEntry corresponding to <code>key</code>.
	 * @param key
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public JournalEntry getJournalEntry(Integer key) throws DatabaseException, SQLException {
		this.retrieve();
		return this.journal_entries_.get(key);
	}

	/**
	 * Obtain the corresponding key belonging to <code>je</code>
	 * @param je
	 * @return The corresponding key of <code>je</code> in this Journal or null if not found.
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public Integer getKey(JournalEntry je) throws DatabaseException, SQLException {
		this.retrieve();
		Integer matching_key = null;
		Enumeration<Integer> all_keys = this.journal_entries_.keys();
		while (all_keys.hasMoreElements()) {
			Integer curr_key = all_keys.nextElement();
			if (this.journal_entries_.get(curr_key).equals(je)) {
				matching_key = curr_key;
				break;
			}
		}
		return matching_key;
	}

	/**
	 * 
	 * @param je
	 * @return True if this journal contains <code>je</code>
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public boolean contains(JournalEntry je) throws DatabaseException, SQLException {
		this.retrieve();
		
		return this.journal_entries_.contains(je);
	}

	public int size() {
		return this.journal_entries_.size();
	}
	
	private Integer getNextKey() {
		return this.journal_entries_.size();
	}

	/**
	 * Journal names are unique, this is not allowed and must be registered through JournalManager.
	 */
	@Override
	public void setName(String name) {
		System.err.println("Not allowed to change name of this object.");
	}
}

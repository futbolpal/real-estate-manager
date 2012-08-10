package reman.client.app.finance.accounts;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

import org.xml.sax.SAXException;

import reman.client.app.finance.DatabaseObjectBatch;
import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.exceptions.InvalidAmountException;
import reman.client.app.finance.accounts.exceptions.InvalidCategoryException;
import reman.client.app.finance.accounts.exceptions.UnknownNodeException;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.finance.exceptions.NameAlreadyExistsException;
import reman.client.app.finance.journals.Journal;
import reman.client.app.finance.journals.JournalEntry;
import reman.client.app.finance.journals.JournalEntryLineItem;
import reman.client.app.finance.journals.exceptions.InvalidJournalEntryException;
import reman.client.app.trees.MapTreeTraverseUtility;
import reman.common.database.DatabaseObject;
import reman.common.database.ManagedDatabaseObject;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.LoggedInException;
import reman.common.database.exceptions.NotPersistedException;

/**
 * This class will manage all Account objects within the finance engine.  In order to use an account within the finance engine
 * it must be registered with this AccountManager.
 * <br/>This manager also provides functionality to guarantee unique Account names and unique Account ids. If an Account is not assigned an
 * id upon registration an id will automatically be assigned.
 * @author Scott
 *
 */
public class AccountManager extends ManagedDatabaseObject {

	/**
	 * These are the root level accounts in the account tree, tree relations (parent, children) are maintained within each Account
	 */
	private Hashtable<String, Account> root_accounts_;

	private AcctIdSystem id_system_;

	public AccountManager() {
		super(new String[] { "id_system_" });
		root_accounts_ = new Hashtable<String, Account>();
		id_system_ = new AcctIdSystem();
	}

	/**
	 * Obtain the first matching result (breadth first search) that has the key of acct_name
	 * @param acct_name
	 * @return
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	public Account getAccount(String acct_name) throws DatabaseException, SQLException {
		this.retrieve();//ensure account map up to date

		return (new MapTreeTraverseUtility<String, Account>()).getTargetNode(this.root_accounts_,
				acct_name);
	}

	public Collection<Account> getRootAccts(boolean retrieve) throws DatabaseException, SQLException {
		return this.getRootAcctsOfSubType(null, retrieve);
	}

	public Collection<Account> getRootAccts() throws DatabaseException, SQLException {
		return this.getRootAccts(true);
	}

	/**
	 * Get all root level Account objects where their types equal <code>type</code>.
	 * It is assumed that child AcctType can not differ from their parent account types
	 * 
	 * @param type The type of root level Accounts to obtain. If null all root level nodes will be obtained.
	 * @return Collection of Account objects that have <code>type</code>. Or all root accounts if <code>type</code> is null.
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	public Collection<Account> getRootAcctsOfSubType(AcctType type) throws DatabaseException,
			SQLException {
		return this.getRootAcctsOfSubType(type, true);
	}

	public Collection<Account> getRootAcctsOfSubType(AcctType type, boolean retrieve)
			throws DatabaseException, SQLException {
		if (retrieve)
			this.retrieve();//ensure account map up to date

		/*TODO: can child AcctType vary from parent AcctType? assumed no...*/
		ArrayList<Account> sub_types = new ArrayList<Account>();

		Enumeration<Account> all_root_accts = this.root_accounts_.elements();
		while (all_root_accts.hasMoreElements()) {
			Account curr_root_acct = all_root_accts.nextElement();
			if (type == null || curr_root_acct.getAcctType() == type)
				sub_types.add(curr_root_acct);
		}

		return sub_types;
	}

	/**
	 * A flat collection of all Account objects (in no particular order).
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public Collection<Account> getAllAccounts() throws DatabaseException, SQLException {
		this.retrieve();

		return (new MapTreeTraverseUtility<String, Account>()).getFlatTree(this.getRootAccounts());
	}

	/**
	 * Removes the Account with the corresponding name (<code>acct_name</code>) from the map of accounts.  Only works when in initialization state.
	 * @param acct_name
	 * @return True if account was successfully removed. False otherwise.
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	public boolean removeAccount(String acct_name) throws SQLException, DatabaseException {
		return this.removeAccount(this.getAccount(acct_name));
	}

	/**
	 * Unregister an account. Only works when in initialization state.
	 * @param acct
	 * @return True if account was successfully removed. False otherwise.
	 * @throws DatabaseException 
	 * @throws SQLException 
	 */
	public boolean removeAccount(Account acct) throws SQLException, DatabaseException {
		return this.replaceAccount(acct, null, false);
	}

	/**
	 * This will take <code>new_acct</code> and register it in the place of <code>remove_acct</code>. This means <code>new_acct</code>
	 * and <code>remove_acct</code> must share a parent and <code>new_acct</code> will inherit all of its children.  The
	 * AcctActionCategory objects can be attempted to be inherited if <code>copy_categories</code> is true.
	 * @param remove_acct This is the account to be removed and unregistered from this AccountManager.
	 * @param new_acct This is the new replacement account.  This account must not be registered with this AccountManager, and must
	 * 								share a common parent with <code>remove_acct</code>.  If this is passed as null, then <code>remove_acct</code>
	 * 								will simply be removed without a replacement.
	 * @return True if account was successfully replaced
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	public boolean replaceAccount(Account remove_acct, Account new_acct, boolean copy_categories)
			throws SQLException, DatabaseException {
		if (!FinanceManager.instance().isInitializationPhase())
			return false;

		this.retrieve();

		Account remove_parent = null;
		DatabaseObjectBatch<DatabaseObject> batch = new DatabaseObjectBatch<DatabaseObject>();
		try {
			if (this.lock()) {
				if (!this.isAccountRegistered(remove_acct, false)) {
					throw new FinanceException(remove_acct, "Account to be removed '" + remove_acct
							+ "' is not registered.");
				}
				/*new account must not be registered, must share a common parent, and must register successfully*/
				if (new_acct != null) {
					if (remove_acct.getParent() != new_acct.getParent()) {
						throw new FinanceException(new_acct, "New replacement account '" + new_acct
								+ "' and account to be removed '" + remove_acct
								+ "' must share a common parent account.");
					}
					if (this.isAccountRegistered(new_acct, false)) {
						throw new FinanceException(new_acct, "New replacement account '" + new_acct
								+ "' is already registered.");
					}
					batch.addToBatch(new_acct);
				}

				/*add account to be removed, and all children to the batch*/
				batch.addToBatch(remove_acct);
				batch.addToBatch(remove_acct.getChildren().values());

				/*categories will need to remove owner account*/
				Collection<AcctActionCategory> removed_acct_categories = remove_acct.getAllCategories();
				batch.addToBatch(removed_acct_categories);

				remove_parent = remove_acct.getParent();
				if (remove_parent != null) {
					batch.addToBatch(remove_parent);
				}

				batch.addToBatch(this);/*treat this object as LAST part of the batch*/

				/*only remove the account if all dependent accounts can be locked*/
				Collection<DatabaseObject> failed_dbos = batch.lockBatch();
				if (failed_dbos.size() == 0) {
					if (remove_parent == null) {
						this.root_accounts_.remove(remove_acct.getName());
					} else {
						remove_parent.removeChildAccount(remove_acct);
					}

					/*keep id collections up to date*/
					id_system_.unUseId(remove_acct.getAcctId());

					/*after old account has been removed, register the new account*/
					try {
						if (new_acct != null) {
							if (FinanceManager.instance().getAccountManager().registerAccount(new_acct, false)) {
								if (!new_acct.lock())/*register will unlock the new_acct, so re-lock it*/
									throw new FinanceException(new_acct, "Failed to lock account '" + new_acct + "'.");
							} else {
								throw new FinanceException(new_acct, "Failed to register '" + new_acct + "'.");
							}
						}
					} catch (Exception e) {
						this.retrieve();/*undo changes*/
						throw new FinanceException(new_acct, "Failed to register '" + new_acct + "'.");
					}

					/*patch the children of the removed account to point to the parent of the removed account
					 * or if there is a replacement account patch children through to removed the new_registered*/
					for (Account child : remove_acct.getChildren().values()) {
						if (new_acct == null) {
							child.setParent(remove_parent);
						} else {
							child.setParent(new_acct);
							new_acct.addChildAccount(child);
						}
					}

					int categories_added = 0;
					/*removed_acct needs to be taken out of each category it previously was under*/
					for (AcctActionCategory cat : removed_acct_categories) {
						if (copy_categories && new_acct != null) {
							/*add the categories from earliest dependent (first to be owned by this acct) to latest (category)
							 * to preserve category tree structure*/
							Stack<AcctActionCategory> cat_dependent_chain = new Stack<AcctActionCategory>();
							AcctActionCategory curr_cat = cat;
							do {
								curr_cat.removeOwnerAcct(remove_acct.getName());
								cat_dependent_chain.add(curr_cat);
								curr_cat = curr_cat.getParent();
							} while (curr_cat != null && curr_cat.getOwnerAccts().contains(remove_acct.getName()));

							while (cat_dependent_chain.size() > 0) {
								curr_cat = cat_dependent_chain.pop();
								try {
									if (new_acct.addActionCategory(curr_cat))
										categories_added++;
								} catch (NameAlreadyExistsException e) {
								}
							}
						} else {
							cat.removeOwnerAcct(remove_acct.getName());
						}
					}

					Collection<DatabaseObject> failed_commit = batch.commitBatch();
					if (failed_commit.size() == 0) {
						FinanceManager.instance().getLedgerManager().removeLedger(remove_acct);

						return true;
					} else {
						String failed_acct_names = DatabaseObjectBatch.getDatabaseObjectNames(failed_commit);
						throw new FinanceException(this,
								"Failed to commit the following dependent (accounts or categories): "
										+ failed_acct_names + ".");
					}
				} else {
					String failed_acct_names = DatabaseObjectBatch.getDatabaseObjectNames(failed_dbos);
					throw new FinanceException(this,
							"Failed to lock the following dependent (accounts or categories): "
									+ failed_acct_names + ".");
				}
			}
		} catch (LoggedInException e) {
			// TODO Auto-generated catch block
			batch.retrieveBatch();
			e.printStackTrace();
			throw e;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			batch.retrieveBatch();
			e.printStackTrace();
			throw e;
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			batch.retrieveBatch();
			e.printStackTrace();
			throw e;
		} finally {
			batch.unlockBatch();
		}

		return false;
	}

	private Hashtable<String, Account> getRootAccounts() throws DatabaseException, SQLException {
		this.retrieve();

		return new Hashtable<String, Account>(this.root_accounts_);
	}

	/**
	 * This function will register an account with this AccountManager.  Accounts must have unique names.
	 * @param acct
	 * @param retrieve_commit_unlock used by replace function.  If true this AccountManager will retrieve,commit, and unlock itself.
	 * @return True if <code>acct</code> was (or already is) registered as a result of this call. False otherwise.
	 * @throws NameAlreadyExistsException
	 * @throws DatabaseException
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 */
	private boolean registerAccount(Account acct, boolean retrieve_commit_unlock)
			throws NameAlreadyExistsException, DatabaseException, SQLException, IllegalArgumentException {
		if (retrieve_commit_unlock)
			this.retrieve();

		DatabaseObjectBatch<DatabaseObject> batch = new DatabaseObjectBatch<DatabaseObject>();
		Account parent = acct.getParent();
		try {
			if (this.lock()) {
				if (this.isAccountRegistered(acct, false))
					return true;

				if (acct.getAcctId() != null && id_system_.isAcctIdInUse(acct.getAcctId())) {
					throw new FinanceException(acct, "Account id '" + acct.getAcctId()
							+ "' is already in use.");
				}
				this.validateNewAccountInfo(acct, acct.getParent(), false);

				if (!FinanceManager.instance().isInitializationPhase())
					acct.getBalanceSystem().zeroAmount();

				batch.addToBatch(acct);
				if (parent != null)
					batch.addToBatch(parent);

				/*only register account if parent can also be locked*/
				Collection<DatabaseObject> failed_dbos = batch.lockBatch();
				if (failed_dbos.size() == 0) {
					if (parent == null) {
						this.root_accounts_.put(acct.getName(), acct);
					} else {
						/*also maintain the parent account, and synchronize with database*/
						parent.addChildAccount(acct);
					}

					/*account id must not be null, and must be greater than 0*/
					if (acct.getAcctId() == null || acct.getAcctId() < 0)
						acct.setId(id_system_.getNextAcctId(acct.getAcctType()));

					/*keep track of all ids currently in use*/
					id_system_.useId(acct.getAcctId());

					batch.commitBatch();
					if (retrieve_commit_unlock)
						this.commit();

					FinanceManager.instance().getLedgerManager().createLedger(acct);

					return true;
				} else {
					String failed_acct_names = DatabaseObjectBatch.getDatabaseObjectNames(failed_dbos);
					throw new FinanceException(this, "Failed to lock the following dependent accounts: "
							+ failed_acct_names + ".");
				}
			}
		} catch (IllegalArgumentException e) {
			batch.retrieveBatch();
			this.retrieve();
			throw e;
		} catch (NameAlreadyExistsException e) {
			batch.retrieveBatch();
			this.retrieve();
			throw e;
		} catch (DatabaseException e) {
			batch.retrieveBatch();
			this.retrieve();
			throw e;
		} catch (SQLException e) {
			batch.retrieveBatch();
			this.retrieve();
			throw e;
		} finally {
			batch.unlockBatch();
			if (retrieve_commit_unlock)
				this.unlock();
		}
		return false;
	}

	/**
	 * Registers <code>acct</code> in the managed Account collection maintained by this AccountManager.  Account names must be unique, and
	 * Account balances will only be incorporated if the finance engine is in the initialization phase.
	 * @return True if <code>acct</code> was (or already is) registered as a result of this call. False otherwise.
	 * @throws SQLException 
	 * @throws DatabaseException 
	 * @throws IllegalArgumentException 
	 * @throws NameAlreadyExistsException 
	 */
	public boolean registerAccount(Account acct) throws NameAlreadyExistsException,
			IllegalArgumentException, DatabaseException, SQLException {
		return this.registerAccount(acct, true);
	}

	/**
	 * Used to create an account within the current project. 'balance' will only be considered if the
	 * program is in the initialization state with respect to finances. If the parent account is null then it is considered
	 * a root account, and added to the root account map
	 * 
	 * @param acct_id Can be passed as 'null' if auto generation is desired.
	 * @param name Name of the account. Key in hashtable of accounts (Must be unique with respect to other accounts).
	 * @param balance Starting balance of the account.  Only relevant if program in initialization of finances state. Can be passed as 'null'.
	 * @param parent_acct Parent type (if applicable) of this account.  Can be passed as 'null'.
	 * @param a_type This account type.  If a 'parent_acct' is passed this much match the 'parent_acct' type
	 * @param normal_balance What type of transaction increases this account.
	 * @param t_scale
	 * @return The account that was created and maintained within this AccountManager, null if failure to create.
	 * @throws SQLException 
	 * @throws FinanceException 
	 * @throws UnknownNodeException
	 * @throws DatabaseException 
	 */
	public Account createAccount(Integer acct_id, String name, Double balance, Account parent_acct,
			AcctType a_type, TransactionType normal_balance, AcctTimeScale t_scale,
			CashCategory cash_category) throws DatabaseException, SQLException {

		this.retrieve();

		Account acct = null;
		if (acct_id == null)
			acct_id = id_system_.getNextAcctId(a_type);
		if (FinanceManager.instance().isInitializationPhase() && balance != null)
			acct = new Account(acct_id, name, parent_acct, a_type, balance, normal_balance, t_scale,
					cash_category);
		else
			acct = new Account(acct_id, name, parent_acct, a_type, normal_balance, t_scale, cash_category);

		if (this.registerAccount(acct))
			return acct;
		return null;
	}

	/**
	 * Used to create/register a temporary account. <code>balance</code> will only be considered if the
	 * program is in the initialization state with respect to finances.
	 * @param acct_id Can be passed as 'null' if auto generation is desired.
	 * @param name
	 * @param balance
	 * @param parent_acct
	 * @param close_to_acct
	 * @param a_type
	 * @param normal_balance
	 * @param close_to_category For automated temporary account closings. Must be valid with respect to close_to_accout at time of account closing (statement generation).
	 * 								The category to associate the close_to_acct_ journal entry line item receiving this accounts balance
	 * @param close_from_category For automated temporary account closings. Must be valid with respect to this account's categories at time of closing (statement generation).
	 * 								The category associated with this accounts closing journal entry line item
	 * @return The account that was created and maintained within this AccountManager, null if failure to create. 
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	public TemporaryAccount createTempAccount(Integer acct_id, String name, Double balance,
			Account parent_acct, Account close_to_acct, AcctType a_type, TransactionType normal_balance,
			Journal close_to_journal, AcctActionCategory close_from_category,
			AcctActionCategory close_to_category, CashCategory cash_category)
			throws NameAlreadyExistsException, FinanceException, UnknownNodeException, DatabaseException,
			SQLException {

		TemporaryAccount temp_acct = null;
		if (acct_id == null)
			acct_id = id_system_.getNextAcctId(a_type);

		if (!this.isAccountRegistered(close_to_acct)) {
			throw new IllegalArgumentException("close_to_acct must be maintained by this AccountManager.");
		}

		if (FinanceManager.instance().isInitializationPhase() && balance != null)
			temp_acct = new TemporaryAccount(acct_id, name, parent_acct, balance, close_to_acct, a_type,
					normal_balance, close_to_journal, close_from_category, close_to_category, cash_category);
		else
			temp_acct = new TemporaryAccount(acct_id, name, parent_acct, close_to_acct, a_type,
					normal_balance, close_to_journal, close_from_category, close_to_category, cash_category);

		if (this.registerAccount(temp_acct))
			return temp_acct;
		return null;
	}

	public boolean isAccountRegistered(Account acct) throws DatabaseException, SQLException {
		return this.isAccountRegistered(acct, true);
	}

	/**
	 * If <code>acct</code> is known about by this manager
	 * @param acct
	 * @return True if 'acct' is known about by this manager
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	boolean isAccountRegistered(Account acct, boolean retrieve) throws DatabaseException,
			SQLException {
		if (retrieve)
			this.retrieve();
		Account managed_acct = (new MapTreeTraverseUtility<String, Account>()).getTargetNode(
				this.root_accounts_, acct);
		return managed_acct != null;
	}

	/**
	 * Checks duplicate name, and that parent account exists, account type doesn't differ from parent account, account time
	 * scale does not differ from parent
	 * @param name
	 * @param parent_acct
	 * @param a_type
	 * @throws IllegalArgumentException
	 * @throws NameAlreadyExistsException
	 * @throws FinanceException
	 * @throws UnknownNodeException
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	private void validateNewAccountInfo(Account subject_acct, Account parent_acct, boolean retreive)
			throws IllegalArgumentException, NameAlreadyExistsException, FinanceException,
			UnknownNodeException, DatabaseException, SQLException {
		if (subject_acct.getName() == null || subject_acct.getName().isEmpty())
			throw new IllegalArgumentException("Name must not be null or empty.");

		if (retreive)
			this.retrieve();

		/*check for duplicate name, and that parent already exists in the list*/
		Account maintained_duplicate_acct = (new MapTreeTraverseUtility<String, Account>())
				.getTargetNode(this.root_accounts_, subject_acct.getName());

		if (maintained_duplicate_acct != null)
			throw new NameAlreadyExistsException(maintained_duplicate_acct);

		/*TODO: verify that sub accounts always have the same account type as parent type*/
		/*TODO: verify that sub accounts always have same time frame as parent accounts*/
		if (parent_acct != null) {
			if (!this.isAccountRegistered(parent_acct))
				throw new UnknownNodeException(parent_acct, "Parent account '" + parent_acct.getName()
						+ "' must be maintained by this AccountManager.");
			if (parent_acct.getAcctType() != subject_acct.getAcctType())
				throw new FinanceException(parent_acct, "Parent account type '" + parent_acct.getAcctType()
						+ "' must be equal to '" + subject_acct.getAcctType() + "'.");
			if (parent_acct.getTimeScale() != subject_acct.getTimeScale())
				throw new FinanceException(parent_acct, "Parent account time scale '"
						+ parent_acct.getTimeScale() + "' must be equal to '" + subject_acct.getTimeScale()
						+ "'.");
			if (subject_acct == parent_acct)
				throw new FinanceException(parent_acct,
						"Parent account must not be the same as subject account.");
		}
	}

	/**
	 * <code>temp_acct</code> must have its temporary account members set (Close To Account, Close From Category, Close To Category,
	 * Close To Journal) in order to automatically close a temporary account.
	 * @param temp_acct
	 * @param close_from_category If the close_from account requires a category, a valid one must be provided
	 * @param close_to_category If the close_to account requires a category, a valid one must be provided
	 * @return The Integer key of the corresponding journal entry in the Close To Journal.
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	public Integer closeAccount(TemporaryAccount temp_acct) throws InvalidJournalEntryException,
			InvalidAmountException, InvalidCategoryException, UnknownNodeException, DatabaseException,
			SQLException {
		return this.closeAccount(temp_acct, temp_acct.getCloseToAccount(), temp_acct
				.getCloseFromCategory(), temp_acct.getCloseToCategory(), temp_acct.getCloseToJournal());
	}

	/**
	 * Create a journal entry which transfers the balance of <code>close_from</code> Account into <code>close_to</code> Account.
	 * Both Account objects must be registered within this AccountManager.
	 * 
	 * @param close_from
	 * @param close_to
	 * @param record_result_to The journal where the resulting journal entry will be recorded to
	 * @param close_from_category If the close_from account requires a category, a valid one must be provided
	 * @param close_to_category If the close_to account requires a category, a valid one must be provided
	 * @return True key of entry in 'record_result_to' journal if successful, null otherwise
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	public Integer closeAccount(Account close_from, Account close_to,
			AcctActionCategory close_from_category, AcctActionCategory close_to_category,
			Journal record_result_to) throws InvalidJournalEntryException, InvalidAmountException,
			InvalidCategoryException, UnknownNodeException, DatabaseException, SQLException {

		this.retrieve();//ensure account map up to date

		Integer entry_key = null;

		if (!this.isAccountRegistered(close_from)) {
			throw new UnknownNodeException(close_from, "The Account '" + close_from.getName()
					+ "' is not known by the AccountManager.");
		}
		if (!this.isAccountRegistered(close_to)) {
			throw new UnknownNodeException(close_to, "The Account '" + close_to.getName()
					+ "' is not known by the AccountManager.");
		}
		if (record_result_to == null) {
			throw new IllegalArgumentException("record_result_to can not be null.");
		}

		/*TODO: does an account need to be a 'TEMPORARY' account to close?*/
		/*close_from_amount will zero the close_from account balance*/
		JournalEntry je = new JournalEntry("Closing account '" + close_from.getName()
				+ "' into account '" + close_to.getName() + "'.", null);
		je.addLineItem(new JournalEntryLineItem(close_to, new AcctAmount(close_from.getBalanceSystem()
				.getAcctAmount().getAmount(), close_from.getBalanceSystem().getAcctAmount()
				.getTransactionType()), "Result of closing account '" + close_from.getName() + "'.",
				close_to_category));
		je.addLineItem(new JournalEntryLineItem(close_from, new AcctAmount(close_from
				.getBalanceSystem().getAcctAmount().getAmount(), TransactionType.getOpposite(close_from
				.getBalanceSystem().getAcctAmount().getTransactionType())), "Closing account '"
				+ close_from.getName() + ".", close_from_category));

		/*use current journal manager to record result*/
		entry_key = FinanceManager.instance().getJournalManager().addJournalEntry(record_result_to, je);

		return entry_key;
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
	 * Export all managed Account objects (and corresponding AcctActionCategory objects) to XML format.
	 * @param file_name
	 * @throws IOException
	 * @throws SAXException
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public void exportAccts(String file_name) throws IOException, SAXException, DatabaseException,
			SQLException {
		AccountImportExport aie = new AccountImportExport(this.getRootAccounts());
		aie.exportAccts(file_name);
	}

	/**
	 * Import all managed Account objects (and corresponding AcctActionCategory objects) from XML format.
	 * @param file_name
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public int importAccts(String file_name) throws SAXException, IOException, DatabaseException,
			SQLException {
		AccountImportExport aie = new AccountImportExport(this.getRootAccounts());
		return aie.importAccts(file_name);
	}
}

package reman.client.app.finance.accounts;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Hashtable;

import reman.client.app.finance.DatabaseObjectBatch;
import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.TransactionType;
import reman.client.app.finance.equations.IVariableAssignable;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.finance.exceptions.NameAlreadyExistsException;
import reman.client.app.finance.ledger.Ledger;
import reman.client.app.trees.IMapTreeNode;
import reman.client.app.trees.MapTreeTraverseUtility;
import reman.common.database.DatabaseObject;
import reman.common.database.ManagedDatabaseObject;
import reman.common.database.exceptions.DatabaseException;

/**
 * A financial account, which is the basis for logically grouping financial interactions a business encounters.
 * An Account primarily maintains a balance which has a normal balance to give sign perspective.
 * <br/>An Account also maintains other organizational and relational attributes such as account type, applicable time scale, parent Account, children Accounts.
 * <br/>An Account has the option to contain AcctActionCategory objects which if present must be used when ever the Account object appears in a JournalEntry.
 * @author Scott
 *
 */
public class Account extends ManagedDatabaseObject implements IMapTreeNode<String, Account>,
		IVariableAssignable {

	private CashCategory cash_category_;

	private Integer acct_id_;

	private AcctType acct_type_;

	private AcctBalanceSystem balance_system_;

	/**
	 * This account can be categorized as a sub account of the parent_acct_
	 */
	private Account parent_acct_;

	/**
	 * Maintained by this class:
	 * If this class has a parent, this account is added to the child_accts_ map of that parent
	 */
	private Hashtable<String, Account> child_accts_;

	/**
	 * This refers to this account applicability with respect to time (Long/Short term)
	 * Top level accounts (in account hierarchy) will decide this value
	 */
	private AcctTimeScale time_scale_;

	/**
	 * When a journal line item entry is made, dependents of these elements, and these elements must be chosen
	 */
	private Hashtable<String, AcctActionCategory> root_action_categories_;

	/**
	 * DatabaseObject use only.
	 */
	protected Account() {
		super(new String[] { "balance_system_" });
	}

	/**
	 * Used for account creation during "normal" (non-initialization) phase of operation.
	 * @param name of this Account
	 * @param parent_acct This is the parent account of this account.  This will maintain the parent account child account list, so before this point it must be guaranteed that the parent account is valid in the AccountManager.
	 * @param acct_type Type of this Account
	 * @param normal_balance
	 * @param t_scale Time scale of this Account.
	 */
	public Account(Integer acct_id, String name, Account parent_acct, AcctType acct_type,
			TransactionType normal_balance, AcctTimeScale t_scale, CashCategory cash_category) {

		this(acct_id, name, parent_acct, acct_type, 0, normal_balance, t_scale, cash_category);
	}

	/**
	 * Only to be used during the initialization state of the accounts
	 * @param name of this Account
	 * @param parent_acct This is the parent account of this account.  This will maintain the parent account child account list, so before this point it must be guaranteed that the parent account is valid in the AccountManager.
	 * @param acct_type Type of this Account
	 * @param balance Initial balance of this Account
	 * @param normal_balance
	 * @param t_scale Time scale of this Account.
	 */
	public Account(Integer acct_id, String name, Account parent_acct, AcctType acct_type,
			double balance, TransactionType normal_balance, AcctTimeScale t_scale,
			CashCategory cash_category) {
		this();
		super.name_ = name;
		this.acct_id_ = acct_id;
		this.child_accts_ = new Hashtable<String, Account>();
		this.root_action_categories_ = new Hashtable<String, AcctActionCategory>();
		this.balance_system_ = new AcctBalanceSystem(normal_balance, new AcctAmount(balance,
				normal_balance));
		this.parent_acct_ = parent_acct;

		this.initAcctType(acct_type);
		this.initTimeScale(t_scale);
		this.initCashCategory(cash_category);
	}

	/**
	 * Obtain the Ledger corresponding to this Account from the LedgerManager.
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public Ledger getLedger() throws DatabaseException, SQLException {
		return FinanceManager.instance().getLedgerManager().getLedger(this);
	}

	public Integer getAcctId() {
		return this.acct_id_;
	}

	public AcctBalanceSystem getBalanceSystem() {
		return this.balance_system_;
	}

	public AcctType getAcctType() {
		return this.acct_type_;
	}

	public Account getParent() {
		return this.parent_acct_;
	}

	public AcctTimeScale getTimeScale() {
		return this.time_scale_;
	}

	/**
	 * This map is maintained by the AccountManager.
	 * @return All the children (keyed by account name) of this account.
	 */
	public Hashtable<String, Account> getChildren() {
		return new Hashtable<String, Account>(this.child_accts_);
	}

	/**
	 * This should only be used by the AccountManager.
	 * @param acct Must have parent==this
	 * @return True if 'acct' was added to this child list.
	 */
	boolean addChildAccount(Account acct) {
		if (acct.getParent() == this) {
			this.child_accts_.put(acct.getName(), acct);
			return true;
		}
		return false;
	}

	/**
	 * This should only be used by the AccountManager.
	 * @param acct
	 * @return True if <code>acct</code> was removed.
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	boolean removeChildAccount(Account acct) throws DatabaseException, SQLException {
		if (!FinanceManager.instance().isInitializationPhase())
			return false;
		return (this.child_accts_.remove(acct.getName()) != null);
	}

	public boolean isLeafNode() {
		return this.child_accts_.size() == 0;
	}

	/**
	 * Obtain a flat collection representation of all associated AcctActionCategory objects.
	 * @return
	 */
	public Collection<AcctActionCategory> getAllCategories() {
		return (new MapTreeTraverseUtility<String, AcctActionCategory>()).getFlatTree(this
				.getRootActionCategories());
	}

	/**
	 * Obtain the root level AcctActionCategory objects, keyed by name.
	 * @return Top (root) level action categories.
	 */
	public Hashtable<String, AcctActionCategory> getRootActionCategories() {
		return new Hashtable<String, AcctActionCategory>(this.root_action_categories_);
	}

	/**
	 * Obtain the first matching result (breadth first search) that == <code>target_category</code>
	 * @param target_category
	 * @return
	 */
	public AcctActionCategory getActionCategory(AcctActionCategory target_category) {
		return (new MapTreeTraverseUtility<String, AcctActionCategory>().getTargetNode(
				this.root_action_categories_, target_category));
	}

	/**
	 * Obtain the first matching result (breadth first search) that has the key of <code>category_name</code>
	 * @param category_name
	 * @return
	 */
	public AcctActionCategory getActionCategory(String category_name) {
		return (new MapTreeTraverseUtility<String, AcctActionCategory>().getTargetNode(
				this.root_action_categories_, category_name));
	}

	/**
	 * Add an AcctActionCategory to the tree of categories. Placement in tree will be based on parent node.
	 * This method needs to lock/commit the category to be successful (to add this account as owner account).
	 * AcctActionCategory objects must have unique names with respect to other AcctActionCategory objects already
	 * in this accounts category tree.
	 * @param category
	 * @return True if <code>category</code> was incorporated into the tree
	 * @throws NameAlreadyExistsException 
	 */
	public boolean addActionCategory(AcctActionCategory category) throws NameAlreadyExistsException {
		/*no duplicate names allowed per account*/
		AcctActionCategory existing_named_cat = (new MapTreeTraverseUtility<String, AcctActionCategory>())
				.getTargetNode(this.root_action_categories_, category.getName());
		if (existing_named_cat != null) {
			throw new NameAlreadyExistsException(existing_named_cat);
		}

		DatabaseObjectBatch batch = new DatabaseObjectBatch();
		AcctActionCategory parent = category.getParent();

		boolean category_added = false;
		batch.addToBatch(category);
		Collection<DatabaseObject> failed_lock = batch.lockBatch();
		if (failed_lock.size() == 0) {
			if (parent == null) {
				category_added = this.root_action_categories_.put(category.getName(), category) == null;
			} else {
				/*maintain the tree structure*/
				if (this.isValidCategory(parent)) {
					category_added = parent.addChildCategory(category);
				} else {
					/*because accounts can take on categories from other accounts
					 *if the parent is not contained within this account, make it a root account*/
					category_added = this.root_action_categories_.put(category.getName(), category) == null;
				}
			}
		}

		if (category_added) {
			category.addOwnerAcct(this.getName());
			batch.commitBatch();
		}

		batch.unlockBatch();

		return category_added;
	}

	/**
	 * Remove <code>category</code> from this Account objects AcctActionCategory tree.
	 * <br/>This will lock,commit,unlock the category to remove this account as owner from category.
	 * @param category
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public boolean removeActionCategory(AcctActionCategory category) throws DatabaseException,
			SQLException {
		return this.replaceActionCategory(category, null);
	}

	/**
	 * Only applicable during the initialization phase of the finance engine.  Remove <code>replace</code> AcctActionCategory
	 * from the current category tree, and place the <code>new_cat<code> AcctActionCategory in its stead.  <code>new_cat</code> will inherit
	 * all children from <code>replace</code>. <code>replace</code>
	 * and <code>new_cat</code> must have a common parent.
	 * @param replace The category to remove from this Account's category tree.
	 * @param new_cat The new category (must not already be included in this Account's category tree). If this is passed as null then
	 * 							<code>replace</code> will simply be removed from this Account's category tree.
	 * @return
	 * @throws DatabaseException Thrown if <code>replace</code> is not in this account's category tree, if no common parent between parameters
	 * 			if <code>new_cat</code> has a name conflict with an existing node in this account's category tree.
	 * @throws SQLException
	 */
	public boolean replaceActionCategory(AcctActionCategory replace, AcctActionCategory new_cat)
			throws DatabaseException, SQLException {
		if (!FinanceManager.instance().isInitializationPhase())
			return false;

		DatabaseObjectBatch batch = new DatabaseObjectBatch();
		try {
			if (!this.isValidCategory(replace))
				throw new FinanceException(replace, "Category to be replaced '" + replace
						+ "' is not a valid category for account '" + this + "'.");
			if (new_cat != null) {
				if (this.isValidCategory(new_cat))
					throw new FinanceException(new_cat, "Replacement category '" + new_cat
							+ "' must not already be a category of account '" + this + "'.");

				if (replace.getParent() != new_cat.getParent())
					throw new FinanceException(new_cat, "Replacement category '" + new_cat
							+ "' must share common parent with category '" + replace + "'.");

				batch.addToBatch(new_cat);
			}
			Collection<AcctActionCategory> children = replace.getChildren().values();
			AcctActionCategory parent = replace.getParent();

			batch.addToBatch(replace);
			batch.addToBatch(children);
			if (parent != null)
				batch.addToBatch(parent);

			Collection<DatabaseObject> failed_lock = batch.lockBatch();
			if (failed_lock.size() == 0) {
				//remove replace cat
				if (parent == null) {
					if (this.root_action_categories_.remove(replace.getName()) == null) {
						throw new FinanceException(replace, "Failed removing category '" + replace + "'.");
					}
				} else {
					if (this.isValidCategory(parent)) {
						if (!parent.removeChildCategory(replace)) {
							throw new FinanceException(replace, "Failed removing category '" + replace + "'.");
						}
					} else {
						/*this category could be borrowed from another account, so only remove from this account's category tree*/
						if (this.root_action_categories_.remove(replace.getName()) == null) {
							throw new FinanceException(replace, "Failed removing category '" + replace + "'.");
						}
					}
				}

				replace.removeOwnerAcct(this.getName());

				if (new_cat != null) {
					if (!this.addActionCategory(new_cat)) {
						throw new FinanceException(new_cat, "Failed to add replacement category '" + new_cat
								+ "'.");
					}
					if (!new_cat.lock()) {/*adding category will unlock the category, re-obtain the lock*/
						throw new FinanceException(new_cat, "Failed to lock replacement category '" + new_cat
								+ "'.");
					}
				}

				//patch children through to existing parent, or replacement cat
				for (AcctActionCategory child : children) {
					if (new_cat == null) {
						child.setParent(parent);
					} else {
						child.setParent(new_cat);
						new_cat.addChildCategory(child);
					}
				}

				Collection<DatabaseObject> failed_commit = batch.commitBatch();
				if (failed_commit.size() != 0) {
					String failed_acct_names = DatabaseObjectBatch.getDatabaseObjectNames(failed_commit);
					throw new FinanceException(this, "Failed to commit the following dependent categories: "
							+ failed_acct_names + ".");
				}

				return true;

			} else {
				String failed_acct_names = DatabaseObjectBatch.getDatabaseObjectNames(failed_lock);
				throw new FinanceException(this, "Failed to lock the following dependent categories: "
						+ failed_acct_names + ".");
			}
		} catch (DatabaseException e) {
			batch.retrieveBatch();
			throw e;
		} catch (SQLException e) {
			batch.retrieveBatch();
			throw e;
		} finally {
			batch.unlockBatch();
		}

	}

	/**
	 * If <code>category</code> is a AcctActionCategory of this Account.
	 * @param category
	 * @return True if <code>category</code> is a category within Account
	 */
	public boolean isValidCategory(AcctActionCategory category) {

		AcctActionCategory found_category = (new MapTreeTraverseUtility<String, AcctActionCategory>())
				.getTargetNode(this.root_action_categories_, category);

		return found_category != null;
	}

	private void initCashCategory(CashCategory cash_category) {
		if (this.parent_acct_ == null)
			this.cash_category_ = cash_category;
		else
			this.cash_category_ = this.parent_acct_.getCashCategory();
	}

	public CashCategory getCashCategory() {
		return this.cash_category_;
	}

	/**
	 * Parameter <code>t_scale</code> will not be allowed to differ from parent account time scale
	 * @param t_scale
	 * @return True if <code>t_scale</code> parameter was applied.
	 */
	private boolean initTimeScale(AcctTimeScale t_scale) {
		if (this.parent_acct_ == null) {
			this.time_scale_ = t_scale;
			return true;
		} else {
			this.time_scale_ = this.parent_acct_.getTimeScale();
			return (this.time_scale_ == t_scale);
		}
	}

	/**
	 * Set the account type of this Account. The account type is not allowed to differ from that of the parent account type.	 
	 * 
	 * @param acct_type
	 * @return True if this account type was applied.
	 */
	private boolean initAcctType(AcctType acct_type) {
		if (this.parent_acct_ == null) {
			this.acct_type_ = acct_type;
			return true;
		} else {
			this.acct_type_ = this.parent_acct_.getAcctType();
			return (this.acct_type_ == acct_type);
		}
	}

	/**
	 * If this Account is of type ASSET and normal balance of CREDIT.
	 * @return
	 */
	public boolean isContraAsset() {
		if (AcctType.getGenericType(this.acct_type_) == AcctType.ASSET
				&& this.balance_system_.getNormalBalance() == TransactionType.CREDIT)
			return true;
		return false;
	}

	/**
	 * If this Account is of type EQUITY and normal balance of DEBIT.
	 * @return
	 */
	public boolean isContraEquity() {
		if (AcctType.getGenericType(this.acct_type_) == AcctType.EQUITY
				&& this.balance_system_.getNormalBalance() == TransactionType.DEBIT)
			return true;
		return false;
	}

	/**
	 * If this Account is of type LIABILITY and normal balance of DEBIT.
	 * @return
	 */
	public boolean isContraLiability() {
		if (AcctType.getGenericType(this.acct_type_) == AcctType.LIABILITY
				&& this.balance_system_.getNormalBalance() == TransactionType.DEBIT)
			return true;
		return false;
	}

	@Override
	public Double getValue() {
		return this.balance_system_.getBalance();
	}

	/**
	 * Based on accounts having unique names
	 */
	public boolean equals(Object o) {
		if (o instanceof Account) {
			Account acct = (Account) o;
			if (this.name_ == acct.getName())
				return true;
		}
		return false;
	}

	@Override
	public void setName(String name) {
		//do nothing, not allowed to change name of account
		System.err.println("Not allowed to change name of this object.");
	}

	public String toString() {
		String value = "";

		if (this.acct_id_ != null)
			value = Integer.toString(this.acct_id_) + " ";

		value += this.name_;

		return value;
	}

	/**
	 * Used by the AccountManager to update this Account object's parent.
	 * @param parent
	 */
	void setParent(Account parent) {
		this.parent_acct_ = parent;
	}

	/*ADDED FOR GUI PURPOSES*/

	/**
	 * This object is allowed to have members set if it is not already registered with finance manager or if the
	 * finance manager is in the initialization phase.
	 * @return
	 * @throws SQLException 
	 * @throws DatabaseException 
	 */
	private boolean isSetable() {
		try {
			if (!FinanceManager.instance().getAccountManager().isAccountRegistered(this,false)
					|| FinanceManager.instance().isInitializationPhase())
				return true;
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/*public void setName(String name) {
		if (this.isSetable()) {
			this.name_ = name;
		}
	}

	public void setAcctId(int id) {
		if (this.isSetable()) {
			this.acct_id_ = id;
		}
	}

	public void setAcctType(AcctType a_type) {
		if (this.isSetable()) {
			this.initAcctType(a_type);
		}
	}

	public void setTimeScale(AcctTimeScale t_scale) {
		if (this.isSetable()) {
			this.initTimeScale(t_scale);
		}
	}*/

	public void setBalance(double balance, TransactionType normal_balance) {
		if (this.isSetable()) {
			this.balance_system_.setNormalBalance(normal_balance);
			this.balance_system_.setBalance(balance);
		}
	}

	void setId(Integer id) {
		if (this.isSetable()) {
			this.acct_id_ = id;
		}
	}

	public void retrieve() throws SQLException, DatabaseException {
		if (this.getName() == "Cash") {
			System.out.println(this.balance_system_);
		}
		super.retrieve();
	}
	/*ADDED FOR GUI PURPOSES*/
}

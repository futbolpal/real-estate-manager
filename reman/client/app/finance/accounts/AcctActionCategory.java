package reman.client.app.finance.accounts;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;

import reman.client.app.finance.DatabaseObjectBatch;
import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.TransactionType;
import reman.client.app.finance.equations.IVariableAssignable;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.trees.IMapTreeNode;
import reman.common.database.DatabaseObject;
import reman.common.database.ManagedDatabaseObject;
import reman.common.database.exceptions.DatabaseException;

/**
 * Used to categorize account actions.  These categories can form hierarchies so the categorizations can be viewed from different levels.
 * Usage ex: When the "Cash" account appears in a journal entry, the user must select what category the action pertains to because this
 * information is needed to generate the Cash Flow statement.
 * AcctActionCategory objects (as long as no name conflicts exists) can also be shared between multiple accounts, if similar categorization
 * needs arise.
 * @author Scott
 *
 */
public class AcctActionCategory extends ManagedDatabaseObject implements
		IMapTreeNode<String, AcctActionCategory>, IVariableAssignable {

	/**
	 * The immediate owners of this category. Child categories inherit parent category owners.
	 */
	private HashSet<String> owner_acct_names_;

	private AcctBalanceSystem balance_system_;

	private AcctActionCategory parent_;

	private Hashtable<String, AcctActionCategory> child_categories_;

	@Override
	public boolean equals(Object o) {
		if (o instanceof AcctActionCategory) {
			AcctActionCategory cat = (AcctActionCategory) o;
			if (cat.getName().equals(this.name_) && cat.getParent() == this.parent_
					&& cat.getOwnerAccts().equals(this.owner_acct_names_)
					&& cat.getChildren().equals(this.child_categories_))
				return true;
		}
		return false;
	}

	/**
	 * For DatabaseObject use only
	 */
	private AcctActionCategory() {
		super(new String[] { "balance_system_" });
	}

	/**
	 * 
	 * @param name
	 * @param parent 
	 * @param normal_balance This is the transaction type of this category
	 */
	public AcctActionCategory(String name, AcctActionCategory parent, TransactionType normal_balance) {
		this(name, parent, normal_balance, 0);
	}

	/**
	 * Used to initialize this AcctActionCategory with a statring balance.
	 * @param name
	 * @param parent
	 * @param normal_balance
	 * @param balance
	 */
	public AcctActionCategory(String name, AcctActionCategory parent, TransactionType normal_balance,
			double balance) {
		this();
		super.name_ = name;
		this.owner_acct_names_ = new HashSet<String>();
		this.child_categories_ = new Hashtable<String, AcctActionCategory>();
		this.balance_system_ = new AcctBalanceSystem(normal_balance, new AcctAmount(balance,
				normal_balance));
		this.parent_ = parent;
	}

	/**
	 * Obtain a collection of Account names which contain this AcctActionCategory object. Child categories inherit parent category owners.
	 * @return
	 */
	public HashSet<String> getOwnerAccts() {
		HashSet<String> owners_ = new HashSet<String>(this.owner_acct_names_);
		if (this.parent_ != null)
			owners_.addAll(this.parent_.getOwnerAccts());
		return owners_;
	}

	/**
	 * Used by the Account object when this AcctActionCategory is added to it.
	 * @param acct_name
	 */
	void addOwnerAcct(String acct_name) {
		this.owner_acct_names_.add(acct_name);
	}

	/**
	 * Maintains the parent of this AcctActionCategory.
	 * <br/>Can be used in situations where this AcctActionCategory is substatuted into another tree structure.
	 * @param parent
	 */
	void setParent(AcctActionCategory parent) {
		this.parent_ = parent;
	}

	/**
	 * This will remove an Account as an owner of this AcctActionCategory object.
	 * @param acct_name Account name
	 * @return Account that was removed, or null if there was an error.
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	boolean removeOwnerAcct(String acct_name) throws DatabaseException, SQLException {
		if (FinanceManager.instance().isInitializationPhase()) {
			return this.owner_acct_names_.remove(acct_name);
		}
		return false;
	}

	/**
	 * To be used only by Account class which maintain a tree of AcctActionCategory
	 * @param category Must have parent==this
	 * @return True if 'category' was added to child list
	 */
	boolean addChildCategory(AcctActionCategory category) {
		if (category.getParent() == this) {
			if (!this.child_categories_.containsKey(category.getName())) {
				this.child_categories_.put(category.getName(), category);
				return true;
			}
		}
		return false;
	}

	/**
	 * Remove the <code>category</code> object from this AcctActionCategory's child tree.
	 * @param category
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	boolean removeChildCategory(AcctActionCategory category) throws DatabaseException, SQLException {
		if (FinanceManager.instance().isInitializationPhase()) {
			return (this.child_categories_.remove(category.getName()) != null);
		}
		return false;
	}

	/**
	 * Replace this AcctActionCategory object with <code>new_cat</code>, which must share a common name and a common parent.
	 * @param new_cat  New category which will take the place of this AcctActionCategory object.
	 * @return
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	public boolean replaceActionCategory(AcctActionCategory new_cat) throws DatabaseException,
			SQLException {
		if (!FinanceManager.instance().isInitializationPhase())
			return false;

		DatabaseObjectBatch batch = new DatabaseObjectBatch();
		try {
			if (this.parent_ != new_cat.getParent()) {
				throw new FinanceException(new_cat, "New category '" + new_cat
						+ "' must have common parent with category being replaced '" + this + "'.");
			}
			if (!new_cat.getName().equals(this.name_)) {
				throw new FinanceException(new_cat, "New category '" + new_cat
						+ "' must have common name to gaurentee unique name per account requirement.");
			}

			Collection<AcctActionCategory> cpy_children = this.getChildren().values();

			batch.addToBatch(this);
			batch.addToBatch(new_cat);
			batch.addToBatch(cpy_children);
			if (this.parent_ != null)
				batch.addToBatch(this.parent_);

			Collection<DatabaseObject> failed_lock = batch.lockBatch();
			if (failed_lock.size() == 0) {
				if (this.parent_ != null) {
					this.parent_.addChildCategory(new_cat);
					this.parent_.removeChildCategory(this);
				}

				for (AcctActionCategory child : cpy_children) {
					child.setParent(new_cat);
				}

				new_cat.owner_acct_names_ = this.owner_acct_names_;

				Collection<DatabaseObject> failed_commit = batch.commitBatch();
				if (failed_commit.size() == 0)
					return true;
				else {
					batch.retrieveBatch();
					String failed_acct_names = DatabaseObjectBatch.getDatabaseObjectNames(failed_lock);
					throw new FinanceException(this, "Failed to commit the following dependent categories: "
							+ failed_acct_names + ".");
				}
			} else {
				String failed_acct_names = DatabaseObjectBatch.getDatabaseObjectNames(failed_lock);
				throw new FinanceException(this, "Failed to lock the following dependent categories: "
						+ failed_acct_names + ".");
			}
		} catch (DatabaseException e) {
			batch.retrieveBatch();
			e.printStackTrace();
			throw e;
		} catch (SQLException e) {
			batch.retrieveBatch();
			e.printStackTrace();
			throw e;
		} finally {
			batch.unlockBatch();
		}
	}

	/**
	 * Get the children AcctActionCategory objects in this AcctActionCategory object's child tree. 
	 * @return
	 */
	public Hashtable<String, AcctActionCategory> getChildren() {
		return this.child_categories_;
	}

	public AcctActionCategory getParent() {
		return this.parent_;
	}

	public boolean isLeafNode() {
		return this.child_categories_.size() == 0;
	}

	public AcctBalanceSystem getBalanceSystem() {
		return this.balance_system_;
	}

	@Override
	public Double getValue() {
		return this.balance_system_.getBalance();
	}

	@Override
	public String toString() {
		return this.getName() + ", " + this.balance_system_.toString();
	}

	@Override
	public void setName(String name) {
		System.err.println("Not allowed to change name of this object");
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
			if (FinanceManager.instance().isInitializationPhase())
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
	/*ADDED FOR GUI PURPOSES*/
}

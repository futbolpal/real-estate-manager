package reman.client.app.finance.templates;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.equations.exceptions.MathException;
import reman.client.app.finance.equations.tokens.Variable;
import reman.client.app.finance.events.ChangeState;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.finance.templates.events.TemplateLineItemEvent;
import reman.client.app.finance.templates.events.TemplateLineItemListner;
import reman.client.app.trees.ICollectionTreeNode;
import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;

/**
 * Organized in a tree, a TemplateContribution will provide organization and structure to Template objects to provide Template functionality to generate Statement objects.
 * <br/>Each TemplateContribution will consist of a map of TemplateLineItem objects.
 * @author Scott
 *
 */
public abstract class TemplateContribution extends DatabaseObject implements
		TemplateLineItemListner, ICollectionTreeNode<TemplateContribution> {

	/**
	 * line items contained within this template
	 */
	private Hashtable<Integer, TemplateLineItem> line_items_;

	private TemplateContribution parent_;

	private ArrayList<TemplateContribution> children_;

	private TransactionType normal_balance_;

	private GlobalVariableSystem gvs_;

	private Template owner_;

	public boolean equals(Object o) {
		if (o instanceof TemplateContribution) {
			TemplateContribution tc = (TemplateContribution) o;
			if (tc.getName().equals(this.name_) && tc.getDescription().equals(this.description_)
					&& tc.getParent() == this.parent_ && tc.getNormalBalance() == this.normal_balance_)
				return true;

		}
		return false;
	}

	/**
	 * DatabaseObject use only
	 */
	protected TemplateContribution() {

	}

	/**
	 * 
	 * @param name
	 * @param description of the relevance this TemplateContribution will have to the financial world once generated.
	 * @param normal_balance of the final generated, then calculated AcctAmount.
	 * @param parent Used to maintain the TemplateContribution tree.
	 */
	public TemplateContribution(String name, String description, TransactionType normal_balance,
			TemplateContribution parent) {
		super.name_ = name;
		super.description_ = description;
		this.normal_balance_ = normal_balance;
		this.parent_ = parent;
		this.children_ = new ArrayList<TemplateContribution>();
		this.line_items_ = new Hashtable<Integer, TemplateLineItem>();
		this.gvs_ = new GlobalVariableSystem();
	}

	/**
	 * Add a child contribution to this TemplateContribution. Keeps the global variable tree in synch.
	 * @param tc
	 * @return
	 */
	boolean addChildContribution(TemplateContribution tc) {
		if (this.children_.contains(tc))
			return false;
		boolean success = this.children_.add(tc);
		if (success) {
			this.updateGlobalVariables(tc.gvs_.getGlobalVars(), false);
		}
		return success;
	}

	/**
	 * Remove a child contribution to this TemplateContribution. Keeps the global variable tree in synch.
	 * @param tc
	 * @return
	 */
	boolean removeChildContribution(TemplateContribution tc) {
		if (!this.children_.contains(tc))
			return false;
		this.updateGlobalVariables(tc.gvs_.getGlobalVars(), true);
		return this.children_.remove(tc);
	}

	/**
	 * This method will add a TemplateLineItem to this TemplateContribution.  This method will track all variables
	 * and maintain the internal GlobalVariableSystem so setting variables can be done at the Template level rather than
	 * at individual TemplateLineItem level.
	 * @param li
	 * @return The key from the internal map of TemplateLineItems maintained in this TemplateContribution and null otherwise.
	 */
	public Integer addLineItem(TemplateLineItem li) {
		Integer key = this.getNextKey();
		if (this.line_items_.contains(key))
			return null;

		/*subscribe to changes made in li*/
		li.addChangeListener(this);

		Hashtable<String, Variable> subject_vars = li.getVariables();

		/*update this contribution's global variables, and delegate down the tree*/
		this.updateGlobalVariables(subject_vars, false);

		this.line_items_.put(key, li);

		return key;
	}

	/**
	 * Remove a TemplateLineItem identified by <code>key</code>.  Removes all unique variables from TemplateLineItem corresponding to <code>key</code> and 
	 * keeps the GlobalVariableSystem synchronized.
	 * @param key
	 * @return
	 */
	public TemplateLineItem removeLineItem(Integer key) {
		if (key == null)
			return null;
		TemplateLineItem curr_item = this.line_items_.get(key);

		/*unsubscribe from line item*/
		curr_item.removeChangeListener(this);

		Hashtable<String, Variable> subject_vars = curr_item.getVariables();

		/*update this contribution's global variables, and delegate down the tree*/
		this.updateGlobalVariables(subject_vars, true);

		return this.line_items_.remove(key);
	}

	/*    xyzb
	 x     yb     zb
							-    zd*/
	/*removes only notify up the chain, adds only notify up the chain*/
	/**
	 * This method updated the internal GlobalVariableSystem and notifies the parent GlobalVariableSystem objects to update.
	 * @param subject_vars  Variable objects which are subject to addition/removal (depending on <code>remove_variables</code>).
	 * @param remove_variables Whether to add/remove the <code>subject_vars</code> from this TempalteContribution's GlobalVariableSystem.
	 */
	private void updateGlobalVariables(Hashtable<String, Variable> subject_vars,
			boolean remove_variables) {

		this.gvs_.updateGlobalVariables(subject_vars, remove_variables);
		this.updateParentGlobalVariables();
	}

	/**
	 * This method will collect all parent TemplateContribution's GlobalVariableSystem objects and insure they are updated.
	 * Also this method will ensure that the owning Template's GlobalVariableSystem of this TemplateContribution is updated.
	 */
	private void updateParentGlobalVariables() {
		TemplateContribution curr_parent = this.parent_;
		while (curr_parent != null) {
			Collection<GlobalVariableSystem> children_gvs = new ArrayList<GlobalVariableSystem>();
			for (TemplateContribution tc : curr_parent.children_) {
				children_gvs.add(tc.gvs_);
			}
			curr_parent.gvs_.updateParentGlobalVariables(children_gvs);
			curr_parent = curr_parent.getParent();
		}
		if (this.owner_ != null) {
			this.owner_.updateGlobalVariables();
		}
	}

	/**
	 * Remove <code>li</code> from this TemplateContribution.
	 * <br/>This method is slower than removing an element by key.
	 * @param li
	 * @return
	 */
	public TemplateLineItem removeLineItem(TemplateLineItem li) {
		Enumeration<Integer> line_item_keys = this.line_items_.keys();
		while (line_item_keys.hasMoreElements()) {
			Integer curr_key = line_item_keys.nextElement();
			if (this.line_items_.get(curr_key).equals(li))
				return this.removeLineItem(curr_key);
		}
		return null;
	}

	/**
	 * To obtain a reference to gvs. Not to be used to edit gvs, only for template to update its global variables.
	 * @return
	 */
	GlobalVariableSystem getGVS() {
		return this.gvs_;
	}

	/**
	 * Ensure the internal GlobalVariableSystem is correct based upon TemplateLineItem change event.
	 */
	public void eventOccurred(TemplateLineItemEvent e) {
		/*remove or add to global variable list depending on state change*/
		gvs_.updateGlobalVariables(e.getTemplateLineItem().getVariables(), e.getState().equals(
				ChangeState.BEFORE_CHANGE));
	}

	/**
	 * Obtain the TemplateLineItem corresponding to <code>key</code>
	 * @param key
	 * @return
	 */
	public TemplateLineItem getLineItem(Integer key) {
		return this.line_items_.get(key);
	}

	/**
	 * Obtain the set of all keys corresponding to all TemplateLineItem objects contained in this.
	 * <br/>This can be used sort the keys and gaurentee an inorder traversal. 
	 * @return
	 */
	public Set<Integer> getLineItemKeySet() {
		return this.line_items_.keySet();
	}

	private Integer getNextKey() {
		return this.line_items_.size();
	}

	/**
	 * The parent TemplateContribution in the tree.
	 */
	public TemplateContribution getParent() {
		return this.parent_;
	}

	/**
	 * The children TemplateContribution objects in the tree.
	 */
	public Collection<TemplateContribution> getChildren() {
		return this.children_;
	}

	/**
	 * When this TemplateContribution is added to a Template, the Template is responsible for invoking this method to update the owning Template.
	 * @param t
	 */
	void setOwner(Template t) {
		this.owner_ = t;
	}

	@Override
	public boolean isLeafNode() {
		return this.children_.size() <= 0;
	}

	/**
	 * The normal balance for the AcctAmount created by the object created by this TemplateContribution's generation.
	 * @return
	 */
	public TransactionType getNormalBalance() {
		return this.normal_balance_;
	}

	/**
	 * This can be used to generate an application of a TemplateContribution. For example a StatementControbution or a JournalEntry.
	 * @return An object of type according to the implementation of this TemplateContribution.
	 */
	public abstract DatabaseObject generateTemplateContribution() throws MathException,
			FinanceException, DatabaseException, SQLException;
}

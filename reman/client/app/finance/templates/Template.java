package reman.client.app.finance.templates;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import reman.client.app.finance.equations.exceptions.MathException;
import reman.client.app.finance.equations.tokens.DoubleToken;
import reman.client.app.finance.equations.tokens.Variable;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.trees.CollectionTreeTraverseUtilitiy;
import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;

/**
 * A Template provides functionality to the end users to create financial entities which may not be provided by default.
 * <br/>The Template maintains a tree structure of TemplateContribution objects.
 * <br/>For example, a Template can be utilized to store re-occurring JournalEntry objects, as to create a new JournalEntry with a known
 * strucutre but with current Account balances.
 * <br/>Another application of the Template is for a Statement that may not be provided by default.  If the end user wishes to view
 * a Statement of arbitrary Accounts in an arbitrary contribution tree strucutre, the Template can provide this functionality.
 * @author Scott
 *
 */
public abstract class Template extends DatabaseObject {

	private GlobalVariableSystem gvs_;

	private ArrayList<TemplateContribution> root_contributions_;

	/**
	 * For DatabaseObject use only.
	 */
	protected Template() {
	}

	/**
	 * 
	 * @param name of this Template.
	 * @param description of the relevance of this Template
	 */
	public Template(String name, String description) {
		this.name_ = name;
		this.description_ = description;
		this.gvs_ = new GlobalVariableSystem();
		this.root_contributions_ = new ArrayList<TemplateContribution>();
	}

	/**
	 * Add a contribution to this template. This will maintain the TemplateContribution tree integrity, or if no parent it will be added to
	 * the collection of root TemplateContribution.
	 * @param tc
	 * @return
	 */
	public boolean addContribution(TemplateContribution tc) {
		if (tc.getParent() == null) {
			if (!this.root_contributions_.contains(tc)) {
				tc.setOwner(this);
				if (this.root_contributions_.add(tc)) {
					this.updateGlobalVariables();
					return true;
				}
			}
		} else {
			TemplateContribution parent = (new CollectionTreeTraverseUtilitiy<TemplateContribution>())
					.getTargetNode(this.root_contributions_, tc.getParent());
			if (parent != null) {
				tc.setOwner(this);
				return tc.getParent().addChildContribution(tc);
			}
		}
		return false;
	}

	/**
	 * Will remove <code>tc</code> and all children from the TemplateContribution tree structure.
	 * This will also keep the GlobalVariableSystem in synch.
	 * @param tc
	 * @return
	 */
	public boolean removeContribution(TemplateContribution tc) {
		TemplateContribution contained_tc = (new CollectionTreeTraverseUtilitiy<TemplateContribution>())
				.getTargetNode(this.root_contributions_, tc);
		if (contained_tc != null) {
			TemplateContribution parent = contained_tc.getParent();
			if (parent != null) {
				if (parent.removeChildContribution(contained_tc)) {
					contained_tc.setOwner(null);
					return true;
				}
			} else {
				if (this.root_contributions_.remove(contained_tc)) {
					contained_tc.setOwner(null);
					this.updateGlobalVariables();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Sets all Variable objects (identified by <code>var_name</code>) contained within this Template to <code>value</code>.
	 * @param var_name Name of the Variable object to set.
	 * @param value Value to set the corresponding Variable object identified by <code>var_name</code>.
	 * @return True if corresponding global variable was found and value was set. False otherwise.
	 */
	public boolean setVariable(String var_name, double value) {
		Variable global_var = this.gvs_.getVariable(var_name);
		if (global_var != null) {
			global_var.setValue(new DoubleToken(value));
			return true;
		}
		return false;
	}

	/**
	 * Synchronize this Template's global variables with all root TemplateContribution nodes.
	 */
	void updateGlobalVariables() {
		Collection<GlobalVariableSystem> children_gvs_collect = new ArrayList<GlobalVariableSystem>(
				this.root_contributions_.size());
		for (TemplateContribution tc : this.root_contributions_) {
			children_gvs_collect.add(tc.getGVS());
		}

		this.gvs_.updateParentGlobalVariables(children_gvs_collect);
	}

	/**
	 * Obtain a copy of the root level TempalteContribution objects contained in this Template.
	 * @return
	 */
	public ArrayList<TemplateContribution> getRootContributions() {
		return new ArrayList<TemplateContribution>(this.root_contributions_);
	}

	/**
	 * This can be used to generate an application of a Template.
	 * <br/>For example to generate Statement or a JournalEntry.
	 * @return An object of type which an implementing Template was designed to generate.
	 */
	public abstract DatabaseObject generateTemplate() throws MathException, FinanceException,
			DatabaseException, SQLException;
}

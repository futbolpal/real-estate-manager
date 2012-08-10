package reman.client.app.finance.gui;

import java.util.Collection;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctActionCategory;
import reman.client.app.trees.MapTreeTraverseUtility;
import reman.common.messaging.DboChangedMessage;

public class AcctActionCategoryTreeModel extends MultiRootTreeModel<AcctActionCategory> {

	private Account root_acct_;

	/**
	 * Create a tree which operates on <code>root_category</code> and all of its children
	 * @param root_category
	 */
	public AcctActionCategoryTreeModel(AcctActionCategory root_category) {
		super(root_category);
	}

	/**
	 * Create a tree model which operates on all AcctActionCategory objects contained within <code>acct</code>
	 * @param acct
	 */
	public AcctActionCategoryTreeModel(Account acct) {
		super(acct.getName() + " Categories");
		this.root_acct_ = acct;
	}

	@Override
	public Object getChild(Object parent, int index) {
		AcctActionCategory[] children = null;
		if (root_msg_.equals(parent)) {
			children = this.root_acct_.getRootActionCategories().values().toArray(
					new AcctActionCategory[0]);
		} else if (parent instanceof AcctActionCategory) {
			AcctActionCategory parent_node = (AcctActionCategory) parent;
			children = parent_node.getChildren().values().toArray(new AcctActionCategory[0]);
		}
		if (children != null) {
			if (children.length > 0 && children.length > index)
				return children[index];
		}
		return null;
	}

	@Override
	public int getChildCount(Object parent) {
		if (root_msg_.equals(parent)) {
			return root_acct_.getRootActionCategories().size();
		}
		if (parent instanceof AcctActionCategory) {
			AcctActionCategory parent_node = (AcctActionCategory) parent;
			return parent_node.getChildren().size();
		}
		return 0;
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		Collection<AcctActionCategory> categories = null;
		if (root_msg_.equals(parent)) {
			categories = this.root_acct_.getRootActionCategories().values();
		} else if (parent instanceof AcctActionCategory) {
			AcctActionCategory parent_node = (AcctActionCategory) parent;
			categories = parent_node.getChildren().values();
		}
		if (categories != null) {
			int index = 0;
			for (AcctActionCategory child_node : categories) {
				if (child_node == child)
					return index;
				index++;
			}
		}
		return -1;
	}

	@Override
	public boolean isLeaf(Object node) {
		if (root_msg_.equals(node)) {
			return root_acct_.getRootActionCategories().size() == 0;
		} else if (node instanceof AcctActionCategory) {
			AcctActionCategory a_node = (AcctActionCategory) node;
			return a_node.isLeafNode();
		}
		return false;
	}

	@Override
	public boolean isContained(AcctActionCategory node) {
		MapTreeTraverseUtility<String, AcctActionCategory> mttu = new MapTreeTraverseUtility<String, AcctActionCategory>();
		AcctActionCategory match = null;

		if (this.root_node_ == null) {
			match = mttu.getTargetNode(this.root_acct_.getRootActionCategories(), node);
		} else {
			if (node == this.root_node_) {
				match = this.root_node_;
			} else {
				match = mttu.getTargetNode(this.root_node_.getChildren(), node);
			}
		}

		return match != null;
	}
	
	public static AcctActionCategory getSelectedLastCategory(JTree cat_tree) {
		TreePath selected_path = cat_tree.getSelectionPath();
		if (selected_path != null && selected_path.getLastPathComponent() instanceof AcctActionCategory) {
			return (AcctActionCategory) selected_path.getLastPathComponent();
		}
		return null;
	}

	@Override
	public void dboChangedEvent(DboChangedMessage m) {
		if (m.getDatabaseObject() instanceof AcctActionCategory) {
			AcctActionCategory cat = (AcctActionCategory) m.getDatabaseObject();
			if (this.isContained(cat)) {
				TreePath root_path = this.getPathToRoot(cat);
				this.valueForPathChanged(root_path, cat);
			}
		}
	}
}

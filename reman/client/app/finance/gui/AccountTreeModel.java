package reman.client.app.finance.gui;

import java.sql.SQLException;
import java.util.Collection;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.accounts.Account;
import reman.client.app.trees.MapTreeTraverseUtility;
import reman.common.database.exceptions.DatabaseException;
import reman.common.messaging.DboChangedMessage;

public class AccountTreeModel extends MultiRootTreeModel<Account> {

	/**
	 * Display all Account objects within the finance engine
	 */
	public AccountTreeModel(String root_msg) {
		super(root_msg);
		try {
			FinanceManager.instance().getAccountManager().retrieve();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Display <code>acct</code> as root node, and all children of <code>acct</code>
	 * @param acct
	 */
	public AccountTreeModel(Account acct) {
		super(acct);
		try {
			acct.retrieve();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Object getChild(Object parent, int index) {
		Account[] children = null;
		try {
			if (root_msg_.equals(parent)) {
				children = FinanceManager.instance().getAccountManager().getRootAccts(false).toArray(
						new Account[0]);

			} else if (parent instanceof Account) {
				Account parent_node = (Account) parent;
				//parent_node.retrieve();
				children = parent_node.getChildren().values().toArray(new Account[0]);
			}
			if (children != null) {
				if (children.length > 0 && children.length > index)
					return children[index];
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getChildCount(Object parent) {
		try {
			if (root_msg_.equals(parent)) {
				return FinanceManager.instance().getAccountManager().getRootAccts(false).size();
			}
			if (parent instanceof Account) {
				Account parent_node = (Account) parent;
				//parent_node.retrieve();
				return parent_node.getChildren().size();
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		try {
			Collection<Account> accounts = null;
			if (root_msg_.equals(parent)) {
				accounts = FinanceManager.instance().getAccountManager().getRootAccts(false);
			} else if (parent instanceof Account) {
				Account parent_node = (Account) parent;
				//parent_node.retrieve();
				accounts = parent_node.getChildren().values();
			}
			if (accounts != null) {
				int index = 0;
				for (Account child_node : accounts) {
					if (child_node == child)
						return index;
					index++;
				}
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public boolean isLeaf(Object node) {
		try {
			if (root_msg_.equals(node)) {
				return FinanceManager.instance().getAccountManager().getRootAccts(false).size() == 0;
			} else if (node instanceof Account) {
				Account a_node = (Account) node;
				//a_node.retrieve();
				return a_node.isLeafNode();
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void dboChangedEvent(DboChangedMessage m) {
		try {
			if (m.getDatabaseObject() instanceof Account) {
				Account acct = (Account) m.getDatabaseObject();
				if (this.root_node_ == null) {
					/*this node will update the tree's structure*/
					//FinanceManager.instance().getAccountManager().retrieve();
					TreePath root_path = this.getPathToRoot(acct);
					this.valueForPathChanged(root_path, acct);
				} else {
					/*this node will only update the tree's structure if it's parent is in the tree*/
					if (acct.getParent() != null) {
						acct.retrieve();
						if (this.isContained(acct.getParent())) {
							TreePath root_path = this.getPathToRoot(acct);
							this.valueForPathChanged(root_path, acct);
						}
					}
				}
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean isContained(Account node) {
		MapTreeTraverseUtility<String, Account> mttu = new MapTreeTraverseUtility<String, Account>();
		Account match = null;
		try {
			if (this.root_node_ == null) {
				Collection<Account> root_accts = FinanceManager.instance().getAccountManager()
						.getRootAccts();
				for (Account root_acct : root_accts) {
					if (node == root_acct) {
						match = root_acct;
						break;
					} else {
						match = mttu.getTargetNode(root_acct.getChildren(), node);
						if (match != null)
							break;
					}
				}
			} else {
				if (node == this.root_node_) {
					match = this.root_node_;
				} else {
					match = mttu.getTargetNode(this.root_node_.getChildren(), node);
				}
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return match != null;
	}

	public static Account getSelectedLastAccount(JTree acct_tree) {
		TreePath selected_path = acct_tree.getSelectionPath();
		if (selected_path != null && selected_path.getLastPathComponent() instanceof Account) {
			return (Account) selected_path.getLastPathComponent();
		}
		return null;
	}
}

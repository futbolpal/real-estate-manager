package reman.client.app.finance.gui;

import java.util.ArrayList;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import reman.client.app.Framework;
import reman.client.app.listeners.DboChangedListener;
import reman.client.app.trees.ITreeNode;

public abstract class MultiRootTreeModel<V extends ITreeNode<V>> implements TreeModel,
		DboChangedListener {

	private ArrayList<TreeModelListener> tree_model_listeners_;
	/**
	 * There can either be a root_msg_ or a root_acct_ (depending if view is global or local to one dbo)
	 */
	protected String root_msg_;

	/**
	 * There can either be a root_msg_ or a root_dbo_ (depending if view is global or local to one dbo)
	 */
	protected V root_node_;
	
	private MultiRootTreeModel() {
		Framework.instance().addDboChangedListener(this);
	}

	/**
	 * Display all ITreeNode<V> objects within the finance engine
	 */
	public MultiRootTreeModel(String root_msg) {
		this();
		this.setRootMsg(root_msg);
		this.root_node_ = null;
		tree_model_listeners_ = new ArrayList<TreeModelListener>();
	}

	/**
	 * Display <code>root_node</code> as root node, and all children of <code>root_node</code>
	 * @param acct
	 */
	public MultiRootTreeModel(V root_node) {
		this();
		this.root_node_ = root_node;
		this.root_msg_ = "";
		tree_model_listeners_ = new ArrayList<TreeModelListener>();
	}

	/**
	 * This method will ensure that the root message node is never null;
	 * @param msg
	 */
	private void setRootMsg(String msg) {
		if (msg != null)
			this.root_msg_ = msg;
		else
			this.root_msg_ = "";
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		this.tree_model_listeners_.add(l);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		this.tree_model_listeners_.remove(l);
	}

	private void fireTreeNodeInserted(TreeModelEvent e) {
		for (TreeModelListener l : this.tree_model_listeners_)
			l.treeNodesInserted(e);
	}

	private void fireTreeNodeRemoved(TreeModelEvent e) {
		for (TreeModelListener l : this.tree_model_listeners_)
			l.treeNodesRemoved(e);
	}

	private void fireTreeStructureChange(TreeModelEvent e) {
		for (TreeModelListener l : this.tree_model_listeners_)
			l.treeStructureChanged(e);
	}

	public TreePath getPathToRoot(V node) {
		if (node == null)
			return null;
		ArrayList<Object> path = new ArrayList<Object>();
		ITreeNode<V> curr_node = node;
		while (curr_node != null) {
			path.add(0, curr_node);
			curr_node = (ITreeNode<V>) curr_node.getParent();
		}

		if (root_node_ == null) {
			path.add(0, root_msg_);
		}

		return new TreePath(path.toArray());
	}

	private TreePath removeLastElement(TreePath path) {
		ArrayList<Object> p = new ArrayList<Object>();
		Object[] old_path = path.getPath();
		for (int i = 0; i < old_path.length - 1; i++)
			p.add(old_path[i]);

		return new TreePath(p);
	}

	public void nodeRemoved(V node, TreePath root_path) {
		if (root_path == null)
			root_path = this.getPathToRoot(node);

		/*notify that parent node's child structure has changed*/
		if (root_path.getLastPathComponent() == node)
			root_path = this.removeLastElement(root_path);

		TreeModelEvent e = new TreeModelEvent(node, root_path);
		this.fireTreeStructureChange(e);
	}

	public void nodeInserted(V node, TreePath root_path) {
		if (root_path == null)
			root_path = getPathToRoot(node);

		/*notify that parent node's child structure has changed*/
		if (root_path.getLastPathComponent() == node)
			root_path = this.removeLastElement(root_path);

		TreeModelEvent e = new TreeModelEvent(node, root_path);
		this.fireTreeStructureChange(e);
	}

	public boolean isMsgRoot() {
		return this.root_node_ == null;
	}

	@Override
	public Object getRoot() {
		if (this.isMsgRoot())
			return this.root_msg_;
		else
			return this.root_node_;
	}

	public abstract boolean isContained(V node);

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		if (path.getLastPathComponent() instanceof ITreeNode && newValue instanceof ITreeNode) {
			V old_node = (V) path.getLastPathComponent();
			V new_node = (V) newValue;

			TreeModelEvent e = new TreeModelEvent(new_node, path);
			this.fireTreeStructureChange(e);
		}
	}
}

package reman.client.app.trees;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class CollectionTreeTraverseUtilitiy<V extends ICollectionTreeNode<V>> {

	public CollectionTreeTraverseUtilitiy() {
	}

	/**
	 * V must have a constructor which takes V as a parameter 
	 * @param root_collect
	 * @return
	 */
	/*public Collection<V> valueCopyTree(Collection<V> root_collect){
		Collection<V> root_cpy_nodes = new ArrayList<V>(root_collect.size());
		/*build tree from bottom up/
		for(V curr_root : root_collect){
			V curr_node = curr_root;
			while(!curr_node.isLeafNode()){
				curr_node = curr_node.
			}
		}
		
	}*/

	/**
	 * Obtain a the tree flattened into a collection (ordered in breadth first manner).
	 */
	public Collection<V> getFlatTree(Collection<V> root_collect) {
		Collection<V> all_v = new ArrayList<V>();

		for (V root_v : root_collect) {
			Queue<V> v_q = new LinkedList<V>();
			v_q.add(root_v);

			while (v_q.size() > 0) {
				V curr_v = v_q.poll();

				all_v.add(curr_v);

				v_q.addAll(curr_v.getChildren());
			}
		}

		return all_v;
	}

	/**
	 * Obtain the first matching result (breadth first search) that == target_node
	 * @param root_collect Collection of root nodes of the tree to traverse
	 * @param target_node
	 * @return
	 */
	public V getTargetNode(Collection<V> root_collect, V target_value) {
		if (root_collect == null || target_value == null)
			return null;

		V result_from_curr_root;
		for (V curr_root_node : root_collect) {
			if ((result_from_curr_root = this.getTargetNode(curr_root_node, target_value)) != null)
				return result_from_curr_root;
		}
		return null;
	}

	private V getTargetNode(V root_node, V target_node) {
		Queue<V> q = new LinkedList<V>();
		q.add(root_node);

		while (q.size() > 0) {
			V curr_node = q.poll();

			/*check if this is the target node*/
			if (curr_node == target_node)
				return curr_node;

			/*append all children to the bfs check queue*/
			q.addAll(curr_node.getChildren());
		}
		return null;
	}
}

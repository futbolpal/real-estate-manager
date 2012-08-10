package reman.client.app.trees;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Perform tree operations where multiple top level (root nodes) maintained in a hashtable.
 * @author Scott
 *
 * @param <K>
 * @param <V> must implement ITreeTraversable<K, V>
 */
public class MapTreeTraverseUtility<K, V extends IMapTreeNode<K, V>> {

	public MapTreeTraverseUtility() {

	}

	public Collection<V> getFlatTree(Hashtable<K, V> root_map) {
		Collection<V> all_v = new ArrayList<V>();

		for (V root_v : root_map.values()) {
			Queue<V> v_q = new LinkedList<V>();
			v_q.add(root_v);

			while (v_q.size() > 0) {
				V curr_v = v_q.poll();

				all_v.add(curr_v);

				v_q.addAll(curr_v.getChildren().values());
			}
		}

		return all_v;
	}

	/**
	 * Obtain the first matching result (breadth first search) that has the key of target_key
	 * Uses equals() method to compare keys
	 * @param root_map
	 * @param target_key
	 * @return
	 */
	public V getTargetNode(Hashtable<K, V> root_map, K target_key) {
		if (root_map == null || target_key == null)
			return null;

		V result_from_curr_root;
		Enumeration<K> top_level_keys = root_map.keys();
		while (top_level_keys.hasMoreElements()) {
			K curr_top_level_key = top_level_keys.nextElement();
			V curr_top_level_node = root_map.get(curr_top_level_key);

			/*must check the key of each root node, because this isn't possible during the bfs*/
			if (curr_top_level_key.equals(target_key))
				return curr_top_level_node;

			if ((result_from_curr_root = this.getTargetNode(curr_top_level_node, target_key)) != null)
				return result_from_curr_root;
		}
		return null;
	}

	private V getTargetNode(V root_node, K target_key) {
		Queue<V> q = new LinkedList<V>();
		q.add(root_node);

		while (q.size() > 0) {
			V curr_node = q.poll();

			/*check if this key is a child key*/
			if (curr_node.getChildren().containsKey(target_key))
				return curr_node.getChildren().get(target_key);

			/*append all the children to bfs queue checked*/
			q.addAll(curr_node.getChildren().values());
		}
		return null;
	}

	/**
	 * Obtain the first matching result (breadth first search) that == target_node
	 * @param root_map
	 * @param target_node
	 * @return
	 */
	public V getTargetNode(Hashtable<K, V> root_map, V target_node) {
		if (root_map == null || target_node == null)
			return null;

		V result_from_curr_root;
		Enumeration<K> top_level_keys = root_map.keys();
		while (top_level_keys.hasMoreElements()) {
			V curr_top_level_node = root_map.get(top_level_keys.nextElement());

			if ((result_from_curr_root = this.getTargetNode(curr_top_level_node, target_node)) != null)
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
			q.addAll(curr_node.getChildren().values());
		}
		return null;
	}
}

package reman.client.app.trees;

import java.util.Hashtable;

public interface IMapTreeNode<K, V> extends ITreeNode<V> {
	public Hashtable<K, V> getChildren();
}

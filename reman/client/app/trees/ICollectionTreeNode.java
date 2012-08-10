package reman.client.app.trees;

import java.util.Collection;

public interface ICollectionTreeNode<V> extends ITreeNode<V> {

	public Collection<V> getChildren();
}

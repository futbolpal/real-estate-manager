package reman.client.app.trees;


public interface ITreeNode<V> {
	public boolean isLeafNode();
	public V getParent();
	
}

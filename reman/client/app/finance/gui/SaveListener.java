package reman.client.app.finance.gui;

import java.awt.Component;

public interface SaveListener<V> {
	public void newNode(V new_node, Component source);
}

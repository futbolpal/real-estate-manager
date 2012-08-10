package reman.client.app.finance.gui;

import reman.client.gui.forms.DboFormPanel;
import reman.common.database.DatabaseObject;

public interface FormSaveListener<V extends DatabaseObject> {
	public void replaceNode(V old_node, V new_node, DboFormPanel<V> source);

	public void newNode(V new_node, DboFormPanel<V> source);
}

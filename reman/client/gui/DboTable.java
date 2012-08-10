package reman.client.gui;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import reman.client.app.listeners.DboChangedListener;
import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;
import reman.common.messaging.DboChangedMessage;
import reman.common.messaging.DboLockedMessage;

public abstract class DboTable extends JTable implements DboChangedListener {
	private boolean dynamic_;
	private ArrayList<Integer> row_locks_;

	public DboTable() {
		row_locks_ = new ArrayList<Integer>();
		this.setSelectionModel(new DboListSelectionModel(this));
	}

	public void setDynamicMode(boolean flag) {
		dynamic_ = flag;
	}

	public boolean isDynamicMode() {
		return dynamic_;
	}

	public boolean isCellEditable(int r, int c) {
		return !row_locks_.contains(r);
	}

	public void dboChangedEvent(DboChangedMessage m) {
		if (m instanceof DboLockedMessage) {
			DboLockedMessage lm = ((DboLockedMessage) m);
			Integer row = this.getRowOfDatabaseObject(lm.getDatabaseObject());
			if (lm.isLocked()) {
				row_locks_.add(row);
			} else {
				row_locks_.remove(row);
			}
		}
	}

	public abstract DatabaseObject getDatabaseObjectAtRow(int r);

	public abstract int getRowOfDatabaseObject(DatabaseObject o);

	public abstract void commitListOwner() throws SQLException, DatabaseException;

	public void update() {
		((DefaultTableModel) this.getModel()).fireTableDataChanged();
	}
}

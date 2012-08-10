package reman.client.gui;

import java.sql.SQLException;

import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.LoggedInException;

public class DboListSelectionModel extends DefaultListSelectionModel implements
		ListSelectionListener {
	private DboTable owner_;
	private DatabaseObject previous_;

	public DboListSelectionModel(DboTable owner) {
		owner_ = owner;
		this.setSelectionMode(SINGLE_SELECTION);
		this.addListSelectionListener(this);
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() || e.getFirstIndex() < 0)
			return;

		/* Unlock old object */
		if (previous_ != null) {
			try {
				previous_.commit();
				previous_.unlock();
			} catch (LoggedInException e1) {
				e1.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			} catch (DatabaseException e1) {
				e1.printStackTrace();
			}
		}

		int row = e.getFirstIndex();
		DatabaseObject o = owner_.getDatabaseObjectAtRow(row);
		try {
			if (!o.isLocked()) {
				o.lock();
				previous_ = o;
			} else {

			}
		} catch (LoggedInException e1) {
			e1.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
}

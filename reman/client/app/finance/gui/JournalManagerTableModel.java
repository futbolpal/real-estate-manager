package reman.client.app.finance.gui;

import java.sql.SQLException;

import javax.swing.table.AbstractTableModel;

import reman.client.app.finance.journals.Journal;
import reman.client.app.finance.journals.JournalEntry;
import reman.client.app.listeners.DboChangedListener;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.ExceedMaxCommitException;
import reman.common.messaging.DboChangedMessage;

public class JournalManagerTableModel extends AbstractTableModel {

	private final String[] COLUMN_NAMES = { "Entry Id", "Date", "Description" };

	private Journal journal_;

	public JournalManagerTableModel() {
		this("Journal");
	}

	public JournalManagerTableModel(String journal_name) {
		this(new Journal(journal_name));
	}

	public JournalManagerTableModel(Journal j) {
		this.journal_ = j;
	}

	public String getColumnName(int c) {
		return COLUMN_NAMES[c];
	}

	public Journal getJournal() {
		return this.journal_;
	}

	public Integer addEntry(JournalEntry je) {
		try {
			if (!this.journal_.contains(je))
				return this.journal_.addJournalEntry(je);
		} catch (ExceedMaxCommitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public JournalEntry getEntry(int row) {
		try {
			return this.journal_.getJournalEntry(row);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public int getRowCount() {
		return this.journal_.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Integer key = rowIndex;
		try {
			JournalEntry entry = journal_.getJournalEntry(key);
			switch (columnIndex) {
			case 0:
				return key;
			case 1:
				return entry.getOccurredTime();
			case 2:
				return entry.getDescription();
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}

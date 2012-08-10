package reman.client.app.finance.gui;

import java.sql.Timestamp;
import java.util.Date;

import javax.swing.table.AbstractTableModel;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.journals.JournalEntry;
import reman.client.app.finance.journals.JournalEntryLineItem;

public class JournalEntryTableModel extends AbstractTableModel {

	private final String[] COLUMN_NAMES = { "Account", "Category", "Debit", "Credit" };

	private JournalEntry entry_;

	public JournalEntryTableModel() {
		this("Journal Entry");
	}

	public JournalEntryTableModel(String description) {
		this(description, new Timestamp((new Date()).getTime()));
	}

	public JournalEntryTableModel(String description, Timestamp occurred) {
		this(new JournalEntry(description, occurred));
	}

	public JournalEntryTableModel(JournalEntry entry) {
		this.entry_ = entry;
	}

	public String getColumnName(int c) {
		return COLUMN_NAMES[c];
	}

	public JournalEntry getJournalEntry() {
		return this.entry_;
	}

	public boolean addLineItem(JournalEntryLineItem line_item) {
		int index = -1;
		if ((index = this.entry_.addLineItem(line_item)) >= 0) {
			this.fireTableDataChanged();
			return true;
		}
		return false;
	}

	public boolean removeLineItem(int row) {
		if (this.entry_.removeLineItem(row) != null) {
			this.fireTableDataChanged();
			return true;
		}
		return false;
	}

	public JournalEntryLineItem getLineItem(int row) {
		return this.entry_.getLineItem(row);
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public int getRowCount() {
		return this.entry_.getLineItems().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		JournalEntryLineItem line_item = entry_.getLineItem(rowIndex);
		boolean credit_amt = line_item.getAmount().getTransactionType() == TransactionType.CREDIT;
		switch (columnIndex) {
		case 0:
			return line_item.getAccount();
		case 1:
			return line_item.getCategory();
		case 2:
			if (!credit_amt)
				return line_item.getAmount().getAmount();
			break;
		case 3:
			if (credit_amt)
				return line_item.getAmount().getAmount();
			break;
		}
		return null;
	}

}

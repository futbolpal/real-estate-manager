package reman.client.app.finance.gui;

import javax.swing.table.AbstractTableModel;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.journals.JournalEntryLineItem;
import reman.client.app.finance.ledger.Ledger;

public class LedgerTableModel extends AbstractTableModel {

	private final String[] COLUMN_NAMES = { "Date", "Account", "Category", "Debit", "Credit" };

	private Ledger ledger_;

	public LedgerTableModel(Ledger ledger) {
		this.ledger_ = ledger;
	}

	public String getColumnName(int c) {
		return COLUMN_NAMES[c];
	}

	public Ledger getLedger() {
		return this.ledger_;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public int getRowCount() {
		return this.ledger_.getLineItems().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		JournalEntryLineItem line_item = this.ledger_.getLineItem(rowIndex);
		boolean credit_amt = line_item.getAmount().getTransactionType() == TransactionType.CREDIT;
		switch (columnIndex) {
		case 0:
			return line_item.getTime();
		case 1:
			return line_item.getAccount();
		case 2:
			return line_item.getCategory();
		case 3:
			if (!credit_amt)
				return line_item.getAmount().getAmount();
			break;
		case 4:
			if (credit_amt)
				return line_item.getAmount().getAmount();
			break;
		}
		return null;
	}
}

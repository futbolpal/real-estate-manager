package reman.client.app.finance.invoice;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.journals.JournalEntryLineItem;

public class EntryLineItem extends InvoiceLineItem {

	public EntryLineItem(JournalEntryLineItem item) {
		super(Integer.toString(item.getAccount().getAcctId()),
				(item.getAmount().getTransactionType() == TransactionType.DEBIT) ? item.getValue() : -item
						.getValue(), item.getAccount().getName(), item.getDescription());
	}
}

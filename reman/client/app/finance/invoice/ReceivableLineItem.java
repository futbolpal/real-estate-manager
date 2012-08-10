package reman.client.app.finance.invoice;

public class ReceivableLineItem extends InvoiceLineItem {

	protected ReceivableLineItem() {
	}

	public ReceivableLineItem(String transaction_code, String address, int ledger_reference,
			double amt) {
		super(transaction_code, amt, address, null);
		this.setLedgerReference(ledger_reference);
	}
}

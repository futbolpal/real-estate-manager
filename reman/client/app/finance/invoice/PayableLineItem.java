package reman.client.app.finance.invoice;

public class PayableLineItem extends InvoiceLineItem {

	protected PayableLineItem() {
	}

	public PayableLineItem(String vendor_code, String vendor_name, String order_num,
			Integer ledger_ref_num, double amt) {
		super(vendor_code,amt,vendor_name,null);
		this.setLedgerReference(ledger_ref_num);
		this.setReference(order_num);
	}
}

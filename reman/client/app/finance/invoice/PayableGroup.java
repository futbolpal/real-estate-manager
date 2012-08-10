package reman.client.app.finance.invoice;

public class PayableGroup extends InvoiceGroup {

	public PayableGroup(String description) {
		super(description);
	}

	public boolean add(InvoiceLineItem item) {
		if (item instanceof PayableLineItem)
			return super.add(item);
		return false;
	}

	public boolean remove(InvoiceLineItem item) {
		if (item instanceof PayableLineItem)
			return super.remove(item);
		return false;
	}

	@Override
	protected float[] getColumnWidths() {
		return new float[] { 15f, 40f, 15f, 15f, 15f };
	}

	@Override
	protected String[] getColumnNames() {
		return new String[] { "Vendor #", "Vendor Name", "Ledger #", "Order #", "Amount" };
	}
}

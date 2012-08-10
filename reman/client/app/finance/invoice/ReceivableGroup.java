package reman.client.app.finance.invoice;

public class ReceivableGroup extends InvoiceGroup {

	public ReceivableGroup(String description) {
		super(description);
	}

	public boolean add(InvoiceLineItem item) {
		if (item instanceof ReceivableLineItem)
			return super.add(item);
		return false;
	}

	public boolean remove(InvoiceLineItem item) {
		if (item instanceof ReceivableLineItem)
			return super.remove(item);
		return false;
	}

	@Override
	protected float[] getColumnWidths() {
		return new float[] { 15f, 55f, 15f, 15f };
	}

	@Override
	protected String[] getColumnNames() {
		return new String[] { "Transaction #", "Address", "Ledger #", "Amount" };
	}
}

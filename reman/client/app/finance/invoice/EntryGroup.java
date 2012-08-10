package reman.client.app.finance.invoice;

public class EntryGroup extends InvoiceGroup {

	public EntryGroup(String description) {
		super(description);
	}

	public boolean add(InvoiceLineItem item) {
		if (item instanceof EntryLineItem)
			return super.add(item);
		return false;
	}

	public boolean remove(InvoiceLineItem item) {
		if (item instanceof EntryLineItem)
			return super.remove(item);
		return false;
	}

	@Override
	protected float[] getColumnWidths() {
		return new float[] { 10f, 35f, 40f, 15f };
	}

	@Override
	protected String[] getColumnNames() {
		return new String[] { "Acct #", "Acct Name", "Description", "Amount" };
	}
}

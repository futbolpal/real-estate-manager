package reman.client.app.finance.invoice;

public class AgentCommissionGroup extends InvoiceGroup {

	public AgentCommissionGroup(String description) {
		super(description);
	}

	public boolean add(InvoiceLineItem item) {
		if (item instanceof AgentCommissionLineItem)
			return super.add(item);
		return false;
	}

	public boolean remove(InvoiceLineItem item) {
		if (item instanceof AgentCommissionLineItem)
			return super.remove(item);
		return false;
	}

	@Override
	protected float[] getColumnWidths() {
		return new float[] { 10f, 25f, 10f, 15f, 25f, 15f };
	}

	@Override
	protected String[] getColumnNames() {
		return new String[] { "Agent #", "Agent Name", "Ledger #", "Transaction #", "Address", "Amount" };
	}
}

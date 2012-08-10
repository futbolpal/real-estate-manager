package reman.client.app.finance.invoice;

import reman.client.basetypes.Agent;

import com.lowagie.text.pdf.PdfPTable;

public class AgentCommissionLineItem extends InvoiceLineItem {

	protected AgentCommissionLineItem() {
	}

	public AgentCommissionLineItem(Agent agent, String transaction_num, String property_address,
			double amt, int ledger_reference) {
		super(Long.toString(agent.getID()), amt, agent.getName(), property_address);
		this.setReference(transaction_num);
		this.setLedgerReference(ledger_reference);
	}
}

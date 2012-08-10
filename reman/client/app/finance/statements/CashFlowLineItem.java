package reman.client.app.finance.statements;

import reman.client.app.finance.accounts.AcctBalanceSystem;

import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

public class CashFlowLineItem extends BalanceSystemLineItem {
	private boolean total_amt_;

	private CashFlowLineItem() {
	}

	/**
	 * 
	 * @param description
	 * @param system
	 * @param total_amt If this amount should be displayed in the total column
	 */
	public CashFlowLineItem(String description, AcctBalanceSystem system, boolean total_amt) {
		super(description, system);
		this.total_amt_ = total_amt;
	}

	@Override
	public void buildTable(PdfPTable table) {
		PdfPCell empty_cell = new PdfPCell(new Paragraph(""));
		if (total_amt_) {
			table.addCell(description_);
			table.addCell(empty_cell);
			Paragraph amt_p = new Paragraph(super.getBalanceSystem().toString());
			amt_p.setAlignment(Element.ALIGN_RIGHT);
			PdfPCell amt = new PdfPCell(amt_p);
			amt.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(amt);
		} else {
			super.buildTable(table);
			table.addCell(empty_cell);
		}
	}
}

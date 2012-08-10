package reman.client.app.finance.statements;

import reman.client.app.finance.accounts.AcctBalanceSystem;

import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

public class BalanceSystemLineItem extends StatementLineItem {

	private AcctBalanceSystem snap_shot_system_;

	protected BalanceSystemLineItem() {
	}

	public BalanceSystemLineItem(String description, AcctBalanceSystem system) {
		super(description);
		this.snap_shot_system_ = new AcctBalanceSystem(system.getNormalBalance(), system
				.getAcctAmount());
	}

	public AcctBalanceSystem getBalanceSystem() {
		return this.snap_shot_system_;
	}

	@Override
	public void buildTable(PdfPTable table) {
		table.addCell(description_);
		Paragraph amt_p = new Paragraph(snap_shot_system_.toString());
		amt_p.setAlignment(Element.ALIGN_RIGHT);
		PdfPCell amt = new PdfPCell(amt_p);
		//amt.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(amt);
		/*PdfPCell debit_cell = new PdfPCell();
		PdfPCell credit_cell = new PdfPCell();
		if (balance_system_.getNormalBalance() == TransactionType.DEBIT) {
			debit_cell.addElement(new Phrase(balance_system_.toString()));
			credit_cell.addElement(new Phrase(""));
		} else {
			debit_cell.addElement(new Phrase(""));
			credit_cell.addElement(new Phrase(balance_system_.toString()));
		}
		table.addCell(debit_cell);
		table.addCell(credit_cell);*/
	}
}

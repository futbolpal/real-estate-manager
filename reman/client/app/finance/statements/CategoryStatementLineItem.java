package reman.client.app.finance.statements;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.AcctActionCategory;

import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * Provides functionality for each line item within the CategoryStatement.  Each CategoryStatementLineItem is associated with an AcctActionCategory
 * which gives it's AcctBalanceSystem contribution toward the CategoryStatement's calculation.
 * @author Scott
 *
 */
public class CategoryStatementLineItem extends BalanceSystemLineItem {

	private AcctActionCategory cat_;

	/**
	 * DatabaseObject use only.
	 */
	private CategoryStatementLineItem() {
	}

	public CategoryStatementLineItem(AcctActionCategory category) {
		super(category.getDescription(), category.getBalanceSystem());
		this.cat_ = category;
	}

	public AcctActionCategory getCategory() {
		return this.cat_;
	}

	/**
	 * CATEGORY NAME | DESCRIPITION | (DEBIT BALANCE) | (CREDIT BALANCE)
	 */
	@Override
	public void buildTable(PdfPTable table) {
		table.addCell(cat_.getName());
		table.addCell(this.description_);
		PdfPCell debit_cell = new PdfPCell();
		PdfPCell credit_cell = new PdfPCell();
		if (cat_.getBalanceSystem().getNormalBalance() == TransactionType.DEBIT) {
			debit_cell.addElement(new Phrase(this.cat_.getBalanceSystem().toString()));
			credit_cell.addElement(new Phrase(""));
		} else {
			debit_cell.addElement(new Phrase(""));
			credit_cell.addElement(new Phrase(this.cat_.getBalanceSystem().toString()));
		}
		table.addCell(debit_cell);
		table.addCell(credit_cell);
	}
}

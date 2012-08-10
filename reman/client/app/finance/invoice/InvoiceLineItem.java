package reman.client.app.finance.invoice;

import reman.common.database.DatabaseObject;

import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

public abstract class InvoiceLineItem extends DatabaseObject {
	private String id_code_;
	private double amt_;
	private String ref_num_;
	private Integer ledger_ref_num_;

	protected InvoiceLineItem() {
	}

	public InvoiceLineItem(String id_code, double amt, String name, String description) {
		this.name_ = name;
		this.description_ = description;
		this.id_code_ = id_code;
		this.amt_ = amt;
	}

	public Integer getLedgerReference() {
		return this.ledger_ref_num_;
	}

	public void setLedgerReference(Integer ref) {
		this.ledger_ref_num_ = ref;
	}

	public void setIdCode(String code) {
		this.id_code_ = code;
	}

	public String getIdCode() {
		return this.id_code_;
	}

	public void setAmount(double amt) {
		this.amt_ = amt;
	}

	public double getAmount() {
		return this.amt_;
	}

	public void setReference(String ref) {
		this.ref_num_ = ref;
	}

	public String getReference() {
		return this.ref_num_;
	}

	public void buildTable(PdfPTable table) {
		table.addCell(this.id_code_);
		table.addCell(this.name_);
		if (this.ledger_ref_num_ != null)
			table.addCell(Integer.toString(this.ledger_ref_num_));
		if (this.ref_num_ != null)
			table.addCell(this.ref_num_);
		if (this.description_ != null)
			table.addCell(this.description_);
		table.addCell(getCell(Double.toString(this.amt_), Element.ALIGN_RIGHT, Element.ALIGN_CENTER));
	}

	private PdfPCell getCell(Object o, Integer h_align, Integer v_align) {
		Phrase p = new Phrase();
		p.add(o);
		PdfPCell cell = new PdfPCell(p);
		if (h_align != null)
			cell.setHorizontalAlignment(h_align);
		if (v_align != null)
			cell.setVerticalAlignment(v_align);
		return cell;
	}
}

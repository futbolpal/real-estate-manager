package reman.client.app.finance.invoice;

import java.util.ArrayList;

import reman.common.database.DatabaseObject;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

public abstract class InvoiceGroup extends DatabaseObject {

	private ArrayList<InvoiceLineItem> line_items_;

	protected InvoiceGroup() {
		line_items_ = new ArrayList<InvoiceLineItem>();
	}

	public InvoiceGroup(String description) {
		this();
		this.description_ = description;
	}

	public boolean add(InvoiceLineItem item) {
		return this.line_items_.add(item);
	}

	public boolean remove(InvoiceLineItem item) {
		return this.line_items_.remove(item);
	}

	public double getTotalAllocated() {
		double amt = 0;
		for (InvoiceLineItem item : line_items_) {
			amt += item.getAmount();
		}
		return amt;
	}

	protected abstract String[] getColumnNames();

	protected abstract float[] getColumnWidths();

	public void buildPdf(Document pdf_doc) throws DocumentException {

		Paragraph head = new Paragraph(this.description_);
		head.setAlignment(Paragraph.ALIGN_CENTER);
		head.setFont(new Font(Font.HELVETICA, 14, Font.BOLD));
		head.setSpacingAfter(10);
		pdf_doc.add(head);

		String[] column_names = this.getColumnNames();
		PdfPTable table = new PdfPTable(column_names.length);
		table.setWidthPercentage(100f);
		table.setWidths(this.getColumnWidths());
		for (String col_head : column_names) {
			Paragraph p = new Paragraph(col_head);
			p.setFont(new Font(Font.COURIER, 12, Font.BOLD));
			p.setAlignment(Element.ALIGN_CENTER);
			PdfPCell col_h = new PdfPCell(p);
			col_h.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(col_h);
		}

		for (InvoiceLineItem item : line_items_) {
			item.buildTable(table);
		}

		PdfPCell total_des = new PdfPCell();
		total_des.setColspan(table.getNumberOfColumns() - 1);
		Paragraph des = new Paragraph("Total Allocated:");
		des.setAlignment(Paragraph.ALIGN_RIGHT);
		total_des.addElement(des);
		table.addCell(total_des);

		PdfPCell total = new PdfPCell();
		Paragraph t = new Paragraph(Double.toString(this.getTotalAllocated()));
		t.setAlignment(Paragraph.ALIGN_RIGHT);
		total.addElement(t);
		table.addCell(total);

		pdf_doc.add(table);
	}
}

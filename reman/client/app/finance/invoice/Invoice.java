package reman.client.app.finance.invoice;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import reman.client.app.finance.statements.Statement;
import reman.common.database.DatabaseObject;
import reman.common.database.UserManager;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public class Invoice extends DatabaseObject {
	private String invoice_code_;
	private String order_code_;
	private String vendor_;
	private Timestamp invoice_date_;
	private Timestamp due_date_;

	private ArrayList<InvoiceGroup> groups_;

	protected Invoice() {
	}

	public Invoice(String description, String vendor, String invoice_code, String order_code,
			Timestamp due_date) {
		this.groups_ = new ArrayList<InvoiceGroup>();
		this.vendor_ = vendor;
		this.description_ = description;
		this.invoice_code_ = invoice_code;
		this.order_code_ = order_code;
		this.invoice_date_ = new Timestamp((new Date()).getTime());
		this.due_date_ = due_date;
	}

	public boolean add(InvoiceGroup g) {
		return this.groups_.add(g);
	}

	public boolean remove(InvoiceGroup g) {
		return this.groups_.remove(g);
	}

	public void exportToPdf(String file_name) throws IOException {
		if (!file_name.endsWith(".pdf"))
			file_name += ".pdf";

		/*company name*/
		String company_name = "ReMax";//OfficeProjectManager.instance().getCurrentProject().getName()
		Chunk comp_name = new Chunk(company_name);
		comp_name.setUnderline(0.2f, -2f);
		comp_name.setFont(new Font(Font.TIMES_ROMAN, 18, Font.BOLD));
		Paragraph comp_name_p = new Paragraph(comp_name);
		comp_name_p.setAlignment(Element.ALIGN_CENTER);

		/*statement description*/
		Paragraph description_p = new Paragraph(this.description_);
		description_p.setFont(new Font(Font.HELVETICA, Font.DEFAULTSIZE, Font.BOLD));
		description_p.setAlignment(Element.ALIGN_CENTER);
		//description_p.setSpacingAfter(10);

		/*statement description*/
		Paragraph invoice_vendor_p = new Paragraph("Vendor: " + this.vendor_);
		invoice_vendor_p.setFont(new Font(Font.HELVETICA, Font.DEFAULTSIZE, Font.BOLD));
		invoice_vendor_p.setAlignment(Element.ALIGN_CENTER);
		invoice_vendor_p.setSpacingAfter(1);

		/*statement header table*/
		PdfPTable head = new PdfPTable(2);
		head.setWidthPercentage(100f);
		head.setSpacingAfter(10);
		Font header_font = new Font(Font.HELVETICA, 10, Font.NORMAL);
		Paragraph invoice_code = new Paragraph("Invoice #: " + this.invoice_code_, header_font);
		invoice_code.setAlignment(Element.ALIGN_LEFT);
		PdfPCell ic_cell = new PdfPCell(invoice_code);
		ic_cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		ic_cell.setBorderWidth(0);
		head.addCell(ic_cell);

		String invoice_date = Statement.getDateFormat().format(this.invoice_date_);
		Paragraph invoice_date_p = new Paragraph("Invoice Date: " + invoice_date, header_font);
		invoice_date_p.setAlignment(Element.ALIGN_RIGHT);
		PdfPCell id_cell = new PdfPCell(invoice_date_p);
		id_cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		id_cell.setBorderWidth(0);
		head.addCell(id_cell);

		Paragraph order_code = new Paragraph("Order #: " + this.order_code_, header_font);
		order_code.setAlignment(Element.ALIGN_LEFT);
		PdfPCell o_cell = new PdfPCell(order_code);
		o_cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		o_cell.setBorderWidth(0);
		head.addCell(o_cell);

		String due_date = Statement.getDateFormat().format(this.due_date_);
		Paragraph invoice_due_p = new Paragraph("Due Date " + due_date, header_font);
		invoice_due_p.setAlignment(Element.ALIGN_RIGHT);
		PdfPCell i_cell = new PdfPCell(invoice_due_p);
		i_cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		i_cell.setBorderWidth(0);
		head.addCell(i_cell);

		try {
			Document pdf = new Document();
			PdfWriter writer = PdfWriter.getInstance(pdf, new FileOutputStream(file_name));
			pdf.open();
			writer.setPageEvent(new PdfFormater(writer));

			/*add header info*/
			pdf.add(comp_name_p);
			pdf.add(invoice_vendor_p);
			pdf.add(head);

			/*Incorporate groups*/
			for (InvoiceGroup g : this.groups_) {
				g.buildPdf(pdf);
			}

			pdf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class PdfFormater extends PdfPageEventHelper {
		/** A template that will hold the total number of pages. */
		public PdfTemplate tpl_;
		/** The font that will be used. */
		public BaseFont font_;

		public int font_size_;

		/** A template that will hold the total number of pages. */
		public PdfTemplate tpl;

		public PdfFormater(PdfWriter writer) throws DocumentException, IOException {
			this(BaseFont.createFont("Helvetica", BaseFont.WINANSI, false), writer);
		}

		public PdfFormater(BaseFont font, PdfWriter writer) {
			this.font_ = font;
			this.font_size_ = 9;

			// initialization of the template
			tpl = writer.getDirectContent().createTemplate(100, 100);
			tpl.setBoundingBox(new Rectangle(-20, -20, 100, 100));
		}

		public void onEndPage(PdfWriter writer, Document document) {
			/**
			 * Reference:
			 * http://itextdocs.lowagie.com/tutorial/directcontent/pageevents/index.php
			 * http://itext.ugent.be/library/com/lowagie/examples/directcontent/pageevents/PageNumbersWatermark.java
			 */
			PdfContentByte cb = writer.getDirectContent();
			cb.saveState();
			// compose the footer
			String page_num_text = "Page " + writer.getPageNumber() + " of ";
			String prepared_by_text = "Prepared By: ";
			try {
				prepared_by_text += UserManager.instance().getUserInfo(
						UserManager.instance().getCurrentUserID()).getDisplayName();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String prepared_on_date = "Prepared On: "
					+ (new Timestamp(Calendar.getInstance().getTime().getTime())).toString();
			float textBase = document.bottom() - 10;
			float text_size = font_.getWidthPoint(page_num_text, font_size_);
			float adjust = font_.getWidthPoint("0", font_size_);
			cb.beginText();
			cb.setFontAndSize(font_, font_size_);
			cb.setTextMatrix(document.right() - text_size - adjust, textBase);
			cb.showText(page_num_text);
			cb.endText();
			cb.addTemplate(tpl, document.right() - adjust, textBase);

			cb.beginText();
			cb.setFontAndSize(font_, font_size_);
			cb.setTextMatrix(document.left(), textBase - 5);
			cb.showText(prepared_by_text);
			cb.endText();

			cb.beginText();
			cb.setFontAndSize(font_, font_size_);
			cb.setTextMatrix(document.left(), textBase + 5);
			cb.showText(prepared_on_date);
			cb.endText();

			cb.saveState();
			// draw a Rectangle around the page
			cb.setColorStroke(Color.RED);
			cb.setLineWidth(1);
			cb.rectangle(20, 20, document.getPageSize().getWidth() - 40, document.getPageSize()
					.getHeight() - 40);
			cb.stroke();
			cb.restoreState();
		}

		public void onCloseDocument(PdfWriter writer, Document document) {
			tpl.beginText();
			tpl.setFontAndSize(font_, font_size_);
			tpl.setTextMatrix(0, 0);
			tpl.showText(Integer.toString(writer.getPageNumber() - 1));
			tpl.endText();
		}
	}
}

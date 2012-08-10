package reman.client.app.finance.statements;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.AcctBalanceSystem;
import reman.client.app.finance.accounts.exceptions.InvalidAmountException;
import reman.client.app.finance.accounts.exceptions.InvalidCategoryException;
import reman.client.app.finance.accounts.exceptions.UnknownNodeException;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.finance.journals.exceptions.InvalidJournalEntryException;
import reman.client.app.trees.CollectionTreeTraverseUtilitiy;
import reman.common.database.ManagedDatabaseObject;
import reman.common.database.UserManager;
import reman.common.database.exceptions.DatabaseException;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Each Statement type differs in the quantity it reflects of the financial engine.  Some statement report relationships between Account balances,
 * and others report on relationships between AcctActionCategory balances from an Account.  Statements provide an "at a glance" view of the
 * financial status of a particular reference point in the financial engine.
 * <br/>A Statement will automatically maintain its final contribution as StatementContribution objects are added and removed.
 * Statement Order of Generation: http://www.QuickMBA.com/accounting/fin/closing-entries/
 * 1) Income Statement
 * 		Expenses (investments, taxes -- realize expenses under accural accounting when they are incurred) & revenues closed to Income Summary
 * 2) Cash Flows
 * 		Taxes Payable, Interest Payable
 * 		Dividends is closed into Net Income
 * 3) Retained Earnings
 * 		Net Income is closed into Retained Earnings
 * 4) Balance Sheet
 * @author Scott
 *
 */
public abstract class Statement extends ManagedDatabaseObject {
	/*can NOT be Limited Commit, StatementManager must commit preceding statement when the next statement is generated*/

	/**
	 * Each section will make a contribution toward this statement as a whole
	 * This are the root level (no parents) contributions.
	 */
	protected ArrayList<StatementContribution> root_contributions_;

	/**
	 * Maintain the balance of this statement
	 */
	protected AcctBalanceSystem statement_balance_system_;

	/**
	 * Used to obtain this statement's begin time
	 */
	private Statement preceding_statement_;

	/**
	 * Used to maintain the chain of statements, and find the most up to date statement for this statement type
	 */
	private Statement succeeding_statement_;

	/**
	 * Begin time stamp as to when this statement applies to
	 */
	private Timestamp begin_time_;

	/**
	 * End time stamp as to when this statement applies to
	 */
	private Timestamp end_time_;

	/**
	 * This will be set by the StatementManager when registered, so to not allow any more contributions or line items to be added
	 */
	private boolean is_finialized_;

	/**
	 * For DatabaseObject use only
	 */
	protected Statement() {
		super(new String[] { "statement_balance_system_", "root_contributions_" });
	}

	/**
	 * 
	 * @param statementName Used to uniquely identify this type of Statement.  Statement chains are built upon unique statement names.
	 * @param statement_normal_balance  The transaction type of the statement calculation amount.
	 */
	public Statement(String statementName, TransactionType statement_normal_balance,
			Timestamp end_time) {
		this();
		super.name_ = statementName;
		this.statement_balance_system_ = new AcctBalanceSystem(statement_normal_balance);
		this.root_contributions_ = new ArrayList<StatementContribution>();
		this.end_time_ = (end_time == null) ? new Timestamp((new Date()).getTime()) : end_time;
		this.is_finialized_ = false;
	}

	/**
	 * Add <code>sc</code> to this Statement.  If <code>sc</code> has a parent this method will maintain the tree structure of StatementContributions.
	 * This statement must not be finalized (registered with statement manager) in order to add contributions.
	 * @param sc Will be incorporated into the tree if it has a parent, otherwise it will be added to the root list.
	 * @return True if <code>sc<code> is a root contribution, and not already in the root list.  True if <code>sc<code> has a parent, and wasn't previously
	 * 				added to it's parent's child list. In all cases the calculate method must return true after the account is removed for this method to return true.
	 * 				False if statement is finalized or otherwise.
	 * @throws SQLException 
	 * @throws DatabaseException 
	 * @throws FinanceException 
	 */
	public boolean addContribution(StatementContribution sc) throws FinanceException,
			DatabaseException, SQLException {
		if (!this.is_finialized_) {
			if (sc.getParent() == null) {
				if (this.root_contributions_.contains(sc))
					return false;
				if (this.root_contributions_.add(sc)) {
					sc.setOwner(this);
					return this.calculate(false);
				}
			} else if (sc.getParent().addChildContribution(sc)) {
				sc.setOwner(this);
				return this.calculate(false);
			}
		}
		return false;
	}

	/**
	 * If this statement is not finalized and the calculate method can be successful after the contribution is removed, true is returned.
	 * @param sc
	 * @return
	 * @throws SQLException 
	 * @throws DatabaseException 
	 * @throws FinanceException 
	 * @throws UnknownNodeException 
	 * @throws InvalidCategoryException 
	 * @throws InvalidJournalEntryException 
	 * @throws InvalidAmountException 
	 */
	public boolean removeContribution(StatementContribution sc) throws InvalidAmountException,
			InvalidJournalEntryException, InvalidCategoryException, UnknownNodeException,
			FinanceException, DatabaseException, SQLException {
		if (!this.is_finialized_) {
			StatementContribution matching_sc = (new CollectionTreeTraverseUtilitiy<StatementContribution>())
					.getTargetNode(this.root_contributions_, sc);
			if (matching_sc != null) {
				StatementContribution parent = matching_sc.getParent();
				if (parent == null) {
					if (this.root_contributions_.remove(matching_sc)) {
						matching_sc.setOwner(null);
						return this.calculate(false);
					}
				} else {
					if (parent.removeChildContribution(matching_sc)) {
						matching_sc.setOwner(null);
						return this.calculate(false);
					}
				}
			}
		}
		return false;
	}

	/**
	 * StatementContribution are maintained in a tree, these are the root (no parent) level StatementContribution.
	 * @return
	 */
	public ArrayList<StatementContribution> getRootContributions() {
		return new ArrayList<StatementContribution>(this.root_contributions_);
	}

	/**
	 * This will invoke each root contribution's calculate() method, and return the final tabulated amount.
	 * @param end_fiscal_year True if this statement is generated at the end of the fiscal year.
	 * @return True if the statement was successfully calculated, false otherwise.
	 * @throws SQLException 
	 * @throws DatabaseException 
	 * @throws FinanceException 
	 */
	boolean calculate(boolean end_fiscal_year) throws FinanceException, DatabaseException,
			SQLException {

		this.statement_balance_system_.zeroAmount();

		for (StatementContribution contribution : this.root_contributions_) {
			this.statement_balance_system_.addAmount(contribution.calculate(end_fiscal_year), true);
		}

		return true;
	}

	/**
	 * Obtain the final amount calculated in this Statement.
	 * @return null if not finalized
	 */
	public AcctBalanceSystem getFinalAmount() {
		//if (this.is_finialized_)
		return this.statement_balance_system_;
		//return null;
	}

	/**
	 * Begining time this Statement pretains to.
	 * @return
	 */
	public Timestamp getBeginTime() {
		return this.begin_time_;
	}

	/**
	 * Ending time this Statement pretains to.
	 * @return
	 */
	public Timestamp getEndTime() {
		return this.end_time_;
	}

	/**
	 * Maintain the Statement chain.  This will allow begin/end dates of successive Statements to be properly aligned.
	 * <br/>Should only be used by StatementManager.
	 * @param statement
	 */
	void setPrecedingStatement(Statement statement) {
		this.preceding_statement_ = statement;
		if (this.preceding_statement_ != null) {
			this.begin_time_ = this.preceding_statement_.getEndTime();
			//super.name_ = this.preceding_statement_.getName();
		}
	}

	/**
	 * Maintain the Statement chain.  This will allow begin/end dates of successive Statements to be properly aligned.
	 * <br/>Should only be used by StatementManager.
	 * @param statement
	 */
	void setSucceedingStatement(Statement statement) {
		this.succeeding_statement_ = statement;
	}

	/**
	 * Obtain a reference to the previous Statement in this Statement's chain.
	 * @return
	 */
	public Statement getPrecedingStatement() {
		return this.preceding_statement_;
	}

	/**
	 * Obtain a reference to the next Statement in this Statement's chain.
	 * @return
	 */
	public Statement getSucceedingStatement() {
		return this.succeeding_statement_;
	}

	/**
	 * When this Statement is registered with a StatementManager this method will allow the final calculated amount to be obtained.
	 * <br/>This should only be used by the StatementManager during registration process.
	 */
	void setFinialized(boolean finalized) {
		this.is_finialized_ = finalized;
	}

	/**
	 * If this statement is finalized, then it will not be possible to add StatementContributions or StatementLineItems to this Statement.
	 * @return
	 */
	public boolean isFinalized() {
		return this.is_finialized_;
	}

	private String getStartDate() {
		if (this.getBeginTime() == null)
			return "";
		return Statement.getDateFormat().format(begin_time_);
	}

	private String getEndDate() {
		if (this.getEndTime() == null)
			return "";
		return Statement.getDateFormat().format(end_time_);
	}

	private String getFileOutputName() {
		return this.getName() + getOutputDate(this.getEndTime());
	}

	private String getOutputDate(Timestamp t) {
		String format_date = DateFormat.getDateInstance(DateFormat.SHORT).format(t);
		return format_date.replace('/', '_');
	}

	public void exportToPdf() throws IOException {
		this.exportToPdf(this.getFileOutputName());
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

		/*statement name*/
		Chunk statement_name = new Chunk(this.name_);
		statement_name.setFont(new Font(Font.HELVETICA, Font.DEFAULTSIZE, Font.BOLD));
		Paragraph statement_name_p = new Paragraph(statement_name);
		statement_name_p.setAlignment(Element.ALIGN_CENTER);

		/*statement date*/
		Chunk statement_dates = new Chunk("From " + this.getStartDate() + " To " + this.getEndDate(),
				new Font(Font.HELVETICA, 10, Font.NORMAL));
		Paragraph statement_dates_p = new Paragraph(statement_dates);
		statement_dates_p.setAlignment(Element.ALIGN_CENTER);
		statement_dates_p.setSpacingAfter(10);

		try {
			Document pdf = new Document();
			PdfWriter writer = PdfWriter.getInstance(pdf, new FileOutputStream(file_name));
			pdf.open();
			writer.setPageEvent(new PdfFormater(writer));

			/*add header info*/
			pdf.add(comp_name_p);
			pdf.add(statement_name_p);
			pdf.add(statement_dates_p);

			/*Incorporate data table*/
			pdf.add(this.getTable());

			pdf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected abstract String[] getColumnNames();

	protected abstract float[] getColumnWidths();

	/**
	 * This will generate a PDF table (used by iText API). Use the helper function <code>getTable(final String[] column_names)</code>
	 * to obtain default functionality.
	 * @return
	 * @throws DocumentException 
	 */
	public PdfPTable getTable() throws DocumentException {
		/*set up title and header row*/
		String[] column_names = this.getColumnNames();
		PdfPTable table = new PdfPTable(column_names.length);
		table.setWidthPercentage(100f);
		table.setWidths(this.getColumnWidths());
		for (String header : column_names) {
			Paragraph h_p = new Paragraph(header);
			h_p.setFont(new Font(Font.COURIER, 12, Font.BOLD));
			h_p.setAlignment(Element.ALIGN_CENTER);
			PdfPCell h_cell = new PdfPCell(h_p);
			h_cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(h_cell);
		}
		table.setHeaderRows(1);

		/*delegate population of table to root contributions*/
		for (StatementContribution root_cont : this.root_contributions_) {
			root_cont.buildTable(table, 0);
		}

		/*set up footer and total resulting amount row*/
		PdfPCell footer_cell = new PdfPCell(new Phrase(this.description_));
		footer_cell.setColspan(table.getNumberOfColumns() - 1);
		table.addCell(footer_cell);
		Paragraph statement_total = new Paragraph(this.statement_balance_system_.toString());
		statement_total.setAlignment(Element.ALIGN_RIGHT);
		PdfPCell s_total = new PdfPCell(statement_total);
		s_total.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(s_total);

		return table;
	}

	public static DateFormat getDateFormat() {
		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
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
					+ Statement.getDateFormat().format((new Timestamp((new Date()).getTime())));
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

package reman.client.app.finance.statements;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.AcctAmount;
import reman.client.app.finance.accounts.AcctBalanceSystem;
import reman.client.app.finance.accounts.exceptions.InvalidAmountException;
import reman.client.app.finance.accounts.exceptions.InvalidCategoryException;
import reman.client.app.finance.accounts.exceptions.UnknownNodeException;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.finance.journals.exceptions.InvalidJournalEntryException;
import reman.client.app.trees.ICollectionTreeNode;
import reman.common.database.ManagedDatabaseObject;
import reman.common.database.exceptions.DatabaseException;

import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * Holds a collection of StatementLineItem objects.  This StatementContribution will offer its calculated amount as the
 * the sum of all contained StatementLineItem objects and the sum of all descendant StatementContribution objects.
 * (Maintained in a tree structure so that parent StatementContribution will depend upon children StatementContribution).
 * @author Scott
 *
 */
public abstract class StatementContribution extends ManagedDatabaseObject implements
		ICollectionTreeNode<StatementContribution> {

	/**
	 * Internally maintained list of children. When a new StatementControbution is created if it has a parent, the new class will append its self the the parent's list of children
	 */
	private ArrayList<StatementContribution> children_;

	private StatementContribution parent_;

	protected AcctBalanceSystem balance_system_;

	protected ArrayList<StatementLineItem> line_items_;

	/**
	 * Statement which this contribution belongs to
	 */
	private Statement owner_;

	/**
	 * For DatabaseObject use only
	 */
	protected StatementContribution() {
		super(new String[] { "balance_system_", "children_", "line_items_" });
	}

	/**
	 * 
	 * @param contribution_name Identification of this StatementContribution
	 * @param result_description Of the final calculated amount of this StatementContribution.
	 * @param normal_contribution Of the final calculated amount of this StatementContribution.
	 * @param parent Used to maintain the StatementContribution tree structure.
	 */
	public StatementContribution(String contribution_name, String result_description,
			TransactionType normal_contribution, StatementContribution parent) {
		super.name_ = contribution_name;
		super.description_ = result_description;
		this.children_ = new ArrayList<StatementContribution>();
		this.line_items_ = new ArrayList<StatementLineItem>();
		this.balance_system_ = new AcctBalanceSystem(normal_contribution);
		this.parent_ = parent;
	}

	/**
	 * Add <code>sc</code> as a child StatementContribution to this StatementContribution.  The StatementContribution must not be finalized.
	 * @param sc
	 * @return
	 */
	boolean addChildContribution(StatementContribution sc) {
		if (this.isFinalized())
			return false;

		if (this.children_.contains(sc))
			return false;
		return this.children_.add(sc);
	}

	/**
	 * 
	 * @param sc
	 * @return True if <code>sc</code> was removed from child collection.  False if StatementContribution is finalized or otherwise.
	 */
	boolean removeChildContribution(StatementContribution sc) {
		if (this.isFinalized())
			return false;
		return this.children_.remove(sc);
	}

	/**
	 * Obtain a collection the direct descendants of this StatementContribution.
	 */
	public Collection<StatementContribution> getChildren() {
		return new ArrayList<StatementContribution>(this.children_);
	}

	/**
	 * Parent StatementContribution in the StatementContribution tree structure.
	 */
	public StatementContribution getParent() {
		return this.parent_;
	}

	/**
	 * Implementing classes will provide specific functionality based on the view point of the financial engine which is represented.
	 * @param end_fiscal_year True if this statement is generated at the end of the fiscal year.
	 * @return Final contribution of this StatementContribution.
	 * @throws InvalidJournalEntryException
	 * @throws InvalidAmountException
	 * @throws InvalidCategoryException
	 * @throws UnknownNodeException
	 * @throws DatabaseException
	 * @throws SQLException
	 */
	abstract AcctAmount calculate(boolean end_fiscal_year) throws InvalidJournalEntryException,
			InvalidAmountException, InvalidCategoryException, UnknownNodeException, DatabaseException,
			SQLException;

	/**
	 * The final calculated amount of this StatementContribution.
	 * @return The final amount of this statement's contribution. Null if not finalized or otherwise.
	 */
	public AcctAmount getContribution() {
		//if (this.isFinalized())
		return this.balance_system_.getAcctAmount();
		//return null;
	}

	/**
	 * Add a <code>line_item</code> to this StatementContribution.  This StatementContribution must not be finalized.
	 * @param line_item
	 * @return True if <code>line_item</code> was added. False if finalized or otherwise.
	 * @throws SQLException 
	 * @throws DatabaseException 
	 * @throws FinanceException 
	 */
	public boolean addLineItem(StatementLineItem line_item) throws FinanceException,
			DatabaseException, SQLException {
		if (!this.isFinalized() && this.line_items_.add(line_item)) {
			line_item.setOwner(this);
			if (this.owner_ != null)
				return this.owner_.calculate(false);
			return true;
		}
		return false;
	}

	/**
	 * Remove a StatementLineItem from this StatementContribution. This StatementContribution must not be finalized.
	 * @param line_item
	 * @return True if StatementLineItem removed. False if Statement containing this StatementContribution is finalized or otherwise.
	 * @throws SQLException 
	 * @throws DatabaseException 
	 * @throws FinanceException 
	 */
	public boolean removeLineItem(StatementLineItem line_item) throws FinanceException,
			DatabaseException, SQLException {
		if (!this.isFinalized() && this.line_items_.remove(line_item)) {
			line_item.setOwner(null);
			if (this.owner_ != null)
				return this.owner_.calculate(false);
			return true;
		}
		return false;
	}

	@Override
	public boolean isLeafNode() {
		return this.children_.size() <= 0;
	}

	/**
	 * Used (by the Statement) when this StatementContribution is added/removed from a Statement.
	 * @param s
	 */
	void setOwner(Statement s) {
		this.owner_ = s;
	}

	/**
	 * This StatementContribution is finalized if the Statement which contains this is finalized.
	 * @return
	 */
	public boolean isFinalized() {
		if (this.owner_ != null)
			return this.owner_.isFinalized();
		return false;
	}

	/**
	 * Provides PDF support through iText API.  This will delegate to child StatementContrbution object before
	 * contributing this objects contained StatementLineItem objects.
	 */
	public void buildTable(PdfPTable table, int child_depth) {
		PdfPCell header = new PdfPCell();
		Paragraph title;
		Font header_font;
		float indent = 0;
		switch (child_depth) {
		case 0:
			header_font = new Font(Font.HELVETICA, 14, Font.BOLD);
			indent = 5;
			break;
		case 1:
			header_font = new Font(Font.HELVETICA, 12, Font.BOLD);
			indent = 10;
			break;
		case 2:
			header_font = new Font(Font.HELVETICA, 12, Font.ITALIC);
			indent = 15;
			break;
		default:
			header_font = new Font(Font.HELVETICA, 10, Font.ITALIC);
			indent = 20;
		}
		title = new Paragraph(this.getName(), header_font);
		header.setColspan(table.getNumberOfColumns());
		header.addElement(title);
		header.setIndent(indent);
		header.setVerticalAlignment(Element.ALIGN_MIDDLE);
		table.addCell(header);

		/*delegate to child contributions*/
		for (StatementContribution child : this.getChildren())
			child.buildTable(table, child_depth + 1);

		/*add this contribution's line items*/
		for (StatementLineItem line_item : this.line_items_)
			line_item.buildTable(table);

		/*add footer and balance*/
		Paragraph descr = new Paragraph(this.description_, header_font);
		descr.setAlignment(Element.ALIGN_RIGHT);
		PdfPCell footer = new PdfPCell();
		footer.setColspan(table.getNumberOfColumns() - 1);
		footer.addElement(descr);
		footer.setIndent(indent);
		footer.setVerticalAlignment(Element.ALIGN_MIDDLE);
		table.addCell(footer);
		Paragraph total = new Paragraph(this.balance_system_.toString(), header_font);
		PdfPCell t_cell = new PdfPCell(total);
		t_cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		t_cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
		table.addCell(t_cell);
	}

	public boolean equals(Object o) {
		if (o instanceof StatementContribution) {
			StatementContribution sc = (StatementContribution) o;
			if (sc.getName().equals(this.name_) && sc.getDescription().equals(this.description_)
					&& sc.getParent() == this.parent_ && sc.balance_system_.equals(this.balance_system_)
					&& sc.getChildren().equals(this.children_))
				return true;
		}
		return false;
	}
}

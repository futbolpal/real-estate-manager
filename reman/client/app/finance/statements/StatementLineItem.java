package reman.client.app.finance.statements;

import java.util.ArrayList;

import reman.client.app.finance.accounts.AcctBalanceSystem;
import reman.common.database.ManagedDatabaseObject;

import com.lowagie.text.pdf.PdfPTable;

/**
 * Just as the StatementContribution provides structure and organization to the Statement; the StatementLineItem provides a concrete amount to
 * the StatementContribution.
 * <br/>In the financial world some StatementLineItem objects are a result of a judgment call (depreciation, inventory,...) and thus StatementNotes can be used
 * to note these decisions as made.
 * @author Scott
 *
 */
public abstract class StatementLineItem extends ManagedDatabaseObject {

	private ArrayList<StatementNote> notes_;

	private StatementContribution owner_;

	/**
	 * DatabaseObject use only
	 */
	protected StatementLineItem() {
		super(new String[] { "notes_" });
	}

	/**
	 * 
	 * @param description Of this StatementLineItem relative to the Statement containing this StatementLineItem.
	 */
	public StatementLineItem(String description) {
		super.description_ = description;
		this.notes_ = new ArrayList<StatementNote>();
	}

	/**
	 * Add a StatementNote to this StatementLineItem.
	 * @param note
	 * @return True if note is added, false if otherwise.
	 */
	public boolean addNote(StatementNote note) {
		return this.notes_.add(note);
	}

	/**
	 * Remove a StatementNote from this StatementLineItem. This StatementLineItem must not be finalized. 
	 * @param note
	 * @return True if note is removed, false if this line item is finalized or otherwise.
	 */
	public boolean removeNote(StatementNote note) {
		if (this.isFinalized())
			return false;
		return this.notes_.remove(note);
	}

	/**
	 * Obtain a collection of notes as a result of this StatementLineItem.
	 * @return
	 */
	public ArrayList<StatementNote> getNotes() {
		return new ArrayList<StatementNote>(this.notes_);
	}

	/**
	 * This StatementLineItem is finalized when the StatementContribution which contains this StatementLineItem is finalized.
	 * @return
	 */
	public boolean isFinalized() {
		if (this.owner_ != null)
			return this.owner_.isFinalized();
		return false;
	}

	/**
	 * Used (by the StatementContribution) when this StatementLineItem is added/removed from a StatementContribution.
	 * @param s
	 */
	void setOwner(StatementContribution sc) {
		this.owner_ = sc;
	}

	/**
	 * This will be called by StatementContribution to contribute rows to the <code>table</code> being built
	 * @param table
	 */
	public abstract void buildTable(PdfPTable table);
}

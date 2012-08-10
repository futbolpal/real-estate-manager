package reman.client.app.finance.journals;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import reman.client.app.finance.accounts.AcctAmount;
import reman.client.app.finance.accounts.exceptions.InvalidAmountException;
import reman.client.app.finance.accounts.exceptions.InvalidCategoryException;
import reman.client.app.finance.equations.exceptions.MathException;
import reman.client.app.finance.templates.TemplateContribution;
import reman.client.app.finance.templates.TemplateLineItem;
import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;

/**
 * This is the specific application of TemplateContribution for JournalEntry.  The JournalEntryTemplateContribution will generate
 * all JournalEntryLineItem objects upon generation of the parent template.
 * @author Scott
 *
 */
public class JournalEntryTemplateContribution extends TemplateContribution {

	/**
	 * DatabaseObject use only
	 */
	private JournalEntryTemplateContribution() {

	}

	/**
	 * 
	 * @param name Identification for this JournalEntryTemplateContribution
	 * @param description Describing the overall contribution of this JournalEntryTemplateContribution
	 */
	public JournalEntryTemplateContribution(String name, String description) {
		super(name, description, null, null);
	}

	/**
	 * Applies this template by generating a journal entry, based off of each JournalEntryTemplateLineItem element contained within.
	 * All variables must be set prior to invoking this method.
	 * @return Null or type JournalEntry
	 * @throws MathException Thrown during evaluation of an Equation contained within a JournalEntryTemplateLineItem.
	 * @throws DatabaseException 
	 * @throws MathException 
	 * @throws InvalidCategoryException 
	 * @throws InvalidAmountException 
	 */
	@Override
	public DatabaseObject generateTemplateContribution() throws InvalidAmountException,
			InvalidCategoryException, MathException, DatabaseException, SQLException {

		JournalEntry je = new JournalEntry(this.getDescription(), new Timestamp((new Date()).getTime()));
		/*enumerate line items in order*/
		Vector<Integer> line_items_keys = new Vector<Integer>(super.getLineItemKeySet());
		Collections.sort(line_items_keys);

		for (Integer curr_line_item_key : line_items_keys) {

			JournalEntryTemplateLineItem curr_line_item = this.getLineItem(curr_line_item_key);

			je.addLineItem((JournalEntryLineItem) curr_line_item.generateTemplateLineItem());
		}
		return je;
	}

	/**
	 * Add a JournalEntryTemplateLineItem to this template.
	 * @param li Must be of type JournalEntryTemplateLineItem.
	 */
	@Override
	public Integer addLineItem(TemplateLineItem li) {
		if (!(li instanceof JournalEntryTemplateLineItem))
			return null;
		return super.addLineItem(li);
	}

	/**
	 * Obtain the corresponding JournalEntryTemplateLineItem to <code>key</code>.
	 */
	@Override
	public JournalEntryTemplateLineItem getLineItem(Integer key) {
		return (JournalEntryTemplateLineItem) super.getLineItem(key);
	}
}

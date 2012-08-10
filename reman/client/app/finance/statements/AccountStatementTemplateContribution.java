package reman.client.app.finance.statements;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Vector;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.equations.exceptions.MathException;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.finance.templates.TemplateContribution;
import reman.client.app.finance.templates.TemplateLineItem;
import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;

/**
 * This class will serve as a place holder to the resulting generated AccountStatementContribution.
 * This provides functionality of AccountStatementContributions beyond what is provided by default, and allows the potential for custom Statements to be built.
 * This class provides tree structure organization identical to that of the AccountStatementContribution and when generated will yield its corresponding AccountStatementContribution.
 * @author Scott
 *
 */
public class AccountStatementTemplateContribution extends TemplateContribution {

	/**
	 * This will be set by this TemplateContribution's template as generation occurs.  This is so
	 * this TemplateContribution's generate() method will be able to generate an AccountStatementContribution
	 * with the corresponding parent in the StatementContribution tree.
	 */
	private AccountStatementContribution template_generated_parent_contribution_;

	/**
	 * This will hold the result of this TemplateContribtion's generate() method
	 */
	private AccountStatementContribution generated_contribution_;

	/**
	 * DatabaseObject use only
	 */
	private AccountStatementTemplateContribution() {

	}

	/**
	 * 
	 * @param name Of the generated AccountStatementContribution.
	 * @param description Of the generated AccountStatementContribution's final contribution.
	 * @param normal_balance Of the generated AccountStatementContribution's final contribution.
	 * @param parent To maintain the tree structure in the containing AccountStatement AccountStatementContribution tree.
	 */
	public AccountStatementTemplateContribution(String name, String description,
			TransactionType normal_balance, AccountStatementTemplateContribution parent) {
		super(name, description, normal_balance, parent);
		this.template_generated_parent_contribution_ = null;
		this.generated_contribution_ = null;
	}

	/**
	 * This will generate the corresponding AccountStatementContribution base off of the line items contained within.
	 * IMPORTANT this object's setGernationParent() must be called prior to this invocation so the resultant StatementContribution tree will be intact.
	 * @return Object of type AccountStatementContribution or null.
	 * @throws SQLException 
	 */
	@Override
	public DatabaseObject generateTemplateContribution() throws MathException, FinanceException,
			DatabaseException, SQLException {

		this.generated_contribution_ = new AccountStatementContribution(this.name_, this.description_,
				this.getNormalBalance(), this.template_generated_parent_contribution_);

		/*enumerate line items in order*/
		Vector<Integer> line_items_keys = new Vector<Integer>(super.getLineItemKeySet());
		Collections.sort(line_items_keys);

		for (Integer curr_line_item_key : line_items_keys) {
			AccountStatementTemplateLineItem curr_line_item = (AccountStatementTemplateLineItem) super
					.getLineItem(curr_line_item_key);
			this.generated_contribution_.addLineItem((StatementLineItem) curr_line_item.generateTemplateLineItem());
		}

		return this.generated_contribution_;
	}

	/**
	 * Add an AccountStatementTemplateLineItem to this AccountStatementTemplateContribution
	 * @param li Must be of type AccountStatementTemplateLineItem.
	 */
	@Override
	public Integer addLineItem(TemplateLineItem li) {
		if (!(li instanceof AccountStatementTemplateLineItem))
			return null;
		return super.addLineItem(li);
	}

	public AccountStatementContribution getGeneratedContribtuion() {
		return this.generated_contribution_;
	}

	/**
	 * Used during template generation to maintain resulting StatementContribution tree structure.
	 * <br/>ONLY TO BE USED BY AccountStatementTemplate during template generation.
	 * @param asc
	 */
	void setGenerationParent(AccountStatementContribution asc) {
		this.template_generated_parent_contribution_ = asc;
	}
}

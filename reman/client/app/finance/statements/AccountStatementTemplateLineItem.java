package reman.client.app.finance.statements;

import java.sql.SQLException;

import reman.client.app.finance.equations.exceptions.MathException;
import reman.client.app.finance.templates.TemplateLineItem;
import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;

/**
 * Generation of AccountStatementTemplateLineItem does not include the use of Equation but instead is a direct result of Account balances, and
 * thus this class directly maintains an AccountStatementLineItem object.
 * @author Scott
 *
 */
public class AccountStatementTemplateLineItem extends TemplateLineItem {

	private AccountStatementLineItem acct_line_item_;

	/**
	 * DatabaseObject use only
	 */
	private AccountStatementTemplateLineItem() {
	}

	/**
	 * Equations are not used for StatementTemplates.  Each Account balance system drives the Statement result.
	 * Equation functionality can be duplicated in AccountStatementTemplate objects by different AccountStatementContributionTemplate objects and varying the normal balance between AccountStatementContributionTemplate.
	 * @param description Of the Account balance toward the generated AccountStatement.
	 * @param line_item Corresponding line item used during AccountStatement generation.
	 */
	public AccountStatementTemplateLineItem(String description, AccountStatementLineItem line_item) {
		super(description, null, null);
		this.acct_line_item_ = line_item;
	}

	/**
	 * 
	 * @return Obtain a reference to the internal AccountStatementLineItem.
	 */
	@Override
	public DatabaseObject generateTemplateLineItem() throws MathException, SQLException,
			DatabaseException {
		return this.acct_line_item_;
	}
}

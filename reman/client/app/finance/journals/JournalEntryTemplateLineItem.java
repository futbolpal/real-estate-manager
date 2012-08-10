package reman.client.app.finance.journals;

import java.sql.SQLException;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctActionCategory;
import reman.client.app.finance.accounts.AcctAmount;
import reman.client.app.finance.equations.Equation;
import reman.client.app.finance.equations.exceptions.MathException;
import reman.client.app.finance.templates.TemplateLineItem;
import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;

/**
 * Represents a line item on a journal entry template.  This class serves to associate a transaction type to the result of the evaluated
 * Equation, so an AcctAmount can be generated to apply to the <code>applied_to_acct_</code>. 
 * This class also listens to its Equation's change events and delegates them to listeners of this class.
 * @author Scott
 *
 */
public class JournalEntryTemplateLineItem extends TemplateLineItem {

	private Account applied_to_acct_;
	private AcctActionCategory category_applied_to_;

	/**
	 * For DatabaseObject use only
	 */
	private JournalEntryTemplateLineItem() {
	}

	/**
	 * 
	 * @param descrption
	 * @param apply_eq_to_acct Account which will have the Equation's evaluated result applied to.
	 * @param normal_balance What transaction type the Equation's evaluated result will be viewed as.
	 * @param eq This is the equation, which will determine the AcctAmount to be applied to <code>apply_eq_to_acct</code>
	 * @param normal_balance How the evaluated expression's value will apply to its corresponding account (when chosen)
	 */
	public JournalEntryTemplateLineItem(String descrption, Equation eq,
			TransactionType normal_balance, Account apply_eq_to_acct,
			AcctActionCategory apply_eq_to_category) {
		super(descrption, eq, normal_balance);
		this.applied_to_acct_ = apply_eq_to_acct;
		this.category_applied_to_ = apply_eq_to_category;
	}

	/**
	 * The Account object which the resulting Equation evaluation Amount will be applied to.
	 * @return
	 */
	public Account getApplyToAccount() {
		return this.applied_to_acct_;
	}

	public void setApplyToAccount(Account acct) {
		this.applied_to_acct_ = acct;
	}

	/**
	 * The AcctActionCategory object which the resulting Equation evaluation Amount will be applied to.
	 * @return
	 */
	public AcctActionCategory getApplyToCategory() {
		return this.category_applied_to_;
	}

	public void setApplyToCategory(AcctActionCategory cat) {
		this.category_applied_to_ = cat;
	}

	/**
	 * This will preserve the equation's structure
	 * @return Object of type JournalEntryLineItem or null if problem occurred.
	 */
	@Override
	public DatabaseObject generateTemplateLineItem() throws MathException, SQLException,
			DatabaseException {
		if (this.getApplyToAccount() == null)
			throw new IllegalArgumentException(
					"No corresponding account found to apply equation result of line item '" + this + "'.");

		AcctAmount amt_to_apply = new AcctAmount(Double.parseDouble(this.eq_.evaluate(true)), this
				.getNormalBalance());

		JournalEntryLineItem jeli = new JournalEntryLineItem(this.getApplyToAccount(), amt_to_apply,
				this.getDescription(), this.getApplyToCategory());

		return jeli;
	}
}

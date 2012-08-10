package reman.client.app.finance.journals;

import java.sql.Timestamp;
import java.util.Hashtable;

import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctActionCategory;
import reman.client.app.finance.templates.TemplateInput;

public class JournalEntryTemplateInput extends TemplateInput {

	private Hashtable<Integer, AcctActionCategory> acct_action_categories_;
	private Hashtable<Integer, Account> accounts_to_line_items_;

	/**
	 * DatabaseObject use only
	 */
	private JournalEntryTemplateInput() {
	}

	/**
	 * 
	 * @param template_description
	 * @param occurred
	 * @param accounts_to_line_items Map key's correspond to which Account is applied to which JournalEntryTemplateLineItem contained within.
	 * 															 ex: accounts_to_line_items entry <1,account_x> maps the equation evaluation for line_items_ with key=1 to account_x
	 * @param line_item_descriptions Descrptions to apply to each journal entry line item
	 * @param acct_action_categories Each category (key is equivalent to line number) is the category that will be associated with the corresponding line item generated.
	 * 					This category must be valid with respect to the same keyed 'accounts_to_line_items' account
	 */
	private JournalEntryTemplateInput(String template_description, Timestamp occurred,
			Hashtable<Integer, String> line_item_descriptions,
			Hashtable<Integer, AcctActionCategory> acct_action_categories,
			Hashtable<Integer, Account> accounts_to_line_items) {
		super(template_description, occurred, line_item_descriptions);
		this.accounts_to_line_items_ = accounts_to_line_items;
		this.acct_action_categories_ = acct_action_categories;
	}

	public Hashtable<Integer, AcctActionCategory> getAcctActionCategories() {
		return this.acct_action_categories_;
	}

	public Hashtable<Integer, Account> getAccountsToLineItems() {
		return this.accounts_to_line_items_;
	}
}

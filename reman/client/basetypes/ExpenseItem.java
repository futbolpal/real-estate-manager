package reman.client.basetypes;

import java.sql.SQLException;
import java.util.ArrayList;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctActionCategory;
import reman.client.app.finance.equations.Equation;
import reman.client.app.finance.equations.exceptions.MathException;
import reman.client.app.finance.journals.JournalEntryLineItem;
import reman.client.app.finance.journals.JournalEntryTemplateLineItem;
import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;

public class ExpenseItem extends DatabaseObject {
	protected static final String PRICE_VAR = "price";
	private String code_;
	private double price_;
	private TimeRange effective_time_;

	protected JournalEntryTemplateLineItem line_item_expense_;
	protected JournalEntryTemplateLineItem line_item_pay_;

	protected ExpenseItem() {
	}

	/**
	 * The amount of <code>price</code> will be applied to <code>expense_acct</code> with normal balance <code>price_normal</code>.
	 * The amount of <code>price</code> will be applied to <code>pay_acct</code> with the normal balance opposite of <code>price_normal</code>.
	 * @param code
	 * @param descripiton
	 * @param price Should be positive for a payable amount
	 * @param price_normal
	 * @param expense_acct
	 * @param expense_cat
	 * @param pay_acct
	 * @param pay_cat
	 * @param when_effective
	 */
	public ExpenseItem(String code, String company_name, String descripiton, double price,
			TransactionType price_normal, Account expense_acct, AcctActionCategory expense_cat,
			Account pay_acct, AcctActionCategory pay_cat, TimeRange when_effective) {
		this.effective_time_ = when_effective;
		this.code_ = code;
		this.name_ = company_name;
		this.description_ = descripiton;
		this.price_ = price;

		try {
			line_item_expense_ = new JournalEntryTemplateLineItem(descripiton, new Equation(PRICE_VAR),
					price_normal, expense_acct, expense_cat);
			line_item_pay_ = new JournalEntryTemplateLineItem(descripiton, new Equation(PRICE_VAR),
					TransactionType.getOpposite(price_normal), pay_acct, pay_cat);

		} catch (MathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setEffectiveTime(TimeRange range) {
		this.effective_time_ = range;
	}

	public TimeRange getEffectiveTime() {
		return this.effective_time_;
	}

	public String getCode() {
		return this.code_;
	}

	public void setCode(String code) {
		this.code_ = code;
	}

	public double getPrice() {
		return this.price_;
	}

	public void setPrice(double price) {
		this.price_ = price;
	}

	public Account getExpenseAccount() {
		return this.line_item_expense_.getApplyToAccount();
	}

	public ArrayList<JournalEntryLineItem> evaluate() {

		ArrayList<JournalEntryLineItem> result = new ArrayList<JournalEntryLineItem>();
		line_item_expense_.getEquation().setVariable(PRICE_VAR, price_);
		line_item_pay_.getEquation().setVariable(PRICE_VAR, price_);

		try {
			JournalEntryLineItem result1 = (JournalEntryLineItem) line_item_expense_
					.generateTemplateLineItem();
			if (result1 != null)
				result.add(result1);

			JournalEntryLineItem result2 = (JournalEntryLineItem) line_item_pay_
					.generateTemplateLineItem();
			if (result2 != null)
				result.add(result2);
		} catch (MathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
}

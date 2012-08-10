package reman.client.basetypes;

import java.util.ArrayList;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctActionCategory;
import reman.client.app.finance.equations.exceptions.MathException;
import reman.client.app.finance.journals.JournalEntryLineItem;

public class DeductionItem extends ExpenseItem {
	private static String LIMIT_VAR = "limit";
	private Double limit_amt_;

	protected DeductionItem() {

	}

	public DeductionItem(String code, String company_name, String descripiton, double price,
			TransactionType price_normal, Account expense_acct, AcctActionCategory expense_cat,
			Account pay_acct, AcctActionCategory pay_cat, TimeRange when_effective) {
		super(code, company_name, descripiton, price, price_normal, expense_acct, expense_cat,
				pay_acct, pay_cat, when_effective);
	}

	public void setLimit(Double limit) {
		try {
			this.limit_amt_ = limit;
			if (limit == null) {
				line_item_expense_.setEquation(PRICE_VAR);
				line_item_pay_.setEquation(PRICE_VAR);
			} else {
				String min_ex = "min(" + PRICE_VAR + "," + LIMIT_VAR + ")";
				line_item_expense_.setEquation(min_ex);
				line_item_pay_.setEquation(min_ex);
			}
		} catch (MathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Double getLimit() {
		return this.limit_amt_;
	}

	public ArrayList<JournalEntryLineItem> evaluate() {
		if (limit_amt_ != null) {
			line_item_expense_.getEquation().setVariable(LIMIT_VAR, this.limit_amt_);
			line_item_pay_.getEquation().setVariable(LIMIT_VAR, this.limit_amt_);
		}

		return super.evaluate();
	}
}

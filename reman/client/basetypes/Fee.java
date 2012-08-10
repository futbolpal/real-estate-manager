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

public class Fee extends DatabaseObject {
	protected final String PRICE_VAR = "price";
	private ArrayList<FeeStep> steps_;
	private TransactionTimeFrame when_charged_;
	private double total_before_fee_;

	private JournalEntryTemplateLineItem fee_item_;

	protected Fee() {
	}

	public Fee(String description, TransactionTimeFrame when_charged, TimeRange when_effective,
			TransactionType result_normal_balance, Account apply_to_acct, AcctActionCategory apply_to_cat) {

		this.when_charged_ = when_charged;
		this.steps_ = new ArrayList<FeeStep>();

		try {
			fee_item_ = new JournalEntryTemplateLineItem(description, new Equation(PRICE_VAR),
					result_normal_balance, apply_to_acct, apply_to_cat);
		} catch (MathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public JournalEntryLineItem evaluate() {
		double amt = 0;
		if (steps_.size() > 0) {
			for (FeeStep step : steps_) {
				amt += step.calculate(this.total_before_fee_);
			}
		}

		fee_item_.getEquation().setVariable(PRICE_VAR, amt);

		JournalEntryLineItem line_item = null;
		try {
			line_item = (JournalEntryLineItem) fee_item_.generateTemplateLineItem();
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
		return line_item;
	}

	public boolean removeStep(FeeStep step) {
		return this.removeStep(step);
	}

	public boolean addStep(FeeStep step) {
		return this.steps_.add(step);
	}

	public TransactionTimeFrame getWhenApplied() {
		return this.when_charged_;
	}

	public void setWhenApplied(TransactionTimeFrame when) {
		this.when_charged_ = when;
	}

	/**
	 * Call this function before evaluating this fee.  If this is a percentile fee, this is the fee amount the percentile will be based off of.
	 * @param total
	 */
	public void setTotal(double total) {
		this.total_before_fee_ = total;
	}
}

package reman.client.app.finance.accounts;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.exceptions.InvalidAmountException;
import reman.common.database.DatabaseObject;

/**
 * General amount for an financial transaction. Normal balance acts as sign of this amount.
 * If normal balance is currently 1,DEBIT and a 2,CREDIT addition is made the result will be 1,CREDIT
 * @author Scott
 *
 */
public class AcctAmount extends DatabaseObject {
	private double amount_;
	private TransactionType trans_type_;

	/**
	 * DatabaseObject use only
	 */
	private AcctAmount() {
	}

	public AcctAmount(double amt, TransactionType trans_type) {
		amount_ = amt;
		trans_type_ = trans_type;
	}

	public AcctAmount(AcctAmount amt) {
		this.amount_ = amt.getAmount();
		this.trans_type_ = amt.getTransactionType();
	}

	public double getAmount() {
		return amount_;
	}

	public TransactionType getTransactionType() {
		return trans_type_;
	}

	/**
	 * Add <code>amt</code> to this AcctAmount.
	 * Will apply the amount if it increases this amount or does not make it less than 0 (not allowed to flip transaction type).
	 * 
	 * @throws InvalidAmountException If transaction results in changing the transaction type of this AcctAmount.
	 * @returns True if the transaction type didn't change as a result of the addition.  False if the transaction type changed as a result.
	 */
	public boolean add(AcctAmount amt) throws InvalidAmountException {
		return this.add(amt, false);
	}

	/**
	 * Add <code>amt</code> to this AcctAmount. <code>amt</code> has the option to allow this amount to be made negative.
	 * @param amt
	 * @param allow_flip_trans_type If this result is allowed to flip the transaction type.
	 * @throws InvalidAmountException 
	 * @returns True if the transaction type didn't change as a result of the addition.  False if the transaction type changed as a result.
	 */
	public boolean add(AcctAmount amt, boolean allow_flip_trans_type) throws InvalidAmountException {

		/*amount will count negative if the transaction types are opposite*/
		double relative_amt = ((amt.getTransactionType() == this.trans_type_) ? amt.getAmount() : -amt
				.getAmount());

		boolean result_negative = this.amount_ + relative_amt < 0;

		if (!allow_flip_trans_type && result_negative) {
			/*param amt can either increase the amount or not make it less than 0*/
			throw new InvalidAmountException(this, "Adding amount'" + amt
					+ "' will result in this amount '" + this + "' to flip transaction type.");
		} else {
			if (result_negative) {
				this.trans_type_ = TransactionType.getOpposite(this.trans_type_);
				this.amount_ = (-relative_amt) - this.amount_;
			} else
				this.amount_ += relative_amt;

			return !result_negative;
		}
	}

	/**
	 * Flips the transaction type of <code>amt</code>, and adds <code>amt</code>.
	 * @param amt
	 * @throws InvalidAmountException
	 * @returns True if the transaction type didn't change as a result of the subtraction.  False if the transaction type changed as a result.
	 */
	public boolean subtract(AcctAmount amt) throws InvalidAmountException {
		return this.subtract(amt, false);
	}

	/**
	 * Flips the transaction type of <code>amt</code>, and adds <code>amt</code> with the option to allow the amount transaction type to change.
	 * @param amt
	 * @param allow_flip_trans_type
	 * @throws InvalidAmountException
	 * @returns True if the transaction type didn't change as a result of the subtraction.  False if the transaction type changed as a result.
	 */
	public boolean subtract(AcctAmount amt, boolean allow_flip_trans_type)
			throws InvalidAmountException {
		AcctAmount sub_amt = new AcctAmount(amt.getAmount(), TransactionType.getOpposite(amt
				.getTransactionType()));
		return this.add(sub_amt, allow_flip_trans_type);
	}

	public String toString() {
		return this.amount_ + "," + this.trans_type_;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof AcctAmount))
			return false;
		AcctAmount amt = (AcctAmount) obj;

		if ((this.amount_ == amt.getAmount())) {
			if (this.amount_ == 0 || this.trans_type_ == amt.getTransactionType())
				return true;
		}
		return false;
	}
}

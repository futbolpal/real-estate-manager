package reman.client.app.finance.accounts;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.exceptions.InvalidAmountException;
import reman.client.app.finance.gui.BalanceSystemFormPanel;
import reman.client.gui.forms.DboFormPanel;
import reman.common.database.DatabaseObject;
import reman.common.database.ManagedDatabaseObject;

/**
 * The balance system will serve to provide reference on an AcctAmount by supplying a normal balance.  This will allow the balance system
 * as negative/positive in reference to the normal balance.
 * @author Scott
 *
 */
public class AcctBalanceSystem extends ManagedDatabaseObject implements
		Comparable<AcctBalanceSystem> {

	private TransactionType normal_balance_;

	private AcctAmount amt_;

	/**
	 * Database use only
	 */
	private AcctBalanceSystem() {
		super(new String[] { "amt_" });
	}

	public AcctBalanceSystem(TransactionType normal_balance) {
		this(normal_balance, new AcctAmount(0, normal_balance));
	}

	public AcctBalanceSystem(TransactionType normal_balance, AcctAmount init_amt) {
		this();
		this.normal_balance_ = normal_balance;
		this.amt_ = init_amt;
	}

	/**
	 * Increase (or decrease if negative) this system's amount by <code>amt</code>, but do not allow this transaction to make the balance
	 * system more negative.
	 * @param amt
	 * @throws InvalidAmountException
	 */
	public void addAmount(double amt) throws InvalidAmountException {
		this.addAmount(amt, false);
	}

	/**
	 * Increase (or decrease if negative) this system's amount by <code>amt</code>, and provide the option to allow this balance system to
	 * be made more negative.
	 * @param amt
	 * @param allow_flip_trans_type Allow this action to make the AcctAmount flip transaction type
	 * @throws InvalidAmountException
	 */
	public void addAmount(double amt, boolean allow_flip_trans_type) throws InvalidAmountException {
		TransactionType amt_trans_type = this.normal_balance_;
		if (amt < 0) {
			amt_trans_type = TransactionType.getOpposite(amt_trans_type);
			amt *= -1;
		} else {
			allow_flip_trans_type = true;/*if amount is positive, it will net increase the amount, allow flip of transaction type*/
		}
		this.amt_.add(new AcctAmount(amt, amt_trans_type), allow_flip_trans_type);
	}

	/**
	 * Decrease (or increase if negative) this system's amount by <code>amt</code>, but do not allow this transaction to make the balance
	 * system more negative.
	 * @param amt
	 * @throws InvalidAmountException
	 */
	public void subtractAmount(double amt) throws InvalidAmountException {
		this.subtractAmount(amt, false);
	}

	/**
	 * Decrease (or increase if negative) this system's amount by <code>amt</code>, and provide the option to allow this balance system to
	 * be made more negative.
	 * @param amt
	 * @param allow_flip_trans_type If this action net decreases the amount and this is true, the AcctAmount will allow a flip transaction type
	 * @throws InvalidAmountException
	 */
	public void subtractAmount(double amt, boolean allow_flip_trans_type)
			throws InvalidAmountException {
		this.addAmount(-amt, allow_flip_trans_type);
	}

	/**
	 * Increase (or decrease if transaction type opposite this system's normal balance) this system's amount by <code>amt</code>, but do not allow this transaction to make the balance
	 * system more negative.
	 * @param amt
	 * @throws InvalidAmountException
	 */
	public void addAmount(AcctAmount amt) throws InvalidAmountException {
		this.addAmount(amt, false);
	}

	/**
	 * Increase (or decrease if transaction type opposite this system's normal balance) this system's amount by <code>amt</code>and provide the option to allow this balance system to
	 * be made more negative.
	 * @param amt
	 * @param allow_flip_trans_type If this action net decreases the amount and this is true, the AcctAmount will allow a flip transaction type
	 * @throws InvalidAmountException 
	 */
	public void addAmount(AcctAmount amt, boolean allow_flip_trans_type)
			throws InvalidAmountException {
		/*if normal balance == trans type and amount > 0 this action will net increase the amount, allow transaction flip in this case*/
		if (amt.getTransactionType() == this.normal_balance_ && amt.getAmount() > 0)
			allow_flip_trans_type = true;
		this.amt_.add(amt, allow_flip_trans_type);
	}

	/**
	 * Decrease (or increase if transaction type opposite this system's normal balance) this system's amount by <code>amt</code>, but do not allow this transaction to make the balance
	 * system more negative.
	 * @param amt
	 * @throws InvalidAmountException 
	 */
	public void subtractAmount(AcctAmount amt) throws InvalidAmountException {
		this.subtractAmount(amt, false);
	}

	/**
	 * Decrease (or increase if transaction type opposite this system's normal balance) this system's amount by <code>amt</code>, and provide the option to allow this balance system to
	 * be made more negative.
	 * @param amt
	 * @param allow_flip_trans_type Allow this action to make the AcctAmount flip transaction type
	 * @throws InvalidAmountException 
	 */
	public void subtractAmount(AcctAmount amt, boolean allow_flip_trans_type)
			throws InvalidAmountException {
		/*if normal balance == trans type and amount < 0 this action will net increase the amount, allow transaction flip in this case*/
		if (amt.getTransactionType() == this.normal_balance_ && amt.getAmount() < 0)
			allow_flip_trans_type = true;
		this.amt_.subtract(amt, allow_flip_trans_type);
	}

	/**
	 * Brings this balance system to 0.
	 * @throws InvalidAmountException
	 */
	public void zeroAmount() {
		try {
			this.amt_.subtract(this.amt_);
		} catch (InvalidAmountException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * If this system's normal balance is not equal to the amount's transaction type, and the amount is not equal to 0.
	 * @return
	 */
	public boolean isNegative() {
		return AcctBalanceSystem.isNegative(this.amt_, this.normal_balance_);
	}

	/**
	 * Get the balance of this system.  Will return negative if the balance is opposite the normal balance.
	 * @return
	 */
	public double getBalance() {
		return AcctBalanceSystem.getAmount(this.amt_, this.normal_balance_);
	}

	/**
	 * 
	 * @param amt
	 * @param normal_bal
	 * @return True if <code>amt</code> is negative with respect to <code>normal_bal</code>
	 */
	public static boolean isNegative(AcctAmount amt, TransactionType normal_bal) {
		return normal_bal != amt.getTransactionType() && amt.getAmount() != 0;
	}

	/**
	 * Obtain the amount of <code>amt</code> with respect to <code>normal_bal</code>
	 * @param amt
	 * @param normal_bal
	 * @return
	 */
	public static double getAmount(AcctAmount amt, TransactionType normal_bal) {
		return (AcctBalanceSystem.isNegative(amt, normal_bal)) ? -amt.getAmount() : amt.getAmount();
	}

	public AcctAmount getAcctAmount() {
		return new AcctAmount(amt_);
	}

	public TransactionType getNormalBalance() {
		return this.normal_balance_;
	}

	public String toString() {
		Double amt = this.getBalance();
		String str_amt = amt.toString();
		return (amt < 0) ? "(" + str_amt + ")" : str_amt;
	}

	public boolean equals(Object o) {
		if (o instanceof AcctBalanceSystem) {
			AcctBalanceSystem bs = (AcctBalanceSystem) o;
			if (bs.getAcctAmount().equals(this.amt_)
					&& bs.getNormalBalance().equals(this.normal_balance_))
				return true;
		}
		return false;
	}

	@Override
	public void setName(String name) {
		//do nothing
		System.err.println("Not allowed to change name of this object.");
	}

	/**
	 * Compares getBalance() output
	 */
	@Override
	public int compareTo(AcctBalanceSystem o) {
		return (int) (this.getBalance() - o.getBalance());
	}

	/*ADDED FOR GUI PURPOSES*/
	public static DboFormPanel<AcctBalanceSystem> getFormPanel(String name, AcctBalanceSystem abs,
			DboFormPanel<? extends DatabaseObject> parent, boolean read_only) throws Exception {
		return new BalanceSystemFormPanel(name, abs, parent, read_only);
	}

	/*private boolean isSetable() {
		try {
			return FinanceManager.instance().isInitilizationPhase();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}*/

	public void setNormalBalance(TransactionType normal_balance) {
		this.normal_balance_ = normal_balance;
	}

	public void setBalance(double amt) {
		this.zeroAmount();
		try {
			this.addAmount(amt, true);
		} catch (InvalidAmountException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*ADDED FOR GUI PURPOSES*/
}

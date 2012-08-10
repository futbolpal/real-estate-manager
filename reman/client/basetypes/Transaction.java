package reman.client.basetypes;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctActionCategory;
import reman.client.app.finance.accounts.AcctAmount;
import reman.client.app.finance.accounts.exceptions.InvalidCategoryException;
import reman.client.app.finance.invoice.AgentCommissionGroup;
import reman.client.app.finance.invoice.AgentCommissionLineItem;
import reman.client.app.finance.invoice.EntryGroup;
import reman.client.app.finance.invoice.EntryLineItem;
import reman.client.app.finance.invoice.Invoice;
import reman.client.app.finance.invoice.PayableGroup;
import reman.client.app.finance.invoice.PayableLineItem;
import reman.client.app.finance.invoice.ReceivableGroup;
import reman.client.app.finance.invoice.ReceivableLineItem;
import reman.client.app.finance.journals.Journal;
import reman.client.app.finance.journals.JournalEntry;
import reman.client.app.finance.journals.JournalEntryLineItem;
import reman.common.database.OfficeProjectManager;
import reman.common.database.exceptions.DatabaseException;

public class Transaction extends RealEstateAction {
	private String transaction_code_;
	private Timestamp agree_date_;
	private ArrayList<BusinessEntity> buyers_;
	private double sale_price_;
	private double total_buy_commission_percent_;
	private double total_sell_commission_percent_;
	private ArrayList<Commission> buyer_commissions_;
	private ArrayList<Commission> seller_commissions_;
	private JournalEntry journal_entry;
	private Listing listing_;

	private ArrayList<Expense> expenses_;
	private Account commission_payable_;
	private AcctActionCategory commission_p_cat_;
	private Account account_receivable_;
	private AcctActionCategory ar_cat_;
	private Account commission_income_;
	private AcctActionCategory commission_i_cat_;
	private Account agent_commission_;
	private AcctActionCategory agent_cat_;
	private Invoice invoice_;
	private EntryGroup e_group_;
	private PayableGroup p_group_;
	private ReceivableGroup r_group_;
	private AgentCommissionGroup a_group_;

	public Transaction(String transaction_code, Property prop, Account account_receivable,
			AcctActionCategory ar_cat, Account commission_income, AcctActionCategory commission_i_cat,
			Account commission_payable, AcctActionCategory commission_p_cat, Account agent_commission,
			AcctActionCategory agent_cat) {
		super(prop);
		this.transaction_code_ = transaction_code;
		this.account_receivable_ = account_receivable;
		this.ar_cat_ = ar_cat;
		this.commission_income_ = commission_income;
		this.commission_i_cat_ = commission_i_cat;
		this.commission_payable_ = commission_payable;
		this.commission_p_cat_ = commission_p_cat;
		this.agent_commission_ = agent_commission;
		this.agent_cat_ = agent_cat;
		this.agree_date_ = new Timestamp(0L);
		this.buyers_ = new ArrayList<BusinessEntity>();
		this.sale_price_ = 0;
		this.total_buy_commission_percent_ = 0;
		this.total_sell_commission_percent_ = 0;
		this.buyer_commissions_ = new ArrayList<Commission>();
		this.seller_commissions_ = new ArrayList<Commission>();
		this.expenses_ = new ArrayList<Expense>();
	}

	/**
	 * Must create and register journal entries generated before invoice can be generated.  If error in journal entries then the invoice
	 * will be null.
	 * @param journal
	 * @param description
	 * @param vendor
	 * @param invoice_code
	 * @param order_code
	 * @param due_date
	 * @return
	 * @throws InvalidCategoryException
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	public Invoice getInvoice(Journal journal, String description, String vendor,
			String invoice_code, String order_code, Timestamp due_date) throws InvalidCategoryException,
			SQLException, DatabaseException {

		this.journal_entry = generateJournalEntry();
		FinanceManager.instance().getJournalManager().addJournalEntry(journal, journal_entry);

		this.invoice_ = new Invoice(description, vendor, invoice_code, order_code, due_date);

		this.invoice_.add(e_group_);
		this.invoice_.add(a_group_);
		this.invoice_.add(p_group_);
		this.invoice_.add(r_group_);

		return this.invoice_;
	}

	public JournalEntry getJournalEntry() {
		return this.journal_entry;
	}

	public boolean addExpense(Expense e) {
		return this.expenses_.add(e);
	}

	public boolean removeExpense(Expense e) {
		return this.expenses_.remove(e);
	}

	public String getTransactionCode() {
		return this.transaction_code_;
	}

	public void setTransactionCode(String code) {
		this.transaction_code_ = code;
	}

	public double getTotalBuyCommissionPercent() {
		return this.total_buy_commission_percent_;
	}

	public void setTotalBuyCommissionPercent(double percent) {
		this.total_buy_commission_percent_ = percent;
	}

	public double getTotalSellCommissionPercent() {
		return this.total_sell_commission_percent_;
	}

	public void setTotalSellCommissionPercent(double percent) {
		this.total_sell_commission_percent_ = percent;
	}

	public void setSalePrice(double price) {
		this.sale_price_ = price;
	}

	public double getSalePrice() {
		return this.sale_price_;
	}

	public boolean addBuyerCommission(Commission c) {
		return this.buyer_commissions_.add(c);
	}

	public boolean removeBuyerCommission(Commission c) {
		return this.buyer_commissions_.remove(c);
	}

	public ArrayList<Commission> getBuyerCommission() {
		return new ArrayList<Commission>(this.buyer_commissions_);
	}

	public boolean addSellerCommission(Commission c) {
		return this.seller_commissions_.add(c);
	}

	public boolean removeSellerCommission(Commission c) {
		return this.seller_commissions_.remove(c);
	}

	public ArrayList<Commission> getSellerCommission() {
		return new ArrayList<Commission>(this.seller_commissions_);
	}

	/**
	 * 
	 * @return
	 * @throws InvalidCategoryException
	 * @throws SQLException
	 * @throws DatabaseException
	 */
	private JournalEntry generateJournalEntry() throws InvalidCategoryException, SQLException,
			DatabaseException {
		/*debit ar, credit commission income, debit agent commission, credit commission payable
		 *  Commission payable (what office pays to agent after fees and split costs go to office)
		Agent commission (total before split and fees earned by agent)
		AR (total before split and fees)
		Commission Income (total before splits...to counter AR)*/
		Timestamp occurred = new Timestamp((new Date()).getTime());
		/*prepare invoice groups*/
		this.e_group_ = new EntryGroup("Ledger Entries");
		this.p_group_ = new PayableGroup("Accounts Payable");
		this.a_group_ = new AgentCommissionGroup("Agent Commission Payable");
		this.r_group_ = new ReceivableGroup("Accounts Receivable");

		JournalEntry entry = new JournalEntry("Transaction for " + this.getProperty(), occurred);

		for (Expense e : expenses_) {
			if (e.getEffectiveTime().isActive()) {
				for (ExpenseItem e_item : e.getItems()) {
					if (e_item.getEffectiveTime().isActive()) {
						this.p_group_.add(new PayableLineItem(e_item.getCode(), e_item.getName(),
								this.transaction_code_, e_item.getExpenseAccount().getAcctId(), e_item.getPrice()));
						for (JournalEntryLineItem item : e.evaluate()) {
							entry.addLineItem(item);
						}
					}
				}
			}
		}
		TransCalcAmts amts = new TransCalcAmts();
		amts.setTotalBuyerCommission(this.total_buy_commission_percent_);
		amts.setTotalSellerCommission(this.total_sell_commission_percent_);

		for (Commission com : buyer_commissions_) {
			if (OfficeProjectManager.instance().getCurrentProject().isMember(com.getAgent())) {
				entry.addLineItems(this.getJournalEntry(com, true, amts));
			}
		}

		for (Commission com : seller_commissions_) {
			if (OfficeProjectManager.instance().getCurrentProject().isMember(com.getAgent())) {
				entry.addLineItems(this.getJournalEntry(com, false, amts));
			}
		}

		this.r_group_.add(new ReceivableLineItem(transaction_code_, this.getProperty().getLocation().getShortAddress(), this.account_receivable_.getAcctId(), amts.getTotalOfficeEarnings()));

		entry.addLineItem(new JournalEntryLineItem(this.account_receivable_, new AcctAmount(amts
				.getTotalOfficeEarnings(), this.account_receivable_.getBalanceSystem().getNormalBalance()),
				"Office inflow", this.ar_cat_));
		entry.addLineItem(new JournalEntryLineItem(this.commission_income_, new AcctAmount(amts
				.getTotalOfficeEarnings(), this.commission_income_.getBalanceSystem().getNormalBalance()),
				"", this.commission_i_cat_));

		for (JournalEntryLineItem item : entry.getLineItems().values()) {
			e_group_.add(new EntryLineItem(item));
		}

		return entry;
	}

	private ArrayList<JournalEntryLineItem> getJournalEntry(Commission c, boolean isBuyerCalc,
			TransCalcAmts amts) throws InvalidCategoryException, SQLException, DatabaseException {

		ArrayList<JournalEntryLineItem> items = new ArrayList<JournalEntryLineItem>();
		double percent_recieved = 0;

		if (amts.getCommissionRemaining(isBuyerCalc) > c.getPercent())
			percent_recieved = c.getPercent();
		else
			percent_recieved = amts.getCommissionRemaining(isBuyerCalc);

		amts.incCommissionUsed(isBuyerCalc, -percent_recieved);

		double total_before_fees = percent_recieved * this.getSalePrice();
		total_before_fees += c.getFlatAmount();

		JournalEntryLineItem agent_commission = new JournalEntryLineItem(agent_commission_,
				new AcctAmount(total_before_fees, agent_commission_.getBalanceSystem().getNormalBalance()),
				"Commission earned by " + c.getAgent().getName(), this.agent_cat_);
		items.add(agent_commission);

		amts.incTotalOfficeEarnings(total_before_fees);

		double total_fee_amt = 0;
		for (Fee f : c.getAgent().getFees()) {
			f.setTotal(total_before_fees);
			/*figure out how much is left for this agent*/
			JournalEntryLineItem item = f.evaluate();
			total_fee_amt += Math.abs(item.getValue());
			items.add(item);
		}

		double amt_agent_commission = total_before_fees - total_fee_amt;

		JournalEntryLineItem agent_commission_payable = new JournalEntryLineItem(commission_payable_,
				new AcctAmount(amt_agent_commission, TransactionType.CREDIT), "Commission payable to "
						+ c.getAgent().getName(), this.commission_p_cat_);
		items.add(agent_commission_payable);

		/*build invoice for agent commission payable*/
		this.a_group_.add(new AgentCommissionLineItem(c.getAgent(), this.transaction_code_, this
				.getProperty().getLocation().getShortAddress(), amt_agent_commission, commission_payable_
				.getAcctId()));

		return items;
	}

	private class TransCalcAmts {

		private double total_office_earnings_;
		private double buyer_commission_total_;
		private double buyer_commission_used_;
		private double seller_commission_total_;
		private double seller_commission_used_;

		public TransCalcAmts() {
			total_office_earnings_ = 0;
			buyer_commission_total_ = 0;
			buyer_commission_used_ = 0;
			seller_commission_total_ = 0;
			seller_commission_used_ = 0;
		}

		public void setTotalOfficeEarnings(double earnings) {
			this.total_office_earnings_ = earnings;
		}

		public double getTotalOfficeEarnings() {
			return this.total_office_earnings_;
		}

		public void setTotalBuyerCommission(double total) {
			this.buyer_commission_total_ = total;
		}

		public double getTotalBuyerCommission() {
			return this.buyer_commission_total_;
		}

		public void setUsedBuyerCommission(double used) {
			this.buyer_commission_used_ = used;
		}

		public double getUsedBuyerCommission() {
			return this.buyer_commission_used_;
		}

		public void setTotalSellerCommission(double total) {
			this.seller_commission_total_ = total;
		}

		public double getTotalSellerCommission() {
			return this.seller_commission_total_;
		}

		public void setUsedSellerCommission(double used) {
			this.seller_commission_used_ = used;
		}

		public double getUsedSellerCommission() {
			return this.seller_commission_used_;
		}

		public double getRemainingBuyerCommission() {
			return this.buyer_commission_total_ - this.buyer_commission_used_;
		}

		public double getRemainingSellerCommission() {
			return this.seller_commission_total_ - this.seller_commission_used_;
		}

		public void incBuyerCommissionUsed(double amt) {
			this.buyer_commission_used_ += amt;
		}

		public void incTotalOfficeEarnings(double amt) {
			this.total_office_earnings_ += amt;
		}

		public void incSellerCommissionUsed(double amt) {
			this.seller_commission_used_ += amt;
		}

		public void incCommissionUsed(boolean forBuyer, double amt) {
			if (forBuyer)
				this.incBuyerCommissionUsed(amt);
			else
				this.incSellerCommissionUsed(amt);
		}

		public double getCommissionRemaining(boolean forBuyer) {
			if (forBuyer)
				return this.getRemainingBuyerCommission();
			else
				return this.getRemainingSellerCommission();
		}
	}
}

package reman.client.app.finance.statements;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.AcctAmount;
import reman.client.app.finance.accounts.AcctBalanceSystem;
import reman.client.app.finance.accounts.CashCategory;
import reman.client.app.finance.accounts.exceptions.InvalidAmountException;
import reman.client.app.finance.exceptions.FinanceException;
import reman.common.database.exceptions.DatabaseException;

import com.lowagie.text.pdf.PdfPTable;

public class CashFlowStatement extends Statement {

	private CashFlowStatement() {
	}

	public CashFlowStatement(AcctBalanceSystem net_income, BalanceSheet previous_bs,
			BalanceSheet current_bs, Timestamp end_time) throws DatabaseException, SQLException {
		super("Cash Flow Statement", TransactionType.DEBIT, end_time);

		BalanceSystemContribution op = new BalanceSystemContribution("Cash Provided by Operations",
				"Net Operations Cash", TransactionType.DEBIT, null);

		BalanceSystemContribution cwc = new BalanceSystemContribution("Change In Working Capital",
				"Net Change In Working Capital", TransactionType.DEBIT, op);

		BalanceSystemContribution finance = new BalanceSystemContribution("Financing Activities",
				"Net Financing Activities", TransactionType.DEBIT, null);

		BalanceSystemContribution investing = new BalanceSystemContribution("Investing Activities",
				"Net Investing Activities", TransactionType.DEBIT, null);

		/*BalanceSystemContribution cash = new BalanceSystemContribution("Cash Represented By",
				"Net Cash", TransactionType.DEBIT, null);*/

		this.buildContribution(cwc, previous_bs, current_bs, CashCategory.WORKING_CAPITAL);
		op.addLineItem(new CashFlowLineItem("Net Income", net_income, true));
		this.buildContribution(op, previous_bs, current_bs, CashCategory.DEPRECIATION);

		this.buildContribution(finance, previous_bs, current_bs, CashCategory.FINANCING);
		this.buildContribution(investing, previous_bs, current_bs, CashCategory.INVESTMENT);

		super.addContribution(op);
		super.addContribution(cwc);
		super.addContribution(finance);
		super.addContribution(investing);

	}

	private void buildContribution(BalanceSystemContribution contr, BalanceSheet previous,
			BalanceSheet current, CashCategory cash) throws FinanceException, DatabaseException,
			SQLException {
		Collection<AccountStatementLineItem> current_lines = current.getLineItems(cash);
		Collection<AccountStatementLineItem> previous_lines = new ArrayList<AccountStatementLineItem>();
		if (previous != null)
			previous_lines = previous.getLineItems(cash);

		boolean directly_related = (cash == CashCategory.DEPRECIATION || cash == CashCategory.FINANCING || cash == CashCategory.CASH) ? true
				: false;
		boolean total_amt = cash == CashCategory.DEPRECIATION;

		for (AccountStatementLineItem current_line : current_lines) {
			BalanceSystemLineItem new_line = null;
			for (AccountStatementLineItem previous_line : previous_lines) {
				if (current_line.getAccount() == previous_line.getAccount()) {
					new_line = this.buildLineItem(previous_line, current_line, directly_related, total_amt);
					break;
				}
			}
			if (new_line == null) {
				new_line = this.buildLineItem(null, current_line, directly_related, total_amt);
			}
			contr.addLineItem(new_line);
		}
	}

	private CashFlowLineItem buildLineItem(BalanceSystemLineItem previous,
			AccountStatementLineItem current, boolean directly_related, boolean total_amt) {
		/*if there is no previous line item...make a "0" entry so that information can still be generation*/
		if (previous == null) {
			previous = new BalanceSystemLineItem("dummy", new AcctBalanceSystem(current
					.getBalanceSystem().getNormalBalance()));
		}

		CashFlowLineItem line_item = null;
		try {
			TransactionType regular_normal_balance = previous.getBalanceSystem().getNormalBalance();

			AcctBalanceSystem use_balance = null;
			AcctBalanceSystem difference_balance = new AcctBalanceSystem(regular_normal_balance, current
					.getBalanceSystem().getAcctAmount());
			difference_balance.subtractAmount(previous.getBalanceSystem().getAcctAmount(), true);
			boolean increased = current.getBalanceSystem().compareTo(previous.getBalanceSystem()) > 0;
			int multiply_factor = (directly_related) ? 1 : -1;
			/*TransactionType use_balance_trans = ((directly_related && increased) || (!directly_related && !increased)) ? TransactionType.CREDIT
					: TransactionType.DEBIT;*/
			use_balance = new AcctBalanceSystem(this.statement_balance_system_.getNormalBalance(),
					new AcctAmount(multiply_factor * difference_balance.getAcctAmount().getAmount(),
							this.statement_balance_system_.getNormalBalance()));

			line_item = new CashFlowLineItem(current.getAccount().toString(), use_balance, total_amt);

		} catch (InvalidAmountException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return line_item;
	}

	/**
	 * This method will not add a contribution, the contributions are automatically generated based on CashFlowStatement properties
	 * upon instantiation.
	 */
	@Override
	public boolean addContribution(StatementContribution sc) {
		return false;
	}

	@Override
	protected float[] getColumnWidths() {
		final float[] column_widths = { 50f, 25f, 25f };
		return column_widths;
	}

	@Override
	protected String[] getColumnNames() {
		final String[] column_names = { "Activity", "Item Amount", "Totals" };
		return column_names;
	}
}

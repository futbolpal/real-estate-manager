package reman.client.app.finance.statements;

import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctBalanceSystem;

import com.lowagie.text.pdf.PdfPTable;

/**
 * The basic element of an AccountStatement.  Each AccountStatementLineItem contains an Account whose balance will be used in AccountStatement calculation.
 * @author Scott
 *
 */
public class AccountStatementLineItem extends BalanceSystemLineItem {

	private Account acct_;

	/**
	 * DatabaseObject use only.
	 */
	private AccountStatementLineItem() {

	}

	/**
	 * 
	 * @param description What <code>account</code> means in the containing AccountStatement.
	 * @param account Account whose balance will be used in AccountStatement calculation.
	 */
	public AccountStatementLineItem(String description, Account account) {
		super(description, account.getBalanceSystem());
		this.acct_ = account;
	}

	/**
	 * Obtain the Account contained in this AccountStatementLineItem
	 * @return
	 */
	public Account getAccount() {
		return this.acct_;
	}

	/**
	 * ID | NAME | DESCRIPTION | BALANCE
	 */
	@Override
	public void buildTable(PdfPTable table) {
		table.addCell(Integer.toString(acct_.getAcctId()));
		table.addCell(acct_.getName());
		table.addCell(this.description_);
		table.addCell(acct_.getBalanceSystem().toString());
	}
}

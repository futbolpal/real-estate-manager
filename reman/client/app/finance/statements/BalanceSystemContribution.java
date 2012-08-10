package reman.client.app.finance.statements;

import java.sql.SQLException;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.AcctAmount;
import reman.client.app.finance.accounts.exceptions.InvalidAmountException;
import reman.client.app.finance.accounts.exceptions.InvalidCategoryException;
import reman.client.app.finance.accounts.exceptions.UnknownNodeException;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.finance.journals.exceptions.InvalidJournalEntryException;
import reman.common.database.exceptions.DatabaseException;

public class BalanceSystemContribution extends StatementContribution {

	protected BalanceSystemContribution() {

	}

	public BalanceSystemContribution(String name, String description, TransactionType normal_balance,
			BalanceSystemContribution parent) {
		super(name, description, normal_balance, parent);
	}

	@Override
	AcctAmount calculate(boolean end_fiscal_year) throws InvalidJournalEntryException,
			InvalidAmountException, InvalidCategoryException, UnknownNodeException, DatabaseException,
			SQLException {
		this.balance_system_.zeroAmount();

		/*account for each child contribution*/
		for (StatementContribution controbution : this.getChildren()) {
			this.balance_system_.addAmount(controbution.calculate(end_fiscal_year), true);
		}

		/*take all accounts within this contribution into account*/
		for (StatementLineItem line_item : this.line_items_) {
			BalanceSystemLineItem item = (BalanceSystemLineItem) line_item;
			this.balance_system_.addAmount(item.getBalanceSystem().getAcctAmount(), true);
		}

		return this.balance_system_.getAcctAmount();
	}

	@Override
	public boolean addLineItem(StatementLineItem line_item) throws FinanceException,
			DatabaseException, SQLException {
		if (!(line_item instanceof BalanceSystemLineItem))
			return false;
		return super.addLineItem(line_item);
	}
}

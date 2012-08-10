package reman.client.app.finance.statements;

import java.sql.SQLException;
import java.sql.Timestamp;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.AcctTimeScale;
import reman.client.app.finance.accounts.AcctType;
import reman.client.app.finance.exceptions.FinanceException;
import reman.common.database.exceptions.DatabaseException;

import com.lowagie.text.pdf.PdfPTable;

/**
 * AccountStatement will contain a tree structure of AccountStatementContribution objects, each AccountStatementContributions has a normal balance which to view its final tabulated result
 * and thus provide reference toward its contribution in this AccountStatement.
 * 
 * @author Scott
 *
 */
public class AccountStatement extends Statement {
	/**
	 * For DatabaseObject use only.
	 */
	protected AccountStatement() {
	}

	/**
	 * 
	 * @param statementName Uniquely identifies statement types.
	 * @param statement_normal_balance Final normal balance of this statement's overall tabulated amount.
	 */
	public AccountStatement(String statementName, TransactionType statement_normal_balance,
			Timestamp end_time) {
		super(statementName, statement_normal_balance, end_time);
	}

	/**
	 * Incorporate <code>section</code> as part of this AccountStatement.
	 * If <code>section</code> has no parent it is considered a root section.  If a section has parents associated, then it will be inserted accordingly into the StatementContribution tree of this Statement.
	 * and doesn't need to be added.
	 * @param section Must have no parent to be added at root level. Must be of type AccountStatementContribution
	 * @return
	 * @throws SQLException 
	 * @throws DatabaseException 
	 * @throws FinanceException 
	 */
	@Override
	public boolean addContribution(StatementContribution section) throws FinanceException,
			DatabaseException, SQLException {
		if (!(section instanceof AccountStatementContribution))
			return false;
		return super.addContribution(section);
	}

	@Override
	protected float[] getColumnWidths() {
		final float[] column_widths = { 10f, 35f, 40f, 15f };
		return column_widths;
	}

	@Override
	protected String[] getColumnNames() {
		final String[] column_names = { "Acct #", "Acct Name", "Description", "Balance" };
		return column_names;
	}
}

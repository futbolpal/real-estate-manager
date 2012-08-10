package reman.client.app.finance.statements;

import java.sql.SQLException;

import reman.client.app.finance.accounts.AcctActionCategory;
import reman.client.app.finance.exceptions.FinanceException;
import reman.common.database.exceptions.DatabaseException;

/**
 * Each CategoryStatementContribution related one to one with an AcctActionCategory.  The CategoryStatementContribution will contain no StatementLineItem objects
 * as each CategoryStatementContribution directly represents the AcctAmount of the AcctActionCategory contained within.
 * @author Scott
 *
 */
public class CategoryStatementContribution extends BalanceSystemContribution {

	/**
	 * DatabaseObject use only
	 */
	private CategoryStatementContribution() {
	}

	/**
	 * 
	 * @param category The AcctActionCategory for which this CategoryStatementContribution's calculation amount derives from.
	 * @param parent To maintain the StatementContribution tree structure.
	 */
	public CategoryStatementContribution(AcctActionCategory category,
			CategoryStatementContribution parent) {
		super(category.getName(), "Net contribution from " + category.getName(), category
				.getBalanceSystem().getNormalBalance(), parent);
		try {
			super.addLineItem(new CategoryStatementLineItem(category));
		} catch (FinanceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return True if <code>line_item</code> was added to this CategoryStatementContribution
	 * @throws SQLException 
	 * @throws DatabaseException 
	 * @throws FinanceException 
	 */
	public boolean addLineItem(StatementLineItem line_item) throws FinanceException,
			DatabaseException, SQLException {
		if (line_item instanceof CategoryStatementLineItem) {
			return super.addLineItem(line_item);
		}
		return false;
	}
}

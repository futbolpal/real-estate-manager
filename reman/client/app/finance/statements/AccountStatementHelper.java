package reman.client.app.finance.statements;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctTimeScale;
import reman.client.app.finance.exceptions.FinanceException;
import reman.common.database.exceptions.DatabaseException;

public class AccountStatementHelper {
	/**
	 * Each Account will have a line item. If the Account has children, its child Accounts line items will be represented in a child contribution.
	 * @param root_accts
	 * @param root_contribution
	 * @param t_scale Accounts will only be considered which have this time scale.  If passed as 'null' all time scales are considered
	 * @throws SQLException 
	 * @throws DatabaseException 
	 * @throws FinanceException 
	 */
	public static void buildRootContribution(Collection<Account> root_accts,
			StatementContribution root_contribution, AcctTimeScale t_scale) throws FinanceException,
			DatabaseException, SQLException {
		LinkedList<Account> acct_q = new LinkedList<Account>();
		LinkedList<StatementContribution> contribution_q = new LinkedList<StatementContribution>();
		LinkedList<Integer> expected_accts_q = new LinkedList<Integer>();/*parallel collection to the contribution_q, how many line items each contribution can hold*/
		acct_q.addAll(root_accts);
		contribution_q.add(root_contribution);
		expected_accts_q.add(Integer.MAX_VALUE);/*the root level contribution can except any number of line items*/

		while (acct_q.size() > 0) {
			Account curr_acct = acct_q.poll();
			StatementContribution curr_contribution = contribution_q.peek();

			if (t_scale != null && curr_acct.getTimeScale() != t_scale)
				continue;

			StatementLineItem line_item = null;
			if (curr_contribution instanceof AccountStatementContribution)
				line_item = new AccountStatementLineItem("", curr_acct);
			else
				line_item = new BalanceSystemLineItem(curr_acct.toString(), curr_acct.getBalanceSystem());
			curr_contribution.addLineItem(line_item);

			/*after a line item is added, decrement the expected amount of line items*/
			Integer curr_expected_amt = expected_accts_q.peek();
			curr_expected_amt--;
			if (curr_expected_amt <= 0 && contribution_q.size() > 1) {
				expected_accts_q.remove();
				contribution_q.remove();
			}

			if (!curr_acct.isLeafNode()) {
				/*this node has children. the child account line items belong in a child contribution*/
				StatementContribution child_contribution = null;
				if (root_contribution instanceof AccountStatementContribution) {
					child_contribution = new AccountStatementContribution(
							curr_acct.getName() + " Account(s)", "Total " + curr_acct.getName() + " Account(s)",
							curr_acct.getBalanceSystem().getNormalBalance(),
							(AccountStatementContribution) curr_contribution);
				} else {//if (root_contribution instanceof BalanceSystemContribution) {
					child_contribution = new BalanceSystemContribution(curr_acct.getName(), "", curr_acct
							.getBalanceSystem().getNormalBalance(), (BalanceSystemContribution) curr_contribution);
				}

				curr_contribution.addChildContribution(child_contribution);

				/*add contribution and expected_amt to front of list to deal with immediately (next loop iteration)*/
				contribution_q.addFirst(child_contribution);
				expected_accts_q.addFirst(curr_acct.getChildren().size());/*the expected number of line items for the new child contribution is #children*/

				/*add each child account to the front of the list*/
				for (Account child : curr_acct.getChildren().values()) {
					acct_q.addFirst(child);
				}
			}
		}
	}
}

package reman.client.app.finance.statements;

import java.sql.SQLException;
import java.sql.Timestamp;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.equations.exceptions.MathException;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.finance.templates.Template;
import reman.client.app.finance.templates.TemplateContribution;
import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;

/**
 * Used to custom build commonly occurring Statements.  This provides functionality and ability to generate Statements other than the default provided.
 * The generation of this Statement will create a AccountStatement, where Account balances are taken at the time of generation.
 * @author Scott
 *
 */
public class AccountStatementTemplate extends Template {

	private TransactionType statement_normal_balance_;

	private Timestamp end_time_;

	/**
	 * DatabaseObject use only
	 */
	private AccountStatementTemplate() {
	}

	/**
	 * 
	 * @param template_name Identification of this template.
	 * @param template_description What this template's generated Statement represents
	 * @param statement_normal_balance The transaction type of the final calculated amount of the generated AccountStatment.
	 * @param end_time The end time period which the generated template pretains to
	 */
	public AccountStatementTemplate(String template_name, String template_description,
			TransactionType statement_normal_balance, Timestamp end_time) {
		super(template_name, template_description);
		this.statement_normal_balance_ = statement_normal_balance;
	}

	/**
	 * Produce an AccountStatement with Account balances as is at time of invocation. 
	 * @param TemplateContributionInput must be of type StatementTemplateInput
	 * @return AccountStatement object upon success or null if failure.
	 * @throws DatabaseException 
	 * @throws SQLException 
	 */
	@Override
	public DatabaseObject generateTemplate() throws MathException, FinanceException,
			DatabaseException, SQLException {

		AccountStatement as = new AccountStatement(this.name_, this.statement_normal_balance_,
				this.end_time_);

		for (TemplateContribution tc : super.getRootContributions()) {
			AccountStatementTemplateContribution root_template_node = (AccountStatementTemplateContribution) tc;
			/*parent must be set before generation*/
			root_template_node.setGenerationParent(null);
			AccountStatementContribution root_contribution = (AccountStatementContribution) root_template_node
					.generateTemplateContribution();
			as.addContribution(root_contribution);
			generateContributionSubTree(as, root_template_node);
		}

		return as;
	}

	/**
	 * This will be called after each root level node has been generated, to populate the tree from the top down.
	 * @param as AccountStatment to associate generated contributions with
	 * @param root_node The generation parent must be set.
	 * @throws DatabaseException 
	 * @throws MathException 
	 * @throws FinanceException 
	 * @throws SQLException 
	 */
	private void generateContributionSubTree(AccountStatement as,
			AccountStatementTemplateContribution root_node) throws MathException, FinanceException,
			DatabaseException, SQLException {
		for (TemplateContribution child_node : root_node.getChildren()) {
			AccountStatementTemplateContribution astc_child = (AccountStatementTemplateContribution) child_node;

			/*the child's parent will be the result of the root_node's generation*/
			astc_child.setGenerationParent(root_node.getGeneratedContribtuion());
			AccountStatementContribution asc = (AccountStatementContribution) astc_child
					.generateTemplateContribution();

			as.addContribution(asc);
			/*now that this node is generated, its children can obtain it as a parent and can be generated*/
			this.generateContributionSubTree(as, astc_child);
		}
	}

	/**
	 * Add an AccountStatementTemplateContribution to this AccountStatementTemplate
	 * @param tc Must be of type AccountStatementTemplateContribution.
	 */
	@Override
	public boolean addContribution(TemplateContribution tc) {
		if (!(tc instanceof AccountStatementTemplateContribution))
			return false;
		return super.addContribution(tc);
	}
}

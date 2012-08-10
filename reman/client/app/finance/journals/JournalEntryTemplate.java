package reman.client.app.finance.journals;

import java.sql.SQLException;

import reman.client.app.finance.equations.exceptions.MathException;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.finance.templates.Template;
import reman.client.app.finance.templates.TemplateContribution;
import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;

/**
 * Similar to excel this class will control computations relative to any amount which can be controlled by an Equation.
 * This template can be used to transform a set of equations and relation variables into a JournalEntry.  This class
 * is useful for recurring JournalEntry objects which can be regulated by a set of relational Equation objects utilizing variables and functions.
 * 
 * @author Scott
 *
 */
public class JournalEntryTemplate extends Template {

	/**
	 * For DatabaseObject use only.
	 */
	private JournalEntryTemplate() {
	}

	/**
	 * A template will be identified by <code>name</code>.
	 * @param name
	 * @param description
	 */
	public JournalEntryTemplate(String name, String description) {
		super(name, description);
	}

	/**
	 * This JournalEntryTemplate can only hold one TemplateContribution; multiple TemplateContributions do not apply for
	 * JournalEntries (as it does for Statements).
	 * @param tc Must be of type JournalEntryTemplateContribution
	 * @return True if <code>tc</code> was added.
	 */
	@Override
	public boolean addContribution(TemplateContribution tc){
		if(!(tc instanceof JournalEntryTemplateContribution))
			return false;
		if(super.getRootContributions().size() > 0)
			return false;
		else return super.addContribution(tc);
	}
	
	/**
	 * This will return either null, or the only TemplateControbution contained within this JournalEntryTemplate.
	 * @return The only JournalEntryTemplateContribution in this JournalEntryTemplate or null.
	 */
	public JournalEntryTemplateContribution getContribution(){
		if(this.getRootContributions().size()<=0)
			return null;
		return (JournalEntryTemplateContribution)super.getRootContributions().get(0);
	}
	
	/**
	 * Because this JournalEntryTemplate only consists of a single TemplateContribution, the generation is delegated to that TemplateContribution.
	 * @return Object of type JournalEntry or null.
	 */
	@Override
	public DatabaseObject generateTemplate()
			throws MathException, FinanceException, DatabaseException, SQLException {
		
		JournalEntryTemplateContribution je_tc = this.getContribution();
		if(je_tc == null)
			return null;
		return je_tc.generateTemplateContribution();
	}
}

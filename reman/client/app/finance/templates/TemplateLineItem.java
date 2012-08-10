package reman.client.app.finance.templates;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.equations.Equation;
import reman.client.app.finance.equations.events.EquationChangeEvent;
import reman.client.app.finance.equations.events.EquationChangeListener;
import reman.client.app.finance.equations.exceptions.MathException;
import reman.client.app.finance.equations.tokens.Variable;
import reman.client.app.finance.events.ChangeState;
import reman.client.app.finance.templates.events.TemplateLineItemEvent;
import reman.client.app.finance.templates.events.TemplateLineItemListner;
import reman.common.database.DatabaseObject;
import reman.common.database.ManagedDatabaseObject;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.DatabaseObjectException;

/**
 * 
 * This class provides functionality to build relationships between financial entities in the form of Equation objects by using the Variable objects contained within.
 * <br/>Maintains an Equation and transaction type the result of the evaluated equation will be.
 * <br/>This class will also send notification messages when the internal Equation changes.
 * @author Scott
 *
 */
public abstract class TemplateLineItem extends ManagedDatabaseObject implements
		EquationChangeListener {

	protected Equation eq_;
	private TransactionType normal_balance_;

	/**
	 * to notify template containing this line item when its equation changes
	 */
	private ArrayList<TemplateLineItemListner> change_listeners_;

	/**
	 * For DatabaseObject use only
	 */
	protected TemplateLineItem() {
		super(new String[] { "eq_" });
	}

	/**
	 * 
	 * @param descrption of the evaluated Equation <code>eq</code> result.
	 * @param eq The Equation which will represent the AcctAmount once generated and calculated.
	 * @param normal_balance How the evaluated expression's value will be interpreted as.
	 */
	public TemplateLineItem(String descrption, Equation eq, TransactionType normal_balance) {
		this();
		super.description_ = descrption;
		this.normal_balance_ = normal_balance;
		this.change_listeners_ = new ArrayList<TemplateLineItemListner>();
		this.setEquation(eq);
	}

	/**
	 * Add a listener to this TemplateLineItem's change events
	 * @param ecl
	 * @return
	 */
	public boolean addChangeListener(TemplateLineItemListner ecl) {
		return this.change_listeners_.add(ecl);
	}

	/**
	 * Add a listener to this TemplateLineItem's change events
	 * @param ecl
	 * @return
	 */
	public boolean removeChangeListener(TemplateLineItemListner ecl) {
		return this.change_listeners_.remove(ecl);
	}

	/**
	 * Obtain a copied collection of the objects listening to this TemplateLineItem.
	 * @return
	 */
	public ArrayList<TemplateLineItemListner> getChangeListeners() {
		return new ArrayList<TemplateLineItemListner>(this.change_listeners_);
	}

	/**
	 * Notify listeners of an event.
	 * @param ece
	 */
	private void fireChange(TemplateLineItemEvent ece) {
		for (TemplateLineItemListner ecl : this.change_listeners_)
			ecl.eventOccurred(ece);
	}

	/**
	 * Change the internal Equation to <code>eq</code>.
	 * <br/>This method will fire change events before and after (if the Equation was not and is not set to null) the Equation changes.
	 * @param eq New Equation object, or null if not equation is desired.
	 */
	public void setEquation(Equation eq) {
		/*if a previous equation, unsubscribe from listener, and new change even of this object*/
		if (this.eq_ != null) {
			this.eq_.removeChangeListener(this);
			fireChange(new TemplateLineItemEvent(this, ChangeState.BEFORE_CHANGE));
		}

		this.eq_ = eq;

		if (this.eq_ != null) {
			/*subscribe to new equation, and fire change of this object*/
			this.eq_.addChangeListener(this);
			this.fireChange(new TemplateLineItemEvent(this, ChangeState.AFTER_CHANGE));
		}
	}

	public TransactionType getNormalBalance() {
		return this.normal_balance_;
	}

	public void setNormalBalance(TransactionType type) {
		this.normal_balance_ = type;
	}

	/**
	 * Delegate the changing of this TemplateLineItem's internal Equation on to consumers of this TemplateLineItem's EquationChangeEvent
	 */
	public void equationChange(EquationChangeEvent e) {
		fireChange(new TemplateLineItemEvent(this, e.getState()));
	}

	/**
	 * Update the Equation of this TemplateLineItem to <code>expression</code>.
	 * <br/>This method will fire change events before and after (if the Equation was not and is not set to null) the Equation changes.
	 * @param expression
	 * @throws MathException if <code>expression</code> is not a valid Equation.
	 */
	public void setEquation(String expression) throws MathException {
		Equation e = new Equation(expression);
		this.setEquation(e);
	}

	/**
	 * Obtain a reference to the Equation object contained within.
	 * @return
	 */
	public Equation getEquation() {
		return this.eq_;
	}

	/**
	 * Obtain the result of evaluating this object
	 * @return The implementing template object.
	 * @throws MathException
	 * @throws DatabaseObjectException If the variable's assignment IVariableAssignable threw and exception.
	 */
	public abstract DatabaseObject generateTemplateLineItem() throws MathException, SQLException,
			DatabaseException;

	/**
	 * Obtain a map (keyed by name) of the Variable objects contained within this object's Equation.
	 * @return
	 */
	public Hashtable<String, Variable> getVariables() {
		return this.eq_.getVariables();
	}

	public String toString() {
		return this.eq_.toString();
	}
}

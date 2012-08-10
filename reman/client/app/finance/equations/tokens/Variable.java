package reman.client.app.finance.equations.tokens;

import java.sql.SQLException;

import reman.client.app.finance.equations.IVariableAssignable;
import reman.client.app.finance.equations.events.EquationEvaluatedEvent;
import reman.client.app.finance.equations.events.EquationEvaluatedListener;
import reman.client.app.finance.events.ChangeState;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.LoggedInException;

/**
 * Represents a variable in an equation.  Upon encountering a new variable this class will be created to represent its value,
 * and any equivalent variables (variables are uniqley identified by name) encountered there after will point to same variable object.
 * 
 * @author Scott
 *
 */
public class Variable extends Token implements EquationEvaluatedListener {
	private ValueToken value_;
	private boolean value_set_;
	private IVariableAssignable variable_assignable_;

	/**
	 * For DatabaseObject use only
	 */
	private Variable() {
	}

	public Variable(String variable_name) {
		mark_ = TokenMarkType.VARIABLE;
		this.name_ = variable_name;
		this.value_ = new DoubleToken(0);
		this.value_set_ = false;
		this.variable_assignable_ = null;
	}

	/**
	 * If this Variable has been assigned to an IVariableAssignable, this method will attempt to obtain the value from that object.
	 * @return
	 */
	public ValueToken getValueToken() {
		if (this.variable_assignable_ != null) {
			Double curr_value = this.variable_assignable_.getValue();
			DoubleToken curr_tok_value = null;
			if (curr_value != null)
				curr_tok_value = new DoubleToken(curr_value);
			this.setValue(curr_tok_value);
		}
		return this.value_;
	}

	/**
	 * Assign this variable with the <code>value</code>.  Or if <code>value</code> is null then un-assign this variable.
	 * @param value 
	 */
	public void setValue(ValueToken value) {
		this.value_ = value;
		this.value_set_ = this.value_ != null;
	}

	/**
	 * If this variable is set with a value, this method will return true.
	 * @return
	 */
	public boolean isSet() {
		return this.value_set_;
	}

	public String toString() {
		if (this.isSet()) {
			ValueToken vt = this.getValueToken();
			if (vt != null) {
				return ((Double) vt.getDoubleValue()).toString();
			}
		}
		return this.getName();
	}

	/**
	 * A variable assignable object can be passed in, to set this variable
	 * @param iva
	 */
	public void setValue(IVariableAssignable iva) {
		this.variable_assignable_ = iva;
	}

	/**
	 * If this variable is listening to an equation, and that equation finishes evaluating then this variable will take
	 * the final evaluation value.
	 */
	@Override
	public void equationEvaluated(EquationEvaluatedEvent e) {
		if (e.getState() == ChangeState.AFTER_CHANGE) {
			this.setValue(e.getEquation());
		}
	}

	/**
	 * Manually provided recursive lock, can't use ManagedDatabaseObject because this class inherits from a class that uses ManagedDatabaseObject
	 * @throws LoggedInException 
	 */
	@Override
	public boolean lock() throws SQLException, LoggedInException {
		boolean valid_lock = true;
		if (!this.value_.lock())
			valid_lock = false;

		if (valid_lock && super.lock())
			return true;

		this.unlock();
		return false;
	}

	/**
	 * Manually provided recursive unlock,  can't use ManagedDatabaseObject because this class inherits from a class that uses ManagedDatabaseObject
	 * @throws LoggedInException 
	 */
	@Override
	public boolean unlock() throws SQLException, LoggedInException {
		boolean valid_unlock = true;
		if (!this.value_.unlock())
			valid_unlock = false;
		
		if (super.unlock() && valid_unlock)
			return true;
		return false;
	}

	/**
	 * Manually provided recursive retrieve,  can't use ManagedDatabaseObject because this class inherits from a class that uses ManagedDatabaseObject
	 * @throws DatabaseException 
	 */
	@Override
	public void retrieve() throws SQLException, DatabaseException {
		this.value_.retrieve();
		super.retrieve();
	}

	/**
	 * Manually provided recursive commit,  can't use ManagedDatabaseObject because this class inherits from a class that uses ManagedDatabaseObject
	 * @throws DatabaseException 
	 */
	@Override
	public long commit() throws SQLException, DatabaseException {
		if(this.value_.commit() < 0)
				return -1;
		return super.commit();
	}
}

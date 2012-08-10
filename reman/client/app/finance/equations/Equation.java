package reman.client.app.finance.equations;

import java.util.ArrayList;
import java.util.Hashtable;

import reman.client.app.finance.equations.events.EquationChangeEvent;
import reman.client.app.finance.equations.events.EquationChangeListener;
import reman.client.app.finance.equations.events.EquationEvaluatedEvent;
import reman.client.app.finance.equations.events.EquationEvaluatedListener;
import reman.client.app.finance.equations.exceptions.MathException;
import reman.client.app.finance.equations.exceptions.MathInternalException;
import reman.client.app.finance.equations.exceptions.NoFunctionException;
import reman.client.app.finance.equations.exceptions.VariableNotSetException;
import reman.client.app.finance.equations.functions.Function;
import reman.client.app.finance.equations.tokens.DoubleToken;
import reman.client.app.finance.equations.tokens.Operator;
import reman.client.app.finance.equations.tokens.Token;
import reman.client.app.finance.equations.tokens.TokenMarkType;
import reman.client.app.finance.equations.tokens.ValueToken;
import reman.client.app.finance.equations.tokens.Variable;
import reman.client.app.finance.equations.validation.TokenCheck;
import reman.client.app.finance.events.ChangeState;
import reman.common.database.ManagedDatabaseObject;
import reman.common.database.exceptions.DatabaseObjectException;

/**
 * This will manage an expression that contains variables, functions, and constants.
 * This equation class will evaluate the expression where parentheses and standard order of operation dictate computation priority.
 * In order to evaluate and expression, all variables must be set.
 * This class will fire change events when it's expression is modified.
 * @author Scott
 *
 */
public class Equation extends ManagedDatabaseObject implements IVariableAssignable {
	/**
	 * This is the original tokens of the equation, this will be preserved throughout calculating
	 * the equation's result
	 */
	private ArrayList<Token> tokens_;

	/**
	 * a table of all unique variables contained within the equation, indexed by name
	 */
	private Hashtable<String, Variable> variables_;

	/**
	 * number parens within the equation
	 */
	private int num_parens_;

	/**
	 * When this equation is modified, notify listeners
	 */
	private ArrayList<EquationChangeListener> change_listeners_;

	/**
	 * When this equation is evaluated, these objects will be notified
	 */
	private ArrayList<EquationEvaluatedListener> evaluated_listeners_;

	/**
	 * For DatabaseObject use only
	 */
	private Equation() {
		super(new String[] { "tokens_", "variables_" });
	}

	/**
	 * Initialize this Equation with <code>expression</code>.
	 * @param expression An equation containing variables, constants, and functions.
	 * @throws MathException
	 */
	public Equation(String expression) throws MathException {
		this(expression, "");
	}

	/**
	 * Initialize this Equation with <code>expression</code>.
	 * @param expression An equation containing variables, constants, and functions.
	 * @param description Description of what this Equation represents.
	 * @throws MathException
	 */
	public Equation(String expression, String description) throws MathException {
		this();
		this.tokens_ = new ArrayList<Token>();
		this.variables_ = new Hashtable<String, Variable>();
		this.change_listeners_ = new ArrayList<EquationChangeListener>();
		this.evaluated_listeners_ = new ArrayList<EquationEvaluatedListener>();
		super.setDescription(description);
		this.setEquation(expression);
	}

	/**
	 * Copy constructor.  Initialize this Equation to mirror <code>equation</code>.
	 * @param equation
	 */
	public Equation(Equation equation) {
		this.tokens_ = new ArrayList<Token>(equation.getTokens());
		this.variables_ = new Hashtable<String, Variable>(equation.getVariables());
		this.num_parens_ = equation.getNumParens();
		//this.change_listeners_ = equation.getChangeListeners();
	}

	public String toString() {
		String expression = "";
		for (Token t : this.tokens_)
			expression += t.toString();
		return expression;
	}

	/**
	 * Set this Equation to be represented by <code>expression</code>.  This method will validate and parse <code>expression</code>
	 * and return appropriate exceptions if invalid tokens are encountered.
	 * @param expression 
	 * @throws MathException 
	 */
	public void setEquation(String expression) throws MathException {
		TokenCheck token_checker = new TokenCheck();

		token_checker.tokenizeExpression(expression);
		this.fireChange(new EquationChangeEvent(this, ChangeState.BEFORE_CHANGE));

		this.tokens_ = token_checker.getTokens();
		this.variables_ = token_checker.getVariables();
		this.num_parens_ = token_checker.getNumParens();

		this.fireChange(new EquationChangeEvent(this, ChangeState.AFTER_CHANGE));
	}

	/**
	 * Get the tokens which make up this Equation.
	 * @return
	 */
	public ArrayList<Token> getTokens() {
		return new ArrayList<Token>(this.tokens_);
	}

	/**
	 * Obtain a map (keyed by name) of variables contained within this Equation.
	 * @return
	 */
	public Hashtable<String, Variable> getVariables() {
		return this.variables_;
	}

	/**
	 * Assign the variable identified by <code>name</code> with <code>value</code>.
	 * @param name Name of variable to set.
	 * @param value Value to set the corresponding variable.
	 * @return True if variable was found and set
	 */
	public boolean setVariable(String name, ValueToken value) {
		Variable var = this.variables_.get(name);
		if (var != null) {
			var.setValue(value);
			return true;
		}
		return false;
	}

	/**
	 * Assign the variable identified by <code>name</code> with <code>value</code>
	 * @param name Name of variable to set.
	 * @param value Value to set the corresponding variable.
	 * @return True if variable was found and set
	 */
	public boolean setVariable(String name, double value) {
		return this.setVariable(name, new DoubleToken(value));
	}

	/**
	 * Add a listener which will be informed before/after this equation changes.
	 * @param ecl
	 * @return True if <code>ecl</code> was successfully added.
	 */
	public boolean addChangeListener(EquationChangeListener ecl) {
		return this.change_listeners_.add(ecl);
	}

	/**
	 * Remove an equation change listener.
	 * @param ecl
	 * @return True if <code>ecl</code> was successfully removed.
	 */
	public boolean removeChangeListener(EquationChangeListener ecl) {
		return this.change_listeners_.remove(ecl);
	}

	/**
	 * Obtain a collection of EquationChangeListener which are listening to this equation.
	 * @return
	 */
	public ArrayList<EquationChangeListener> getChangeListeners() {
		return new ArrayList<EquationChangeListener>(this.change_listeners_);
	}

	/**
	 * Add an EquationEvaluatedListener which will be notified before/after this Equation is evaluated.
	 * @param ecl
	 * @return True if <code>eel</code> was successfully added.
	 */
	public boolean addEvaluateListener(EquationEvaluatedListener eel) {
		return this.evaluated_listeners_.add(eel);
	}

	/**
	 * Remove an EquationEvaluatedListener.
	 * @param ecl
	 * @return True if <code>eel</code> was successfully removed.
	 */
	public boolean removeChangeListener(EquationEvaluatedListener ecl) {
		return this.evaluated_listeners_.remove(ecl);
	}

	/**
	 * Obtain a collection of EquationEvaluatedListener which are listening to this equation.
	 * @return
	 */
	public ArrayList<EquationEvaluatedListener> getEvaluteListeners() {
		return new ArrayList<EquationEvaluatedListener>(this.evaluated_listeners_);
	}

	private void fireEvaluateEvent(EquationEvaluatedEvent e) {
		for (EquationEvaluatedListener ecl : this.evaluated_listeners_)
			ecl.equationEvaluated(e);
	}

	private void fireChange(EquationChangeEvent ece) {
		for (EquationChangeListener ecl : this.change_listeners_)
			ecl.equationChange(ece);
	}

	/**
	 * 
	 * @return Number of parenthesis contained within this equation
	 */
	public int getNumParens() {
		return this.num_parens_;
	}

	/**
	 * 
	 * @return First index of close parenthesis token. -1 if no close parenthesis found
	 */
	private int getFirstCloseParen() {
		for (int i = 0; i < this.tokens_.size(); i++) {
			if (this.tokens_.get(i).getMark().equals(TokenMarkType.CLOSE_PAREN))
				return i;
		}
		return -1;
	}

	/**
	 * 
	 * @param index_close_paren Index of closed parenthesis to find corresponding open parenthesis
	 * @return Index of open parenthesis associated with closed parenthesis positioned at 'index_close_paren', or -1 if not found
	 */
	private int getOpenParen(int index_close_paren) {
		for (int i = index_close_paren - 2; i >= 0; i--) {
			if (this.tokens_.get(i).getMark().equals(TokenMarkType.OPEN_PAREN))
				return i;
		}
		return -1;
	}

	/**
	 * 
	 * @param start_index This index+1 will be where the search will begin
	 * @param end_index This index-1 will be the last index checked
	 * @return Index of highest priority operator with in the DoubleRange provided, or -1 if not found;
	 */
	private int getHighestPriorityOperator(int start_index, int end_index) {
		int high_priority_index = -1;
		byte high_priority = Byte.MIN_VALUE;

		Token curr_token;
		byte curr_priority;

		for (int i = start_index + 1; i <= end_index - 1; i++) {
			curr_token = this.tokens_.get(i);
			if (!curr_token.getMark().equals(TokenMarkType.OPERATOR))
				continue;

			curr_priority = ((Operator) curr_token).getPriority();
			if (curr_priority > high_priority) {
				high_priority = curr_priority;
				high_priority_index = i;
			}

		}
		return high_priority_index;
	}

	/**
	 * Should be called in such a way that the inner most functions are evaluated first.
	 * @param fnt_name_index
	 * @return
	 * @throws MathException 
	 * @throws DatabaseObjectException If variable assignment threw and exception
	 */
	private double getFunctionResult(int fnt_name_index) throws MathException,
			DatabaseObjectException {
		/*fun(fun(2,4),fun(7),2,3)*/
		ArrayList<ValueToken> args = new ArrayList<ValueToken>();
		if (fnt_name_index < 0 || fnt_name_index >= this.tokens_.size())
			throw new NoFunctionException("Function index '" + fnt_name_index + "' out of bounds.");
		Token curr_token = this.tokens_.get(fnt_name_index);
		if (!curr_token.getMark().equals(TokenMarkType.FUNCTION))
			throw new NoFunctionException("No function found at index '" + fnt_name_index + "'.");

		Function fnt = (Function) curr_token;
		for (int i = fnt_name_index + 2; i < this.tokens_.size(); i++) {
			curr_token = this.tokens_.get(i);
			if (curr_token.getMark().equals(TokenMarkType.CLOSE_PAREN))
				break;
			else if (curr_token.getMark().equals(TokenMarkType.COMMA))
				continue;
			else if (curr_token.getMark().equals(TokenMarkType.DOUBLE))
				args.add((DoubleToken) curr_token);
			else if (curr_token.getMark().equals(TokenMarkType.VARIABLE)) {
				Variable var = (Variable) curr_token;
				if (var.isSet())
					args.add(var.getValueToken());
				else
					throw new VariableNotSetException(var);
			} else
				throw new MathInternalException("Invalid token of type '" + curr_token.getMark()
						+ "' while evaluating function '" + fnt.toString()
						+ "'. Functions must be evaluated inner most first.");
		}

		/*Dynamically create the function class, and invoke the evaluate method*/
		try {
			return fnt.evaluate(args);
		} catch (Exception ex) {
			throw new MathException("Funciton '" + fnt.toString()
					+ "' method 'evaluate(..)' threw and error of: " + ex.getMessage());
		}
	}

	/**
	 * Should be called in such a way that the inner most functions are evaluated first.
	 * @param fnt_name_index
	 * @throws MathException 
	 */
	private void functionRemoval(int fnt_name_index, int fnt_close_paren_index) throws MathException {
		if (!this.tokens_.get(fnt_name_index).getMark().equals(TokenMarkType.FUNCTION))
			throw new MathInternalException("Can not remove function arguments. Index '" + fnt_name_index
					+ "' does not point to a funciton.");
		if (!this.tokens_.get(fnt_close_paren_index).getMark().equals(TokenMarkType.CLOSE_PAREN))
			throw new MathInternalException("Can not remove function arguments. Index '"
					+ fnt_close_paren_index + "' does not point to a close paren.");
		for (int i = fnt_close_paren_index; i >= fnt_name_index; i--)
			this.tokens_.remove(i);
	}

	/**
	 * Supports bit wise and decimal binary operations.
	 * @param operand_left
	 * @param op
	 * @param operand_right
	 * @return Result of (operand_left op operand_right)
	 * @throws MathException
	 */
	private double getOperationResult(double operand_left, Operator op, double operand_right)
			throws MathException {
		try {
			if (op.getOperator().equals("+"))
				return operand_left + operand_right;
			else if (op.getOperator().equals("-"))
				return operand_left - operand_right;
			else if (op.getOperator().equals("*"))
				return operand_left * operand_right;
			else if (op.getOperator().equals("/")) {
				if (operand_right != 0)
					return operand_left / operand_right;
				else
					throw new MathException("Divide by 0.");
			} else if (op.getOperator().equals("^"))
				return Math.pow(operand_left, operand_right);
			else if (op.getOperator().equals("%")) {
				if (operand_right != 0)
					return operand_left % operand_right;
				else
					throw new MathException("Divide by 0.");
			} else if (op.getOperator().equals("=="))
				return (operand_left == operand_right) ? 1 : 0;
			else if (op.getOperator().equals("!="))
				return (operand_left != operand_right) ? 1 : 0;
			else if (op.getOperator().equals(">"))
				return (operand_left > operand_right) ? 1 : 0;
			else if (op.getOperator().equals("<"))
				return (operand_left < operand_right) ? 1 : 0;
			else if (op.getOperator().equals(">="))
				return (operand_left >= operand_right) ? 1 : 0;
			else if (op.getOperator().equals("<="))
				return (operand_left <= operand_right) ? 1 : 0;
			else if (op.getOperator().equals("&&"))
				return (operand_left != 0 && operand_right != 0) ? 1 : 0;
			else if (op.getOperator().equals("||"))
				return (operand_left != 0 || operand_right != 0) ? 1 : 0;
			else if (op.getOperator().equals("xor"))
				return ((long) operand_left ^ (long) operand_right);
			else if (op.getOperator().equals("<<"))
				return ((long) operand_left << (long) operand_right);
			else if (op.getOperator().equals(">>"))
				return ((long) operand_left >> (long) operand_right);
			else if (op.getOperator().equals(">>>"))
				return ((long) operand_left >>> (long) operand_right);
			else if (op.getOperator().equals("and"))
				return ((long) operand_left & (long) operand_right);
			else if (op.getOperator().equals("or"))
				return ((long) operand_left | (long) operand_right);
			else
				throw new MathInternalException("Unhandled operator '" + op.getOperator()
						+ "' in evaluation.");
		} catch (Exception ex) {
			throw new MathException("Math operation error: '" + operand_left + "' '" + op + "' '"
					+ operand_right + "'.");
		}

	}

	/**
	 * Remove un-needed parentheses around the 'index' param
	 * @param index Open paren index
	 * @return True if parens where removed.
	 */
	private boolean removeParens(int index) {
		if ((index > 1 && (!this.tokens_.get(index - 2).getMark().equals(TokenMarkType.FUNCTION)
				&& this.tokens_.get(index - 1).getMark().equals(TokenMarkType.OPEN_PAREN) && this.tokens_
				.get(index + 1).getMark().equals(TokenMarkType.CLOSE_PAREN)))
				|| (index == 1 && this.tokens_.get(0).getMark().equals(TokenMarkType.OPEN_PAREN) && this.tokens_
						.get(2).getMark().equals(TokenMarkType.CLOSE_PAREN))) {
			this.tokens_.remove(index + 1);
			this.tokens_.remove(index - 1);

			return true;
		}
		/*check for "()"*/
		if (index >= 0 && index + 1 < this.tokens_.size()
				&& this.tokens_.get(index).getMark().equals(TokenMarkType.OPEN_PAREN)
				&& this.tokens_.get(index + 1).getMark().equals(TokenMarkType.CLOSE_PAREN)) {
			this.tokens_.remove(index);
			this.tokens_.remove(index + 1);

			return true;
		}
		return false;
	}

	/**
	 * Will attempt to reduce the equation down to the answer. Only binary math operations are supported.
	 * @param retain_expression If false the expression will be replaced by the result. If true the result will be returned but
	 * 												will no replace the expression's tokens.
	 * @return Result.
	 * @throws MathException Invalid math, Invalid operator, variable not set, function not found
	 * @throws DatabaseObjectException If a variable's assignment from IVariableAssignable threw an excpetion
	 */
	public String evaluate(boolean retain_expression) throws MathException, DatabaseObjectException {
		/*make a copy in-case any unforeseen exceptions occur*/
		this.fireEvaluateEvent(new EquationEvaluatedEvent(this, ChangeState.BEFORE_CHANGE));
		ArrayList<Token> tokens_cpy = new ArrayList<Token>(this.tokens_);

		/*to catch infinite loop*/
		int previous_size = -1;

		try {
			while (this.tokens_.size() > 1 && this.tokens_.size() != previous_size) {
				previous_size = this.tokens_.size();

				int first_close_paren = this.getFirstCloseParen();
				int open_paren = this.getOpenParen(first_close_paren);
				/*if no more parens left, just finish by getting highest priority op*/
				if (first_close_paren < 0 && open_paren < 0) {
					first_close_paren = this.tokens_.size() - 1;
					open_paren = 0;
				}
				int max_priority_op = this.getHighestPriorityOperator(open_paren, first_close_paren);
				if (max_priority_op < 0) {
					/*if no operator found, then this is a function, or possibly empty parens*/
					try {
						double fnt_result_value = this.getFunctionResult(open_paren - 1);
						/*TODO: support for non base10*/
						ValueToken fnt_result_token = new DoubleToken(fnt_result_value);

						this.functionRemoval(open_paren - 1, first_close_paren);
						this.tokens_.add(open_paren - 1, fnt_result_token);
					} catch (NoFunctionException ex) {
						/*if no function found this could be empty parens or just a number in parens*/
						/*get rid of parens around a number*/
						while (this.removeParens(open_paren + 1)) {
						}
						/*get rid of "()"*/
						while (this.removeParens(open_paren)) {
						}
					}
					/*remove all unnecessary parens*/
					while (this.removeParens(open_paren - 1)) {
					}
				} else {
					/*an operator is found and the local expression will need to be evaluated and simplified*/
					/*binary operations only*/
					ValueToken operand_left = this.getOperandValue(this.tokens_.get(max_priority_op - 1));
					ValueToken operand_right = this.getOperandValue(this.tokens_.get(max_priority_op + 1));

					Operator op = (Operator) this.tokens_.get(max_priority_op);

					double op_result_value = this.getOperationResult(operand_left.getDoubleValue(), op,
							operand_right.getDoubleValue());
					ValueToken op_result_token = new DoubleToken(op_result_value);

					/*remove used tokens, add result token*/
					this.tokens_.remove(max_priority_op + 1);
					this.tokens_.remove(max_priority_op);
					this.tokens_.set(max_priority_op - 1, op_result_token);

					/*remove all unnecessary parens*/
					while (this.removeParens(max_priority_op - 1)) {
					}
				}
			}
			Double dresult = null;
			if (this.tokens_.size() == 1) {
				ValueToken result = null;
				if (this.tokens_.get(0) instanceof ValueToken) {
					result = (ValueToken) this.tokens_.get(0);
				} else if (this.tokens_.get(0) instanceof Variable) {
					Variable v = (Variable) this.tokens_.get(0);
					if (v.isSet())
						result = v.getValueToken();
				}
				if (result != null)
					dresult = result.getDoubleValue();
				else {
					throw new MathInternalException(
							"Final token did not evalute down to a ValueToken or a set Variable.");
				}
			} else {
				throw new MathInternalException("General computation error.");
			}
			this.fireEvaluateEvent(new EquationEvaluatedEvent(this, ChangeState.AFTER_CHANGE));
			if (retain_expression)
				this.tokens_ = tokens_cpy;
			return dresult.toString();
		} catch (MathInternalException ex) {
			this.tokens_ = tokens_cpy;
			throw ex;
		}
	}

	/**
	 * Provide generality to support calculations with variables and tokens which contain values other than base 10.
	 * @param operand
	 * @return
	 * @throws MathInternalException
	 * @throws DatabaseObjectException If the variable assignment IVariableAssignment threw and exception
	 */
	private ValueToken getOperandValue(Token operand) throws MathInternalException,
			DatabaseObjectException {
		if (operand.getMark() == TokenMarkType.DOUBLE)
			return ((DoubleToken) operand);
		if (operand.getMark() == TokenMarkType.VARIABLE)
			return ((Variable) operand).getValueToken();
		throw new MathInternalException("Unhandled operand type '" + operand + "'.");
	}

	@Override
	public Double getValue() {
		try {
			return Double.parseDouble(this.evaluate(true));
		} catch (MathException e) {
		} catch (DatabaseObjectException e) {
		}
		return null;
	}
}

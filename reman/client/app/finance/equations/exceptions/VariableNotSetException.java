package reman.client.app.finance.equations.exceptions;

import reman.client.app.finance.equations.tokens.Variable;

/**
 * This exception is thrown when an attempt to evaluate an Equation fails because of a variable not being set.
 * @author Scott
 *
 */
public class VariableNotSetException extends MathException{
	private Variable var_;
	
	public VariableNotSetException(Variable var)
	{
		super("Variable '"+var+"' was not set before equation execution.");
		this.var_ = var;
	}
	
	public String toString()
	{
		return "Variable '"+this.var_+"' was not set before equation execution.";
	}
}

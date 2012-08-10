package reman.client.app.finance.equations.functions;

import java.util.ArrayList;

import reman.client.app.finance.equations.tokens.ValueToken;

/**
 * Function implementation providing the ability to extract the summation value of parameters.
 * @author Scott
 *
 */
public class Sum extends Function{
	
	public Sum()
	{
		super("sum", -1);
	}
	
	/**
	 * Returns the summation value of <code>args</code>.
	 */
	@Override
	public double evaluate(ArrayList<ValueToken> args)
	{
		double result = 0;
		for(ValueToken d : args)
			result += d.getDoubleValue();
		
		return result;
	}

}

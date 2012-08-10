package reman.client.app.finance.equations.functions;

import java.util.ArrayList;

import reman.client.app.finance.equations.tokens.ValueToken;

/**
 * Function implementation providing the ability to extract the maximum value from parameters.
 * @author Scott
 *
 */
public class Max extends Function{

	public Max()
	{
		super("max",-1);
	}
	
	/**
	 * Returns the maximum value amongst <code>args</code>.
	 */
	@Override
	public double evaluate(ArrayList<ValueToken> args)
	{
		double curr_max = Double.MIN_VALUE;
		for(ValueToken d : args)
		{
			if(curr_max < d.getDoubleValue())
				curr_max = d.getDoubleValue();
		}
		return curr_max;
	}
}

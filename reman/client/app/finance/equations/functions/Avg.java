package reman.client.app.finance.equations.functions;

import java.util.ArrayList;

import reman.client.app.finance.equations.tokens.ValueToken;

/**
 * This class provides Average function computation capabilities.
 * @author Scott
 *
 */
public class Avg extends Function{

	public Avg()
	{
		super("avg",-1);
	}
	
	/**
	 * Returns the average of all ValueTokens in <code>args</code>
	 */
	@Override
	public double evaluate(ArrayList<ValueToken> args)
	{
		double result = 0;
		for(ValueToken d : args)
			result += d.getDoubleValue();
		return result/=args.size();
	}
}

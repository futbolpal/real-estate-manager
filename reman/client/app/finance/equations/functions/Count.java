package reman.client.app.finance.equations.functions;

import java.util.ArrayList;

import reman.client.app.finance.equations.tokens.ValueToken;

/**
 * Arguments count
 * @author Scott
 *
 */
public class Count extends Function{

	public Count()
	{
		super("count",-1);
	}
	
	/**
	 * Returns the amount of arguments in <code>args</code>
	 */
	@Override
	public double evaluate(ArrayList<ValueToken> args)
	{
		return args.size();
	}
}

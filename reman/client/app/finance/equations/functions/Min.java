package reman.client.app.finance.equations.functions;

import java.util.ArrayList;

import reman.client.app.finance.equations.tokens.ValueToken;

/**
 * Function implementation providing the ability to extract the minimum value from parameters.
 * @author Scott
 *
 */
public class Min extends Function {

	public Min() {
		super("min", -1);
	}

	/**
	 * Returns the minimum value amongst <code>args</code>.
	 */
	@Override
	public double evaluate(ArrayList<ValueToken> args) {
		double curr_min = Double.MAX_VALUE;
		for (ValueToken d : args) {
			if (curr_min > d.getDoubleValue())
				curr_min = d.getDoubleValue();
		}
		return curr_min;
	}
}
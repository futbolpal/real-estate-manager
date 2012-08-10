package reman.client.app.finance.equations.tokens;

/**
 * Provides functionality to support calculation, and output of values other than just base 10.
 * @author Scott
 *
 */
public abstract class ValueToken extends Token {
	
	/**
	 * DatabaseObject use only
	 */
	protected ValueToken(){}
	
	public ValueToken(double value){}
	
	/**
	 * This is used by the engine during calculation.  The implementing numerical base must implement this method
	 * to provide correct calculation results.
	 * @return
	 */
	public abstract double getDoubleValue();
	
	public abstract String toString();
}

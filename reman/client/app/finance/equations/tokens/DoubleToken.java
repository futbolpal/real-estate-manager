package reman.client.app.finance.equations.tokens;

/**
 * Token for a double value, used in equation parsing and evaluation.  This can take positive or negative value.
 * @author Scott
 *
 */
public class DoubleToken extends ValueToken {

	private double value_;

	/**
	 * DatabaseObject use only
	 */
	private DoubleToken() {
	}

	public DoubleToken(double value) {
		this.mark_ = TokenMarkType.DOUBLE;
		this.value_ = value;
	}

	public double getDoubleValue() {
		return this.value_;
	}
	
	public String toString(){
		return ((Double)this.value_).toString();
	}
}

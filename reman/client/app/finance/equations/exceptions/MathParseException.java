package reman.client.app.finance.equations.exceptions;

/**
 * This class provides a base class exception that will be thrown as a result of an invalid token, or token sequence.
 * @author Scott
 *
 */
public class MathParseException extends MathException {
	private int error_index_;
	public MathParseException(String message, int index) {
		super(message);
		this.error_index_ = index;
	}
	
	public String toString()
	{
		return this.getMessage()+" (index: '"+this.error_index_+"')";
	}
}

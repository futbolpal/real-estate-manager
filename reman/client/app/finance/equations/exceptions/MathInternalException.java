package reman.client.app.finance.equations.exceptions;

/**
 * If something is internally wrong with the computation process. This should error should be corrected by the Equation engine developer
 * and should not be thrown during production.
 * @author Scott
 *
 */
public class MathInternalException extends MathException {
	public MathInternalException(String message) {
		super(message);
	}

	public String toString() {
		return "INTERNAL: " + this.getMessage();
	}
}

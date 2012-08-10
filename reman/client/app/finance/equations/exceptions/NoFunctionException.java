package reman.client.app.finance.equations.exceptions;

/**
 * This exception is thrown if a function name has no corresponding Function class to evaluate.
 * @author Scott
 *
 */
public class NoFunctionException extends MathException{

	public NoFunctionException(String message)
	{
		super(message);
	}
}

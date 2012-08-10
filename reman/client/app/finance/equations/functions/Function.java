package reman.client.app.finance.equations.functions;

import java.util.ArrayList;

import reman.client.app.finance.equations.tokens.Token;
import reman.client.app.finance.equations.tokens.TokenMarkType;
import reman.client.app.finance.equations.tokens.ValueToken;

/**
 * Function is a type of token which implementing classes provide evaluation functionality when given arguments.
 * (This class must live with other functions for reflection to find other function definition classes.)
 * @author Scott
 *
 */
public abstract class Function extends Token {

	/**
	 * Number of expected arguments, -1 for arbitrary amount
	 */
	protected int valid_num_args_;

	/**For DatabaseObject use only*/
	private Function() {
	}

	public Function(String function_name, int valid_args) {
		this.mark_ = TokenMarkType.FUNCTION;
		this.name_ = function_name;
		this.valid_num_args_ = valid_args;
	}

	public int getValidNumArgs() {
		return this.valid_num_args_;
	}

	/**
	 * Obtain the run time Function class name implementation when an Equation is evaluated.
	 * @param fnt_name Non canonical name of function
	 * @return
	 */
	public static String getClassName(String fnt_name) {
		if (fnt_name.length() > 0) {
			fnt_name = fnt_name.toLowerCase();
			fnt_name = Character.toUpperCase(fnt_name.charAt(0)) + fnt_name.substring(1);
		}
		return fnt_name;
	}

	/**
	 * Obtain the run time Function class canonical name (package information included) implementation when an Equation is evaluated.
	 * @param fnt_name Non canonical name of function
	 * @return
	 */
	public static String getCanonicalName(String fnt_name) {		
		return Function.class.getCanonicalName().replace(Function.class.getSimpleName(),
				Function.getClassName(fnt_name));
	}

	/**
	 * This method will perform the function computation. Inheriting classes will implement this method to provide specific functionality.
	 * @param args
	 * @throws Exception Invalid arguments, code definition error
	 * @return
	 */
	public abstract double evaluate(ArrayList<ValueToken> args);
}

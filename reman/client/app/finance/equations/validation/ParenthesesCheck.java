package reman.client.app.finance.equations.validation;

import java.util.Stack;

/**
 * Pre-process an expression that will be then parsed into an Equation.
 * @author Scott
 *
 */
public class ParenthesesCheck {

	public static boolean isOpenParenthesis(char c) {
		if (c == '(' || c == '[' || c == '{')
			return true;
		else
			return false;
	}

	public static boolean isClosedParenthesis(char c) {
		if (c == ')' || c == ']' || c == '}')
			return true;
		else
			return false;
	}

	private static boolean parenthesesMatch(char open, char closed) {
		if (open == '(' && closed == ')')
			return true;
		else if (open == '[' && closed == ']')
			return true;
		else if (open == '{' && closed == '}')
			return true;
		else
			return false;
	}

	/**
	 * Determine if <code>exp</code> contains equal number of open and close parentheses, and also each corresponding open/close parentheses
	 * 					are of the same type.
	 * @param exp
	 * @return True if <code>exp</code> contains equal number of open and close parentheses, and also each corresponding open/close parentheses
	 * 					are of the same type.
	 */
	public static boolean parenthesesValid(String exp) {
		Stack<Character> s = new Stack<Character>();
		int i;
		char current_char;
		Character c;
		char c1;
		boolean ret = true;

		for (i = 0; i < exp.length(); i++) {

			current_char = exp.charAt(i);

			if (isOpenParenthesis(current_char)) {
				c = new Character(current_char);
				s.push(c);
			} else if (isClosedParenthesis(current_char)) {
				if (s.isEmpty()) {
					ret = false;
					break;
				} else {
					c = (Character) s.pop();
					c1 = c.charValue();
					if (!parenthesesMatch(c1, current_char)) {
						ret = false;
						break;
					}
				}
			}
		}

		if (!s.isEmpty())
			ret = false;

		return ret;
	}
}

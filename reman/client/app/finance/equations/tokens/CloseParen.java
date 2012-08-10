package reman.client.app.finance.equations.tokens;

/**
 * Token for a close parentheses, used in equation parsing and evaluation.
 * @author Scott
 *
 */
public class CloseParen extends Token {

	private CloseParen() {
	}

	/**
	 * 
	 * @param text Should be a close parentheses.
	 */
	public CloseParen(String text) {
		this.mark_ = TokenMarkType.CLOSE_PAREN;
		this.name_ = text;
	}
}

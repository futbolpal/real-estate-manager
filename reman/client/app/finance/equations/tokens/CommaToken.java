package reman.client.app.finance.equations.tokens;

/**
 * Token for a comma, used in equation parsing and evaluation.
 * @author Scott
 *
 */
public class CommaToken extends Token {

	/**
	 * Initialize comma token with name ','
	 */
	public CommaToken() {
		this.mark_ = TokenMarkType.COMMA;
		this.name_ = ",";
	}
}

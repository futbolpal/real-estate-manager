package reman.client.app.finance.equations.tokens;

/**
 * Token for a open parentheses, used in equation parsing and evaluation.
 * @author Scott
 *
 */
public class OpenParen extends Token{
	
	private OpenParen(){}
	
	/**
	 * 
	 * @param text Should be an open parentheses.
	 */
	public OpenParen(String text){
		this.mark_ = TokenMarkType.OPEN_PAREN;
		this.name_ = text;
	}

}

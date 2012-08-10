package reman.client.app.finance.equations.tokens;

import reman.client.app.finance.equations.exceptions.MathException;

/**
 * Indicates the run time token type, and provides priority for equation evaluation (highest priority is 0)
 * as well as an index for parsing validation.
 * @author Scott
 *
 */
public enum TokenMarkType {
	DOUBLE(0, 0), /*Double*/
	VARIABLE(2, 1), /*Variable*/
	FUNCTION(1, 2), /*Function --> priority should be less than variable*/
	OPERATOR(3, 3), /*Operator*/
	OPEN_PAREN(4, 4), /*Open paren*/
	CLOSE_PAREN(5, 5), /*Close paren*/
	COMMA(6, 6);/*Comma*/

	private int parse_check_priority_;
	private int array_seq_check_index_;

	/**
	 * 
	 * @param check_priority Priority used for evaluation of tokens.
	 * @param array_index Parsing validation array index.
	 */
	private TokenMarkType(int check_priority, int array_index) {
		this.parse_check_priority_ = check_priority;
		this.array_seq_check_index_ = array_index;
	}

	/**
	 * Parsing validation array index.
	 * @return
	 */
	public int getSeqCheckArrayIndex() {
		return this.array_seq_check_index_;
	}

	/**
	 * Priority used for evaluation of tokens.
	 * @return
	 */
	public int getParseCheckPriority() {
		return this.parse_check_priority_;
	}

	/**
	 * Obtain the TokenMarkType which has the parse priority corresponding to the <code>priority</code>. 
	 * @param priority
	 * @return
	 * @throws MathException
	 */
	public static TokenMarkType getMarkOfPriority(int priority)
			throws MathException {
		for (TokenMarkType tmt : TokenMarkType.values()) {
			if (tmt.getParseCheckPriority() == priority)
				return tmt;
		}
		throw new MathException("Unhandled priority '" + priority + "'.");
	}
}

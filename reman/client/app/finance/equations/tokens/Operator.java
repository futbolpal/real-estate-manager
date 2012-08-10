package reman.client.app.finance.equations.tokens;

/**
 * Token for an operator, used in equation parsing and evaluation.
 * @author Scott
 *
 */
public class Operator extends Token {

	private String operator_;
	
	/**
	 * Computation order of operation, used during evaluation
	 */
	private byte priority_;

	/**
	 * 
	 * @param operator Should be an operator.  See <code>getPriority(String)</code> method for supported operators.
	 */
	public Operator(String operator) {
		this.mark_ = TokenMarkType.OPERATOR;
		this.operator_ = operator;
		try {
			this.priority_ = Operator.getPriority(this.operator_);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			this.priority_ = -1;
		}
	}

	public String getOperator() {
		return this.operator_;
	}

	public byte getPriority() {
		return this.priority_;
	}

	public String toString() {
		return operator_;
	}

	/**
	 * lowest priority is 0.
	 * @throws Exception if <code>operator</code> is unsupported.
	 */
	public static byte getPriority(String operator) throws Exception {
		if (operator.equals("+") || operator.equals("-"))
			return 0;
		else if (operator.equals("*") || operator.equals("/"))
			return 1;
		else if (operator.equals("^"))
			return 2;
		else if (operator.equals("%"))
			return 1;
		else if (operator.equals("==") || operator.equals("!=") || operator.equals(">")
				|| operator.equals("<") || operator.equals(">=") || operator.equals("<="))
			return 0;
		else if (operator.equals("&&") || operator.equals("||"))
			return 0;
		else if (operator.equals("xor"))
			return 0;
		else if (operator.equals("<<") || operator.equals(">>") || operator.equals(">>>"))
			return 0;
		else if (operator.equals("and") || operator.equals("or"))
			return 0;
		else
			throw new Exception("Unhandled operator '" + operator + "'.");
	}
}

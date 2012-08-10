package reman.client.app.finance.equations;

/**
 * If this object's value can be used to set a variable's value.
 * EX: Account, AcctCategory, Equation (Evaluation value), 
 * @author Scott
 *
 */
public interface IVariableAssignable {
	/**
	 * 
	 * @return If value is not valid, null. EX: equation value attempted to be obtained but not evaluated yet.
	 */
	public Double getValue();
}

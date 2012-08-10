package reman.client.app.finance;

/**
 * Provides a representation of positive/negative for computations in the financial world.
 * @author Scott
 *
 */
public enum TransactionType {
	CREDIT, DEBIT;
	
	/**
	 * 
	 * @param t_type
	 * @return CREDIT if <code>t_type</code> is DEBIT and visa-versa.
	 */
	public static TransactionType getOpposite(TransactionType t_type)
	{
		if(t_type == TransactionType.CREDIT)
			return TransactionType.DEBIT;
		return TransactionType.CREDIT;
	}
}

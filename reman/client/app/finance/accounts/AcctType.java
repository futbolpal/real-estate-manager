package reman.client.app.finance.accounts;

/**
 * This represents the basic account types from the financial accounting world.
 * @author Scott
 *
 */
public enum AcctType {
	ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE, GAIN, LOSS, COMMISSION, SALES, OTHER_INCOME;
	/*SALES under REVENUE*/

	/**
	 * 
	 * @param type
	 * @return ASSET, LIABILITY, or EQUITY
	 */
	public static AcctType getGenericType(AcctType type) {
		if (type == REVENUE || type == EXPENSE || type == COMMISSION || type == SALES
				|| type == OTHER_INCOME)
			return EQUITY;
		else if (type == GAIN)
			return ASSET;
		else if (type == LOSS)
			return LIABILITY;
		else if (type == ASSET || type == LIABILITY || type == EQUITY)
			return type;
		return null;
	}

	public static boolean isTemporaryType(AcctType type) {
		if (type == REVENUE || type == EXPENSE || type == GAIN || type == LOSS || type == COMMISSION
				|| type == SALES || type == OTHER_INCOME)
			return true;
		return false;
	}
}

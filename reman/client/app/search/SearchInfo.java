package reman.client.app.search;

/**
 * Contains the SQL statements needed to perform a search on a Class in the application.
 * This class contains no algorithms or any real code, it simply holds two strings and is used in a Hashtable
 * to map a Class to the SQL statements needed to search that Class.
 * @author Will
 *
 */
public class SearchInfo {
	private String join_sql_, match_sql_;
	
	public SearchInfo(String join, String match) {
		join_sql_ = join;
		match_sql_ = match;
	}
	
	/**
	 * Gets the SQL statement to that is needed to join tables together before performing 
	 * the actual match.
	 * @return A string of SQL statements.
	 */
	public String getJoinSql() {
		return join_sql_;
	}
	
	/**
	 * Gets the SQL statement that performs the match.
	 * @return A string of SQL statements.
	 */
	public String getMatchSql() {
		return match_sql_;
	}
}

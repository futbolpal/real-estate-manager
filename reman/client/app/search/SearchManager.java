package reman.client.app.search;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import reman.common.database.DatabaseManager;
import reman.common.database.DatabaseObject;
import reman.common.database.UserManager;

/**
 * The SearchManager is responsible for correlating a list of SearchResults based on a user's query.
 * At the application's startup, the DatabaseManager examines all classes that are persited in the database.
 * For each of these objects, the DatabaseManager calles the registerClass method so that the SearchManager
 * knows about all classes in the database. Of all these classes that are passed in, the SearchManager will
 * determine which should be searchable, and which should not. Some classes, such as abstract classes do not
 * need to be searched, because there will never be an instance of them stored in the database. Other classes,
 * such as the UserManager class will also not be searchable, because it contains personal user information.
 * If a class is determined as searchable, the SearchManager then builds the queries needed to search the
 * database tables that correspond to that class.
 * @author Will
 *
 */
public class SearchManager {
	private Hashtable<Class, SearchInfo> classes_;
	private Hashtable<Class, Method> format_methods_;
	private static SearchManager this_;
	private final String SEARCH_TABLE_NAME = "search_temp";
	
	/**
	 * Initializes a new SearchManager. The only constructor is private because the SearchManager is a
	 * singleton, there is only one instance for the entire application. The SearchManager is referenced through
	 * SearchManager.instance().
	 */
	private SearchManager() {
		classes_ = new Hashtable<Class, SearchInfo>();
		format_methods_ = new Hashtable<Class, Method>();
	}
	
	/**
	 * Determines whether or not the given class can be searched. A class is searchable if both of the
	 * following conditions hold true:<br>
	 * 1) The class is not abstract.<br>
	 * 2) There exists a static method in ResultManager called format[class name].<br><br>
	 * This method uses reflection to examine the methods of ResultManager at run time in order
	 * to determine if there is a method to format the database output for the specified class.
	 * If no such method exists (or has not been implemented yet) then the class will not be searched,
	 * because there is no method to present the output to the user.
	 * @param c The class to test.
	 * @return True if the class can be searched, false otherwise.
	 */
	public boolean isSearchable(Class c) {
		
		/*a class is searchable if there exists a method
		 *  in ResultManager to format the database output*/
		Class paramTypes[] = new Class[1];
		paramTypes[0] = ResultSet.class;
		Method formatMethod = null;
		try {
			formatMethod = ResultManager.class.getMethod("format" + c.getSimpleName(), paramTypes);
			format_methods_.put(c, formatMethod);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			return false;
		}
		return (formatMethod != null);
	}
	
	/**
	 * This method checks if the given class is searchable, and if so, stores a mapping between the class
	 * and the SQL statements needed to search that class.
	 * @param c A class that extends DatabaseObject (in other words, a type that is persisted in our database).
	 */
	public void registerClass(Class c) {
		if (this.isSearchable(c)) {
			SearchInfo info = new SearchInfo(buildJoinQuery(c), matchQuery(c));
			classes_.put(c, info);
		}
	}
	
	/**
	 * Searches the database for all registered classes in the SearchManager for a given string.
	 * 
	 * @param text The string that the user has specified
	 * @param exact Passing true will search the database exactly as the text appears in the query, casing is ignored.
	 * Passing false will return partial matches for each word in the search query.
	 * @return A list of SearchResult objects, which hold a brief summary of objects that match a
	 * users search query.
	 */
	public ArrayList<SearchResult> search(String text, boolean exact) {
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		Enumeration<Class> e = classes_.keys();
		while(e.hasMoreElements()) {
			Class c = e.nextElement();
			SearchInfo info = classes_.get(c);
			results.addAll(executeSearch(c, info, text, exact));
		}//end while
		return results;
	}
	
	/**
	 * Searches the database tables of a single class for a given string.<br>
	 * @param text The string that the user has specified
	 * @param exact Passing true will search the database exactly as the text appears in the query, casing is ignored.
	 * Passing false will return partial matches for each word in the search query.
	 * @return A list of SearchResult objects, which hold a brief summary of objects that match a
	 * users search query.
	 * @throws NotSearchableException If the specified class is not searchable, an exception is thrown.
	 */
	public ArrayList<SearchResult> search(Class c, String text, boolean exact) throws NotSearchableException {
		ArrayList<Class> classes = new ArrayList<Class>();
		classes.add(c);
		return this.search(classes, text, exact);
	}
	
	/**
	 * Searches the database tables of a subset of the classes that are registered in the SearchManager for a given string.
	 * If a class is passed in that is not searchable, it is ommited from the search.
	 * @param text The string that the user has specified
	 * @param exact Passing true will search the database exactly as the text appears in the query, casing is ignored.
	 * Passing false will return partial matches for each word in the search query.
	 * @return A list of SearchResult objects, which hold a brief summary of objects that match a
	 * users search query.
	 * @throws NotSearchableException If any class specified is not searchable, an exception is thrown.
	 */
	public ArrayList<SearchResult> search(ArrayList<Class> classes, String text, boolean exact) throws NotSearchableException {
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		for(Class c : classes) {
			SearchInfo info = classes_.get(c);
			if (info != null)
				results.addAll(executeSearch(c, info, text, exact));
			else
				throw new NotSearchableException(c);
		}
		return results;
	}
	
	/**
	 * Takes a string that contains the keywords "and," "or," and "not" and returns a string
	 * that is formatted such that MySQL can perform fulltext matching using it.
	 * For example, the string "will or scott not jon" will be formatted as "will scott -jon."<br>
	 * This is used to allow the end user to express his or her query in terms of common, boolean keywords,
	 * rather than symbols.
	 * @param text The search query the user specified.
	 * @param exact Whether or not MySQL should match the words specified exactly. False will format the input
	 * string using the wildcard operator *.
	 * @return The nicely formatted match text that MySQL can use to evaluate the search.
	 */
	private String formatSearchText(String text, boolean exact) {
		text = text.trim().replaceAll(" not ", " -");
		text = text.replaceAll("not ", "-");
		text = text.replaceAll(" or ", " ");
		text = formatAndClause(text);
		
		if (!exact) {
			String wildcard_text = "";
			boolean append = true;
			for(int i = 0; i < text.length(); i++) {
				char current_char = text.charAt(i);
				if (text.charAt(i) == '"') {
					append = !append;
				}
				if ((text.charAt(i) == ' ') && (append) &&
						(text.charAt(i - 1) != '"'))
					/* the string text was trimmed, so i - 1 will not cause out of bounds error */
				{
					wildcard_text += "*";
				}
				wildcard_text += current_char;
			}
			//append last wildcard operator if needed
			if (wildcard_text.charAt(wildcard_text.length() - 1) != '"'){
				wildcard_text += "*";
			}
			
			text = wildcard_text;
		}
		return text;
	}
	
	/**
	 * Formats a string specified by the user that contains the "and" keyword into a format that MySQL
	 * can use to evaluate the search. For example, the text "senior and design" will be translated to
	 * "+senior +design", likewise, search text that contains quotes will be formatted as follows:<br>
	 * Input: "Senior design" and fall<br>
	 * Output: +"Senior design" +fall 
	 * @param text The text that contains the "and" keyword.
	 * @return The nicely formatted match text that MySQL can use to evaluate the search. 
	 */
	private String formatAndClause(String text) {
		text = text.trim();
		int index = text.indexOf(" and ");
		while (index > 0) {
			text = text.replaceFirst(" and ", " +");
			int i = index - 1;
			boolean search = true;
			boolean done = false;
			while ((i >= 0) && (!done)) {
				if (text.charAt(i) == '"') {
					search = ! search;
				} else if ((text.charAt(i) == ' ') && (search) &&
						(text.charAt(i + 1) != '-') && (text.charAt(i + 1) != '+')) {
					text = text.substring(0, i) + " +" + text.substring(i + 1, text.length() - 1);
					done = true;
				}
				i--;
			}
			if (!done && !text.startsWith("+") && !text.startsWith("-"))
				text = "+" + text;
			index = text.indexOf(" and ");
		}
		
		return text;
	}
	
	/**
	 * This method actually performs the search against the database.<br>
	 * First, a SQL join is performed in order to select all of the fields of the class that can be searched.
	 * A temporary table is created with the join output, which is then matched using MySQL fulltext indexing
	 * against the user's query. This temporary table is dropped, and the ResultManager is invoked for any database
	 * output.
	 * @param c The class that is being searched.
	 * @param info The SearchInfo object holds two SQL statements needed to search the given class.
	 * @param text The search query that the user specified.
	 * @param exact Whether or not to match the user's query exactly or to search using the wildcard operator.
	 * @return A list of SearchResult objects, which hold a brief summary of objects that match a
	 * users search query.
	 */
	private ArrayList<SearchResult> executeSearch(Class c, SearchInfo info, String text, boolean exact) {
		String create_table_sql = info.getJoinSql();
		String match_sql = String.format(info.getMatchSql(), formatSearchText(text, exact));
		ResultSet rs = null;
		try {
			//TODO: combine these 3 statements into 1 statement/stored proc
			DatabaseManager.executeUpdate(create_table_sql);
			System.out.println("Match Statement:\n" + match_sql);//debug only
			rs = DatabaseManager.executeQuery(match_sql);
			DatabaseManager.executeUpdate("drop table " + SEARCH_TABLE_NAME);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		if (rs != null) {
			//check how many rows were returned
			int rowCount = 0;
			try {
				rowCount = DatabaseManager.getRowCount(rs);
			} catch (SQLException e2) {}
			
			if (rowCount > 0)
			{
				//lookup the correct method to format the results for given class
				Method formatMethod = format_methods_.get(c);
				try {
					//build the list of search results
					Object returnObj = formatMethod.invoke(null, rs);
					return (ArrayList<SearchResult>)returnObj;
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					System.err.print("Error invoking format method for class " 
							+ c.getName() + ". Exception: ");
					e1.printStackTrace();
				}
			}
		}
		//no results
		return new ArrayList<SearchResult>();
	}
	
	/**
	 * Builds the SQL statement that is used to perform the match for a given class.
	 * @param c The class to build the SQL statements for.
	 * @return SQL match statement.
	 */
	public String matchQuery(Class c) {
		String match_col = getMatchFields(c);
		String sql = "select id,pid,display_name_,version_chain_,modified_last_," + match_col + " from " + SEARCH_TABLE_NAME + 
			" where match (" + match_col + ") against('%s' in boolean mode)";
		return sql;
	}
	
	/**
	 * Builds a SQL statement that joins all of the tables that an object of class c is stored in.
	 * For example, if object C extends B, which extends A, an instance of class C will be stored across three
	 * tables, one for each level of the inheritance hierarchy. This method examines the super classes of c
	 * and builds a join statement for each super class in order to return all of the searchable fields
	 * for class c.
	 * @param c The class to build the join statement for.
	 * @return A SQL statement to join the table that holds c, along with the table(s) of its parent classes.
	 */
	public String buildJoinQuery(Class c) {
		ArrayList<String> columns = new ArrayList<String>();
		ArrayList<String> tables = new ArrayList<String>();
		ArrayList<String> where_clause = new ArrayList<String>();
		//String class_table = DatabaseManager.getTableName(c);
		String dbo_table = DatabaseManager.getTableName(DatabaseObject.class);
		String user_table = DatabaseManager.getTableName(UserManager.class);
		String sql = "create temporary table " + SEARCH_TABLE_NAME + "\nselect %s\nfrom %s\nwhere %s";
		columns.add(String.format("%s.id", dbo_table));
		columns.add(String.format("%s.pid", dbo_table));
		//select the modified date
		columns.add(String.format("%s.modified_last_", dbo_table));
		columns.add(String.format("%s.version_chain_", dbo_table));
		//additional join to grab the user name of the person who modified last
		columns.add(String.format("%s.display_name_", user_table));
		tables.add(user_table);
		where_clause.add(String.format("%s.modifier_id_ = %s.id", dbo_table, user_table));
		
		Class tempClass = c;
		while (tempClass != Object.class) {
			String table_name = DatabaseManager.getTableName(tempClass);
			tables.add(table_name);
			columns.addAll(getSearchableFields(tempClass, table_name));
			//join condition
			if (tempClass.getSuperclass() != Object.class) {
				String parent_table = DatabaseManager.getTableName(tempClass.getSuperclass());
				where_clause.add(table_name + ".pid = " + parent_table + ".id");
			}
			tempClass = tempClass.getSuperclass();
		}
		
		sql = String.format(sql, implode(", ", columns),
				implode(", ", tables),
				implode(" and ", where_clause));
		
		return sql;
	}
	
	/**
	 * Builds a list of database columns that correspond to fields of the given class that are searchable.
	 * Currently, the only fields of a class that are searchable are Strings, so this method returns the database
	 * table and column of the String fields in the given class.<br>
	 * In order to build a complete list, all super classes of c must be examined. For example, if
	 * class B extends A, class B must be examined as well as A in order to find the searchable (String) fields.
	 * @param c The class to examine.
	 * @param table_name The database table that class c is mapped to.
	 * @return A list of database columns.
	 */
	public ArrayList<String> getSearchableFields(Class c, String table_name) {
		ArrayList<String> search_fields_ = new ArrayList<String>();
		for(Field f : c.getDeclaredFields()) {
			//make this better by examining the bindings in the DatabaseManager
			if ((f.getType() == String.class)
					&& (!isExcluded(c, f)))
				search_fields_.add(String.format("%s.%s",table_name, f.getName()));
		}
		
		return search_fields_;
	}
	
	/**
	 * Builds a comma separated list of String fields that can be matched against using a SQL query.
	 * This method must examine all super classes of the specified class in order to retrieve the full
	 * list of String fields that belong to the given class.
	 * @param c The class to examine.
	 * @return A String of field names.
	 */
	public String getMatchFields(Class c) {
		String columns = "";
		Class tempClass = c;
		while(tempClass != Object.class) {
			for(Field f : tempClass.getDeclaredFields()) {
				if ((f.getType() == String.class)
						&& (!isExcluded(tempClass, f)))
					columns += f.getName() + ",";
			}
			tempClass = tempClass.getSuperclass();
		}
		
		return (columns == "") ? columns : columns.substring(0, columns.length() - 1);
	}
	
	/**
	 * Checks whether or not the given field of class c should be searched against.
	 * Only String fields are searched, and special cases, such as the version chain field
	 * of DatabaseObject are excluded from searching.
	 * @param c
	 * @param f
	 * @return
	 */
	public boolean isExcluded(Class c, Field f) {
		if ((c == DatabaseObject.class) && (((f.getName() == "version_chain_")) 
				|| ((f.getName() == "declared_class_"))))
			return true;
		else
			return false;
	}
	
	/**
	 * Loops over a list of objects, calling their toString() method and concatenates each element
	 * with the the input String. Example:<br>
	 * Input Array:<br>
	 * Element1<br>
	 * Element2<br>
	 * Element3<br>
	 * Glue: ", "<br>
	 * Output: "Element1, Element2, Element3"
	 * @param glue The String to concatenate with each element of the list.
	 * @param array The list to examine.
	 * @return A String representation of each element of the list.
	 */
	public String implode(String glue, ArrayList<?> array) {
		String result = "";
		for (int i = 0; i < array.size(); i++) {
			result += array.get(i).toString();
			if (i < array.size() - 1)
				result += glue;
		}
		return result;
	}
	
	/**
	 * Returns an enumeration of the classes that have been registered with the SearchManager.
	 * @return
	 */
	public Enumeration<Class> getClasses() {
		return classes_.keys();
	}
	
	/**
	 * The SearchManager is a singleton class, meaning there is only one instance of the SearchManager for the application.
	 * This method will return the current instance or create a new one if needed.
	 * @return The current instance of the SearchManager for use by all application components.
	 */
	public static SearchManager instance() {
		if (this_ == null)
			this_ = new SearchManager();
		return this_;
	}
	
}

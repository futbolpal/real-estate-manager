package test_files;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import reman.client.app.search.SearchManager;
import reman.client.app.search.Searchable;
import reman.common.database.DatabaseManager;
import reman.common.database.DatabaseObject;
import reman.common.database.UserManager;

public class SearchTest {

	private final static String SEARCH_TABLE_NAME = "search_temp";
	
	public static void main(String args[]) throws ClassNotFoundException {
		Class c = null;
		try {
		//c = Class.forName("reman.client.basetypes.Agent");
			c = Class.forName("reman.client.app.office_maintenance.Task");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (c != null) {
			System.out.println("Examining class " + c.getSimpleName() + "\nFields:");
			
			for(Field f : c.getDeclaredFields()) {
				System.out.println("Name: " + f.getName() + " Type: " + f.getType());
			}
			
			System.out.println("\nClass Hierarchy:");
			Class tempClass = Class.forName(c.getName());
			while (tempClass != Object.class) {
				System.out.println(tempClass.getName());
				tempClass = tempClass.getSuperclass();
			}
			
			System.out.println(buildJoinQuery(c));
		}
	}
	
	public static String buildJoinQuery(Class c) {
		ArrayList<String> columns = new ArrayList<String>();
		ArrayList<String> tables = new ArrayList<String>();
		ArrayList<String> where_clause = new ArrayList<String>();
		String class_table = DatabaseManager.getTableName(c);
		String dbo_table = DatabaseManager.getTableName(DatabaseObject.class);
		String user_table = DatabaseManager.getTableName(UserManager.class);
		String sql = "create temporary table " + SEARCH_TABLE_NAME + "\nselect %s\nfrom %s\nwhere %s";
		columns.add(String.format("%s.id", class_table));
		columns.add(String.format("%s.pid", class_table));
		//select the modified date
		columns.add(String.format("%s.modified_last_", dbo_table));
		//additional join to grab the user name of the person who modified last
		columns.add(String.format("%s.display_name_ 'modifier_name'", user_table));
		tables.add(user_table);
		where_clause.add(String.format("%s.modifier_id_ = %s.id", dbo_table, user_table));
		
		Class tempClass = null;
		try {
			tempClass = Class.forName(c.getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
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
		//TODO: add any other fields of the databaseobject table to the columns array
		
		sql = String.format(sql, implode(", ", columns),
				implode(", ", tables),
				implode(" and ", where_clause));
		
		return sql;
	}
	
	public static ArrayList<String> getSearchableFields(Class c, String table_name) {
		ArrayList<String> search_fields_ = new ArrayList<String>();
		for(Field f : c.getDeclaredFields()) {
			//make this better by examining the bindings in the DatabaseManager
			if ((f.getType() == String.class)
					&& (!isExcluded(c, f)))
				search_fields_.add(String.format("%s.%s",table_name, f.getName()));
		}
		
		return search_fields_;
	}
	
	public static String getMatchFields(Class c) {
		String columns = "";
		for(Field f : c.getDeclaredFields()) {
			if ((f.getType() == String.class)
					&& (!isExcluded(c, f)))
				columns += f.getName() + ",";
		}
		return columns.substring(0, columns.length() - 2);
	}
	
	public static boolean isExcluded(Class c, Field f) {
		if ((c == DatabaseObject.class) && (f.getName() == "version_chain_"))
			return true;
		else
			return false;
	}
	
	public static String implode(String glue, ArrayList<String> array) {
		String result = "";
		for (int i = 0; i < array.size(); i++) {
			result += array.get(i);
			if (i < array.size() - 1)
				result += glue;
		}
		return result;
	}
}

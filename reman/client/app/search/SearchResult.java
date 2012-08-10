package reman.client.app.search;

import java.sql.Timestamp;

/**
 * The SearchResult class holds information about an object that is persisted in the database.
 * This is used to present the end user a table which contains the type of object that was found,
 * a brief description of the object, the name of the user who last modified the object, along with
 * the date and time the object was committed to the database.<br>
 * All Classes that are searched will build a list of SearchResult objects, which are then displayed
 * in a grid to the end user.<br>
 * This class is used to display a summary of the objects that matched the user's query. 
 * @author Will
 *
 */
public class SearchResult {
	private String name_, type_, modified_by_, version_chain_;
	private Long id_, pid_;
	private Class class_;
	private Timestamp modified_date_;
	
	public SearchResult(Class c, String type, Long id, Long pid, String description,
			Timestamp date, String modified_by, String version_chain) {
		name_ = description;
		type_ = type;
		id_ = id;
		pid_ = pid;
		class_ = c;
		modified_date_ = date;
		modified_by_ = modified_by;
		version_chain_ = version_chain;
	}
	
	/**
	 * Gets the class to which this SearchResult corresponds to.
	 * @return 
	 */
	public Class getResultClass() {
		return class_;
	}
	
	/**
	 * Gets the display name of the class that was searched.
	 * @return
	 */
	public String getName() {
		return name_;
	}
	
	/**
	 * Gets the user friendly name of the class that was searched.
	 * For example, if the application has searched a class called "OfficeProjectMaintenanceTask",
	 * this method will return a user friendly string that is predefined, such as "Maintenance Task."
	 * @return
	 */
	public String getType(){
		return type_;
	}
	
	/**
	 * Gets the database id of the object that was searched. This is needed to retrieve the actual
	 * object from the database, in the event the user would like more information about the object
	 * that was found.
	 * @return The database id of the object that was found.
	 */
	public Long getId() {
		return id_;
	}
	
	/**
	 * Gets the parent id of the object that was searched. This is needed to retrieve the actual
	 * object from the database, in the event the user would like more information about the object
	 * that was found.
	 * @return The parent id of the object that was found. This value is -1 if there is no parent object.
	 */
	public Long getPid() {
		return pid_;
	}
	
	/**
	 * Gets the display name of the user who last modified the corresponding database object.
	 * @return The user's display name.
	 */
	public String getModifiedBy() {
		return modified_by_;
	}
	
	/**
	 * Gets the date that the database object was last modified.
	 * @return The date of last modification.
	 */
	public Timestamp getModifiedDate() {
		return modified_date_;
	}
	
	public String getVersionChain() {
		return version_chain_;
	}
}

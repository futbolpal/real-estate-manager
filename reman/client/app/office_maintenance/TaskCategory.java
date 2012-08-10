package reman.client.app.office_maintenance;

import reman.client.app.preferences.GlobalPreference;

/**
 * A task category is simply as it sounds, a category for a task.  It is just another
 * way of classifying tasks.  This class does not have any extra fields from DBO since 
 * the "name" field of DBO is used as the name of the category.
 * @author jonathan
 */
public class TaskCategory extends GlobalPreference {
	public TaskCategory(String name) {
		this.setName(name);
	}
}

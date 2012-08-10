package reman.client.app.office_maintenance;

import java.sql.Timestamp;
import java.text.DateFormat;

/**
 * This class represents the base class for a Task.  The tasking system
 * is modeled after Bug Tracking systems such as Mantis or Bugzilla. 
 * <BR><BR>
 * This class contains the base members that all tasks should need.  Tasks can have 
 * attachments and can therefore be "stuck" to other database objects like sticky notes.
 * 
 * @author jonathan
 *
 */
public class Task extends CalendarItem {
	public static enum TaskPriority {
		NONE, LOW, NORMAL, HIGH, URGENT, IMMEDIATE;
	}

	private TaskPriority priority_;
	private int percent_complete_;
	private Timestamp due_;
	private TaskCategory category_;
	private long assignee_;

	public Task() {
		this("New Task");
	}

	public Task(String name) {
		super(name);
		percent_complete_ = 0;
		priority_ = TaskPriority.NONE;
		due_ = new Timestamp(System.currentTimeMillis());
	}

	public void setDueDate(Timestamp d) {
		due_ = d;
	}

	public Timestamp getDueDate() {
		return due_;
	}

	public void setPercentComplete(int p) {
		this.percent_complete_ = p;
	}

	public int getPercentComplete() {
		return this.percent_complete_;
	}

	public void setPriority(TaskPriority p) {
		priority_ = p;
	}

	public TaskPriority getPriority() {
		return priority_;
	}

	public TaskCategory getCategory() {
		return category_;
	}

	public void setCategory(TaskCategory c) {
		category_ = c;
	}

	public String getQuickDescription() {
		String desc = "<html>";
		desc += "<b>Name:</b>" + this.getName() + "<BR>";
		desc += "<b>Priority:</b>" + this.getPriority() + "<BR>";
		desc += "<b>Category:</b>" + this.getCategory() + "<BR>";
		desc += "<b>Due:</b>"
				+ DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(
						this.getDueDate()) + "<BR>";
		desc += "<b>Progress:</b>" + this.getPercentComplete() + "%<BR>";
		desc += "</html>";
		return desc;
	}

	/**
	 * Returns the user id of the person assigned to the task
	 * @return long - the id of the user assigned to the task
	 */
	public long getAssignee() {
		return assignee_;
	}

	/**
	 * Assigns this task to a person
	 * @param id
	 */
	public void setAssignee(long id) {
		assignee_ = id;
	}

	public boolean isExpired() {
		return !(System.currentTimeMillis() < due_.getTime());
	}
}

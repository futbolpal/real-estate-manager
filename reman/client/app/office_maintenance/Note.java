package reman.client.app.office_maintenance;

import java.sql.Timestamp;

import reman.common.database.DatabaseObject;
import reman.common.database.UserManager;

/**
 * A Note is a simple object with text.  Notes are helpful
 * informal reminders.  These notes are global notes that 
 * are shared with all users in a project
 * @author jonathan
 */
public class Note extends DatabaseObject {
	private long created_by_;
	private Timestamp created_on_;
	private String note_;

	public Note() {
		this("");
	}

	public Note(String note) {
		created_by_ = UserManager.instance().getCurrentUserID();
		created_on_ = new Timestamp(System.currentTimeMillis());
		note_ = note;
	}

	/**
	 * Sets the text value of the note
	 * @param note
	 */
	public void setNote(String note) {
		note_ = note;
	}

	/**
	 * @return long - the user id of the note creator
	 */
	public long getCreator() {
		return created_by_;
	}

	/**
	 * @return Timestamp - the time the note was created
	 */
	public Timestamp getCreatedDate() {
		return created_on_;
	}

	/**
	 * Returns the value of the note
	 * @return
	 */
	public String getNote() {
		return note_;
	}

}

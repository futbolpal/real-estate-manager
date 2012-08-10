package reman.client.app.office_maintenance;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import reman.client.app.finance.timer.TimeEventManager;
import reman.common.database.DatabaseObject;
import reman.common.database.UserManager;
import reman.common.database.exceptions.DatabaseException;

public abstract class CalendarItem extends DatabaseObject {
	private Timestamp created_on_;
	private long created_by_;
	private ArrayList<DatabaseObject> attachments_;
	private CalendarItemReminder reminder_;

	public CalendarItem(String name) {
		this.setName(name);
		created_by_ = UserManager.instance().getCurrentUserID();
		created_on_ = new Timestamp(System.currentTimeMillis());
		attachments_ = new ArrayList<DatabaseObject>();
	}

	public void setReminder(CalendarItemReminder r) {
		try {
			TimeEventManager.instance().unRegister(reminder_);
			reminder_ = r;
			TimeEventManager.instance().register(reminder_);
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public CalendarItemReminder getReminder() {
		return reminder_;
	}

	public long getCreator() {
		return created_by_;
	}

	public Timestamp getCreatedDate() {
		return created_on_;
	}

	public void removeAttachment(DatabaseObject o) {
		attachments_.remove(o);
	}

	public void addAttachment(DatabaseObject o) {
		attachments_.add(o);
	}

	public void addNote(Note n) {
		this.addAttachment(n);
	}

	public ArrayList<Note> getNotes() {
		ArrayList<Note> ret = new ArrayList<Note>();
		for (DatabaseObject o : attachments_) {
			if (o instanceof Note)
				ret.add((Note) o);
		}
		return ret;
	}
}

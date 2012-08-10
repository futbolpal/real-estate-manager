package reman.client.app.office_maintenance;

import java.sql.SQLException;
import java.sql.Timestamp;

import reman.client.app.finance.timer.TimeNotificationEntry;
import reman.common.database.OfficeProjectManager;
import reman.common.database.exceptions.DatabaseException;

public class CalendarItemReminder extends TimeNotificationEntry {

	private CalendarItem event_;

	public CalendarItemReminder(CalendarItem item, Timestamp time) {
		super(OfficeMaintenanceManager.instance(), time);
		event_ = item;
		try {
			OfficeProjectManager.instance().getCurrentProject().getTimeEventManager().register(this);
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public CalendarItem getEvent() {
		return event_;
	}

}

package reman.client.app.office_maintenance;

public interface ScheduleListener {
	public void calendarItemAdded(CalendarItem c);
	public void calendarItemRemoved(CalendarItem c);
	public void calendarItemChanged(CalendarItem c);
}

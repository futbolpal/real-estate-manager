package reman.client.app.office_maintenance;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

import reman.client.basetypes.ITimeBoundable;
import reman.client.basetypes.Location;
import reman.client.basetypes.Person;
import reman.client.basetypes.TimeRange;

public class Meeting extends CalendarItem implements ITimeBoundable {
	private TimeRange time_;
	private Location location_;
	private boolean all_day_;
	private ArrayList<Person> attendees_;

	public Meeting() {
		this("New Meeting");
	}

	public Meeting(String name) {
		this(name, new Timestamp(System.currentTimeMillis()));
	}

	public Meeting(String name, Timestamp start) {
		super(name);
		time_ = new TimeRange(start, start);
		location_ = new Location();
		all_day_ = false;
		attendees_ = new ArrayList<Person>();
	}

	public void setAllDay(boolean flag) {
		all_day_ = flag;
	}

	public boolean isAllDay() {
		return all_day_;
	}

	public void addAttendee(Person p) {
		attendees_.add(p);
	}

	public void removeAttendee(Person p) {
		attendees_.remove(p);
	}

	@Override
	public TimeRange getEffectiveTimeRange() {
		return time_;
	}

	@Override
	public void setEffectiveTimeRange(TimeRange range) {
		time_ = range;
	}

	public void setLocation(Location l) {
		location_ = l;
	}

	public Location getLocation() {
		return location_;
	}

	public void setAttendees(ArrayList<Person> a) {
		this.attendees_ = a;
	}

	public ArrayList<Person> getAttendees() {
		return this.attendees_;
	}

	public Calendar getStart() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis((this.getEffectiveTimeRange().getBegin().getTime()));
		return c;
	}

	public Calendar getEnd() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis((this.getEffectiveTimeRange().getEnd().getTime()));
		return c;
	}

	public void setStart(Calendar start) {
		this.getEffectiveTimeRange().setBegin(new Timestamp(start.getTimeInMillis()));
	}

	public void setEnd(Calendar end) {
		this.getEffectiveTimeRange().setEnd(new Timestamp(end.getTimeInMillis()));
	}
}

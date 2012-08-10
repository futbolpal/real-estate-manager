package reman.client.app.office_maintenance;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import reman.client.app.Framework;
import reman.client.app.finance.timer.TimeEvent;
import reman.client.app.finance.timer.TimeListener;
import reman.client.app.listeners.DboChangedListener;
import reman.client.basetypes.TimeRange;
import reman.common.database.DatabaseObject;
import reman.common.database.OfficeProjectManager;
import reman.common.database.exceptions.DatabaseException;
import reman.common.messaging.DboChangedMessage;

public class OfficeMaintenanceManager extends DatabaseObject implements DboChangedListener,
		TimeListener {
	private transient ArrayList<ScheduleListener> listeners_;

	private ArrayList<Meeting> meetings_;
	private ArrayList<Task> tasks_;
	private Mailbox mail_;
	private boolean auto_delete_tasks_;

	public OfficeMaintenanceManager() {
		mail_ = new Mailbox();
		meetings_ = new ArrayList<Meeting>();
		tasks_ = new ArrayList<Task>();
		listeners_ = new ArrayList<ScheduleListener>();
		auto_delete_tasks_ = false;
		Framework.instance().addDboChangedListener(this);
	}

	public ArrayList<Meeting> getMeetings() {
		return meetings_;
	}

	public ArrayList<Task> getTasks() {
		return tasks_;
	}

	public void addMeeting(Meeting m) {
		meetings_.add(m);
		this.fireCalendarItemAdded(m);
	}

	public void addTask(Task t) {
		tasks_.add(t);
		this.fireCalendarItemAdded(t);
	}

	public void removeTask(Task t) {
		tasks_.remove(t);
		this.fireCalendarItemRemoved(t);
	}

	public void removeMeeting(Meeting m) {
		meetings_.remove(m);
		this.fireCalendarItemRemoved(m);
	}

	public void addScheduleListener(ScheduleListener l) {
		listeners_.add(l);
	}

	public void removeCalendarEventListener(ScheduleListener l) {
		listeners_.remove(l);
	}

	public Mailbox getMailbox() {
		return mail_;
	}

	public void setAutoDelete(boolean flag) {
		this.auto_delete_tasks_ = flag;
	}

	private void fireCalendarItemRemoved(CalendarItem i) {
		try {
			this.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		synchronized (listeners_) {
			for (ScheduleListener l : listeners_) {
				l.calendarItemRemoved(i);
			}
		}
	}

	private void fireCalendarItemAdded(CalendarItem i) {
		try {
			this.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		synchronized (listeners_) {
			for (ScheduleListener l : listeners_) {
				l.calendarItemAdded(i);
			}
		}
	}

	public void fireCalendarItemChanged(CalendarItem i) {
		synchronized (listeners_) {
			for (ScheduleListener l : listeners_) {
				l.calendarItemChanged(i);
			}
		}
	}

	public ArrayList<Meeting> getOverlappingMeetings(Meeting m) {
		long ms = m.getStart().getTimeInMillis() / 1000000;
		long me = m.getEnd().getTimeInMillis() / 1000000;
		ArrayList<Meeting> during = new ArrayList<Meeting>();
		for (Meeting n : this.getMeetingsOnDay(m.getStart())) {
			long ns = n.getStart().getTimeInMillis() / 1000000;
			long ne = n.getEnd().getTimeInMillis() / 1000000;
			if (ns >= ms && ns < me)
				during.add(n);
			else if (ms >= ns && ms < ne)
				during.add(n);
		}
		return during;
	}

	public ArrayList<Meeting> getMeetingsBetween(Calendar a, Calendar b) {
		ArrayList<Meeting> during = new ArrayList<Meeting>();
		for (Meeting m : this.getMeetingsOnDay(a)) {
			long c = a.getTimeInMillis() / 1000000;
			long d = b.getTimeInMillis() / 1000000;
			long s = m.getStart().getTimeInMillis() / 1000000;
			if (s >= c && s <= d)
				during.add(m);
		}
		return during;
	}

	public ArrayList<Meeting> getMeetingsAtTime(Calendar time) {
		ArrayList<Meeting> during = new ArrayList<Meeting>();
		for (Meeting m : this.getMeetingsOnDay(time)) {
			long c = time.getTimeInMillis() / 1000000;
			long s = m.getEffectiveTimeRange().getBegin().getTime() / 1000000;
			long e = m.getEffectiveTimeRange().getEnd().getTime() / 1000000;
			System.out.println(c + ":" + s + ":" + e);
			if (s <= c && e > c)
				during.add(m);
		}
		return during;
	}

	public ArrayList<Meeting> getMeetingsOnDay(Calendar day) {
		ArrayList<Meeting> during = new ArrayList<Meeting>();
		for (Meeting m : this.getMeetingsOnWeek(day)) {
			Calendar s = Calendar.getInstance();
			s.setTimeInMillis(m.getEffectiveTimeRange().getBegin().getTime());
			Calendar e = Calendar.getInstance();
			e.setTimeInMillis(m.getEffectiveTimeRange().getEnd().getTime());
			if (s.get(Calendar.DAY_OF_YEAR) <= day.get(Calendar.DAY_OF_YEAR)
					&& e.get(Calendar.DAY_OF_YEAR) >= day.get(Calendar.DAY_OF_YEAR))
				during.add(m);
		}
		return during;
	}

	public ArrayList<Meeting> getMeetingsOnWeek(Calendar week) {
		ArrayList<Meeting> during = new ArrayList<Meeting>();
		for (Meeting m : this.getMeetingsOnMonth(week)) {
			Calendar s = Calendar.getInstance();
			s.setTimeInMillis(m.getEffectiveTimeRange().getBegin().getTime());
			if (s.get(Calendar.WEEK_OF_YEAR) == week.get(Calendar.WEEK_OF_YEAR))
				during.add(m);
		}
		return during;
	}

	public ArrayList<Meeting> getMeetingsOnMonth(Calendar month) {
		ArrayList<Meeting> during = new ArrayList<Meeting>();
		for (Meeting m : meetings_) {
			Calendar s = Calendar.getInstance();
			TimeRange time = m.getEffectiveTimeRange();
			if (time != null && time.getBegin() != null)
				s.setTimeInMillis(m.getEffectiveTimeRange().getBegin().getTime());
			if (s.get(Calendar.MONTH) == month.get(Calendar.MONTH))
				during.add(m);
		}
		return during;
	}

	public ArrayList<Task> getTasksDueOnDay(Calendar day) {
		ArrayList<Task> due = new ArrayList<Task>();
		for (Task t : this.getTasksDueOnWeek(day)) {
			Calendar d = Calendar.getInstance();
			d.setTimeInMillis(t.getDueDate().getTime());
			if (d.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)) {
				due.add(t);
			}
		}
		return due;
	}

	public ArrayList<Task> getTasksDueOnWeek(Calendar week) {
		ArrayList<Task> due = new ArrayList<Task>();
		for (Task t : this.getTasksDueOnMonth(week)) {
			Calendar d = Calendar.getInstance();
			d.setTimeInMillis(t.getDueDate().getTime());
			if (d.get(Calendar.WEEK_OF_YEAR) == week.get(Calendar.WEEK_OF_YEAR)) {
				due.add(t);
			}
		}
		return due;
	}

	public ArrayList<Task> getTasksDueOnMonth(Calendar month) {
		ArrayList<Task> due = new ArrayList<Task>();
		for (Task t : tasks_) {
			Calendar d = Calendar.getInstance();
			d.setTimeInMillis(t.getDueDate().getTime());
			if (d.get(Calendar.MONTH) == month.get(Calendar.MONTH)) {
				due.add(t);
			}
		}
		return due;
	}

	public static OfficeMaintenanceManager instance() {
		return OfficeProjectManager.instance().getCurrentProject().getOfficeMaintenanceManager();
	}

	@Override
	public void dboChangedEvent(DboChangedMessage m) {
		if (m.getDatabaseObject() instanceof CalendarItem) {
			this.fireCalendarItemChanged((CalendarItem) m.getDatabaseObject());
		}
	}

	public void retrieve() throws DatabaseException, SQLException {
		super.retrieve();
		synchronized (tasks_) {
			ArrayList<Task> temp = this.getUnexpiredTasks();
			tasks_.clear();
			tasks_.addAll(temp);
		}
	}

	public void timeEventOccurred(TimeEvent e) {

	}

	public ArrayList<Task> getTasksAssignedTo(long uid) {
		ArrayList<Task> ret = new ArrayList<Task>();
		for (Task t : this.getUnexpiredTasks()) {
			if (t.getAssignee() == uid)
				ret.add(t);
		}
		return ret;
	}

	public ArrayList<Task> getUnexpiredTasks() {
		ArrayList<Task> ret = new ArrayList<Task>();
		for (Task t : tasks_) {
			if (!t.isExpired())
				ret.add(t);
		}
		return ret;
	}

}

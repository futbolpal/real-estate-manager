package reman.client.app.finance.timer;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.TimerTask;

import reman.client.basetypes.TimeRange;
import reman.common.database.DatabaseObject;
import reman.common.database.OfficeProjectManager;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.LoggedInException;

public class TimeNotificationEntry extends DatabaseObject {
	private TimeRange notification_duration_;
	private TimeListener subscriber_;
	private boolean indefinite_;
	private int num_events_;
	private transient TimerTaskSupport timer_task_;

	private TimeNotificationEntry() {
		this.timer_task_ = new TimerTaskSupport();
	}
	
	public TimeNotificationEntry(TimeListener obj, Timestamp notify_time) {
		this(obj, notify_time, 1);
	}

	public TimeNotificationEntry(TimeListener obj, Timestamp notify_time, int num_events_) {
		this(obj, notify_time, false, 1);
	}

	public TimeNotificationEntry(TimeListener obj, Timestamp notify_time, boolean indefinite_repeate) {
		this(obj, notify_time, indefinite_repeate, 1);
	}

	private TimeNotificationEntry(TimeListener obj, Timestamp notify_time,
			boolean indefinite_repeate, int num_events) {
		this();
		Timestamp now = new Timestamp((new Date()).getTime());
		if (now.compareTo(notify_time) > 0)
			swap(now, notify_time);
		this.notification_duration_ = new TimeRange(now, notify_time);
		this.subscriber_ = obj;
		this.indefinite_ = indefinite_repeate;
		this.num_events_ = num_events;
	}

	private void swap(Timestamp t1, Timestamp t2) {
		Timestamp temp = t1;
		t1 = t2;
		t2 = temp;
	}
	
	public TimeListener getSubscriber() {
		return this.subscriber_;
	}

	public Timestamp getNotifyTime() {
		return this.notification_duration_.getEnd();
	}

	public TimeRange getRange() {
		return this.notification_duration_;
	}

	public void decEventsLeft() {
		this.num_events_--;
	}

	public int getEventsLeft() {
		return this.num_events_;
	}

	public boolean isIndefinite() {
		return this.indefinite_;
	}

	public TimerTaskSupport getTimerTask() {
		return this.timer_task_;
	}

	protected class TimerTaskSupport extends TimerTask {
		@Override
		public void run() {
			expireEntry(TimeNotificationEntry.this);
			//this.cancel(); <- done by timer manager
		}

		private void expireEntry(TimeNotificationEntry e) {
			fireTimeEvent(new TimeEvent(new Timestamp((new Date()).getTime()), e));
			try {
				OfficeProjectManager.instance().getCurrentProject().getTimeEventManager().unRegister(e,
						false);
				e.decEventsLeft();
				if (e.isIndefinite() || e.getEventsLeft() > 0) {
					/*add a new entry at same duration away from now*/
					long duration = e.getRange().getDuration();
					Timestamp next_notify = new Timestamp((new Date()).getTime() + duration);
					TimeNotificationEntry new_entry = new TimeNotificationEntry(e.getSubscriber(),
							next_notify, e.isIndefinite(), e.getEventsLeft());
					OfficeProjectManager.instance().getCurrentProject().getTimeEventManager().register(
							new_entry, false);
				}

				OfficeProjectManager.instance().getCurrentProject().getTimeEventManager().synchManager();
			} catch (LoggedInException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (DatabaseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

		private void fireTimeEvent(TimeEvent e) {
			e.getEntry().getSubscriber().timeEventOccurred(e);
		}
	}
}

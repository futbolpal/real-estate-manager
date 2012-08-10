package reman.client.app.finance.timer;

import java.sql.Timestamp;

public class TimeEvent {
	private Timestamp fired_time_;
	private TimeNotificationEntry original_entry_;

	public TimeEvent(Timestamp fire_time, TimeNotificationEntry original_entry_) {
		this.fired_time_ = fire_time;
		this.original_entry_ = original_entry_;
	}

	public boolean isExpireNotification() {
		return this.fired_time_.compareTo(this.original_entry_.getNotifyTime()) > 0;
	}

	public Timestamp getFiredTime() {
		return this.fired_time_;
	}

	public TimeNotificationEntry getEntry() {
		return this.original_entry_;
	}

}

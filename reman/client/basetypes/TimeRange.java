package reman.client.basetypes;

import java.sql.Timestamp;
import java.util.Date;

import reman.common.database.DatabaseObject;

public class TimeRange extends DatabaseObject {
	public static enum TimeRangeType {
		DATE, DATE_TIME;
	}

	private Timestamp begin_;
	private Timestamp end_;
	private TimeRangeType type_;

	public TimeRange() {
		/*used for DBO construction*/
		this(new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
	}

	public TimeRange(Timestamp begin, Timestamp end) {
		this.setBegin(begin);
		this.setEnd(end);
	}

	public TimeRange(Timestamp begin, Timestamp end, TimeRangeType type) {
		this.setBegin(begin);
		this.setEnd(end);
	}

	public boolean setBegin(Timestamp begin) {
		//if end_ has not been initilized or the parameter value is <= existing end_, valid begin
		if (end_ == null || begin.compareTo(end_) <= 0) {
			begin_ = begin;
			return true;
		} else {
			return false;
		}
	}

	public boolean setEnd(Timestamp end) {
		//if begin_ is not initilized or parameter end is >= existing begin_, valid end
		if (begin_ == null || end.compareTo(begin_) >= 0) {
			end_ = end;
			return true;
		} else {
			return false;
		}
	}

	public long getDuration() {
		return this.end_.getTime() - this.getBegin().getTime();
	}

	public void setType(TimeRangeType type) {
		type_ = type;
	}

	public Timestamp getBegin() {
		return this.begin_;
	}

	public Timestamp getEnd() {
		return this.end_;
	}

	public TimeRangeType getType() {
		return this.type_;
	}

	public boolean isActive() {
		return this.isInRange(new Timestamp((new Date()).getTime()));
	}

	public boolean isInRange(Timestamp val) {
		return this.begin_.compareTo(val) <= 0 && this.end_.compareTo(val) >= 0;
	}

	public boolean isInRange(TimeRange val) {
		return isInRange(val.begin_) && isInRange(val.end_);
	}

	public boolean isBefore(Timestamp val) {
		return this.end_.compareTo(val) <= 0;
	}

	public boolean isBefore(TimeRange val) {
		return isBefore(val.begin_);
	}

	public boolean isAfter(Timestamp val) {
		return this.begin_.compareTo(val) >= 0;
	}

	public boolean isAfter(TimeRange val) {
		return isAfter(val.end_);
	}

	public boolean startsBefore(Timestamp val) {
		return this.begin_.compareTo(val) <= 0;
	}

	public boolean startsBefore(TimeRange val) {
		return startsBefore(val.begin_);
	}

	public boolean endsAfter(Timestamp val) {
		return (this.end_.compareTo(val) >= 0);
	}

	public boolean endsAfter(TimeRange val) {
		return endsAfter(val.end_);
	}
}

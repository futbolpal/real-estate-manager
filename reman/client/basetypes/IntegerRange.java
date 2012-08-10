package reman.client.basetypes;

import reman.common.database.DatabaseObject;

/**
 * No generics means that a range must be made for each type
 * @author Scott
 *
 */
public class IntegerRange extends DatabaseObject implements Comparable<IntegerRange> {
	private Integer begin_ = null;
	private Integer end_ = null;

	private IntegerRange() {
		/*used for DBO construction*/
	}

	public IntegerRange(Integer begin, Integer end) {
		this.setBegin(begin);
		this.setEnd(end);
	}

	public void setBegin(Integer begin) {
		if (begin == null) {
			System.err
					.println("'begin' param passed as 'null' in 'IntegerRange' class 'setBegin()' method. Obj="
							+ this.toString());
			return;
		}

		//if end_ has not been initilized or the parameter value is <= existing end_, valid begin
		if (end_ == null || begin.compareTo(end_) <= 0)
			begin_ = begin;
		else {
			//IntegerODO: log or throw exception
			//error, begin parameter is greater than end_
		}
	}

	public void setEnd(Integer end) {
		if (end == null) {
			System.err
					.println("'end' param passed as 'null' in 'IntegerRange' class 'setEnd()' method. Obj="
							+ this.toString());
			return;
		}

		//if begin_ is not initilized or parameter end is >= existing begin_, valid end
		if (begin_ == null || end.compareTo(begin_) >= 0)
			end_ = end;
		else {
			//IntegerODO: log or throw exception
			//error, end parameter is less than begin_
		}
	}

	public Integer getBegin() {
		return this.begin_;
	}

	public Integer getEnd() {
		return this.end_;
	}

	public boolean isInRange(Integer val) {
		return this.isInRange(val, val);
	}

	public boolean isInRange(Integer begin, Integer end) {
		return this.isInRange(new IntegerRange(begin, end));
	}

	public boolean isInRange(IntegerRange val) {
		if (this.begin_.compareTo(val.getEnd()) <= 0
				&& this.end_.compareTo(val.getBegin()) >= 0)
			return true;
		return false;
	}

	public boolean isBefore(Integer val) {
		return this.isBefore(val, val);
	}

	public boolean isBefore(Integer begin, Integer end) {
		return this.isBefore(new IntegerRange(begin, end));
	}

	public boolean isBefore(IntegerRange val) {
		if (this.end_.compareTo(val.getBegin()) <= 0)
			return true;
		return false;
	}

	public boolean isAfter(Integer val) {
		return this.isAfter(val, val);
	}

	public boolean isAfter(Integer begin, Integer end) {
		return this.isAfter(new IntegerRange(begin, end));
	}

	public boolean isAfter(IntegerRange val) {
		if (this.begin_.compareTo(val.getEnd()) >= 0)
			return true;
		return false;
	}

	public boolean startsBefore(Integer val) {
		return this.startsBefore(val, val);
	}

	public boolean startsBefore(Integer begin, Integer end) {
		return this.startsBefore(new IntegerRange(begin, end));
	}

	public boolean startsBefore(IntegerRange val) {
		if (this.begin_.compareTo(val.getBegin()) <= 0)
			return true;
		return false;
	}

	public boolean endsAfter(Integer val) {
		return this.endsAfter(val, val);
	}

	public boolean endsAfter(Integer begin, Integer end) {
		return this.endsAfter(new IntegerRange(begin, end));
	}

	public boolean endsAfter(IntegerRange val) {
		if (this.end_.compareTo(val.getEnd()) >= 0)
			return true;
		return false;
	}

	@Override
	public int compareTo(IntegerRange o) {
		return this.begin_-o.getBegin();
	}
}
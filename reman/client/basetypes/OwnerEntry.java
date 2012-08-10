package reman.client.basetypes;

import reman.client.gui.forms.DboFormPanel;
import reman.client.gui.forms.OwnersEntryFormPanel;
import reman.common.database.DatabaseObject;

public class OwnerEntry extends DatabaseObject implements ITimeBoundable {

	private TimeRange time_range_;
	private BusinessEntity owner_;

	public OwnerEntry() {
		this.time_range_ = new TimeRange();
		/*TODO: what to initilize owner_ to?*/
		this.owner_ = null;
	}

	public TimeRange getEffectiveTimeRange() {
		return this.time_range_;
	}

	public void setEffectiveTimeRange(TimeRange range) {
		this.time_range_ = range;
	}

	public BusinessEntity getOwner() {
		return owner_;
	}
}

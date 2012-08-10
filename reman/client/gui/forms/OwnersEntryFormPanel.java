package reman.client.gui.forms;

import reman.client.basetypes.BusinessEntity;
import reman.client.basetypes.OwnerEntry;
import reman.client.gui.TimeRangeField;

public class OwnersEntryFormPanel extends DboFormPanel<OwnerEntry> {
	private TimeRangeField time_range_;
	private BusinessEntityFormPanel owner_;

	public OwnersEntryFormPanel(OwnerEntry o, DboFormPanel<?> parent, boolean read_only) {
		super("Owner", o, parent, read_only);

		this.addFormItem(time_range_ = new TimeRangeField("Time Period", o.getEffectiveTimeRange(),
				parent, read_only));
		this.addFormItem(owner_ = new BusinessEntityFormPanel(o.getOwner(), this, this.read_only_));
	}

	public void retrieveForm() {
		time_range_.retrieveForm();
		owner_.retrieveForm();
	}

	public void updateForm() {
		time_range_.updateForm();
		owner_.updateForm();
	}

}

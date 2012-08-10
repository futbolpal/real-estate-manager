package reman.client.gui.forms;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import reman.client.app.office_maintenance.Meeting;
import reman.client.basetypes.Person;
import reman.client.gui.TimeRangeField;
import reman.client.gui.basic_gui.BasicSelectionPanel;
import reman.common.database.OfficeProjectManager;

public class MeetingFormPanel extends DboFormPanel<Meeting> {
	private JTextField name_;
	private TimeRangeField date_;
	private LocationFormPanel location_;
	private JCheckBox all_day_;
	private BasicSelectionPanel<Person> attendees_;

	public MeetingFormPanel(Meeting meeting, DboFormPanel<?> parent, boolean read_only) {
		super("Meeting", meeting, parent, read_only);

		this.addFormItem("Name", name_ = new JTextField(20));

		all_day_ = new JCheckBox("All Day");
		all_day_.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				date_.setEnabled(!all_day_.isSelected());
			}
		});
		this.addFormItem("", all_day_);
		this.addFormItem(date_ = new TimeRangeField("Time", meeting.getEffectiveTimeRange(), this,
				read_only));
		this.addFormItem(location_ = new LocationFormPanel(meeting.getLocation(), this, read_only));
		attendees_ = new BasicSelectionPanel<Person>("", OfficeProjectManager.instance()
				.getCurrentProject().getOffice().getMembers(), meeting.getAttendees(), (int) this
				.getPreferredSize().getWidth(), 300, false);
		this.addFormItem("Attendees", attendees_);
		updateForm();
	}

	public void retrieveForm() {
		dbo_.setName(name_.getText());
		dbo_.setAllDay(all_day_.isSelected());
		date_.retrieveForm();
		location_.retrieveForm();
	}

	public void updateForm() {
		name_.setText(dbo_.getName());
		all_day_.setSelected(dbo_.isAllDay());
		date_.updateForm();
		location_.updateForm();
	}

}

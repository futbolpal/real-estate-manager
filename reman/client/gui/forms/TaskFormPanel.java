package reman.client.gui.forms;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Timestamp;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.JTextField;

import reman.client.app.office_maintenance.CalendarItemReminder;
import reman.client.app.office_maintenance.Task;
import reman.client.app.office_maintenance.Task.TaskPriority;
import reman.client.basetypes.Person;
import reman.client.gui.DateField;
import reman.common.database.DatabaseObject;
import reman.common.database.OfficeProjectManager;
import reman.common.database.UserManager;

public class TaskFormPanel extends DboFormPanel<Task> {
	private JTextField name_;
	private JTextField desc_;
	private JSlider percent_done_;
	private DateField due_date_;
	private JComboBox priority_;
	private JComboBox assign_to_others_;
	private JCheckBox assign_to_me_;
	private JComboBox reminder_;

	public TaskFormPanel(Task a, DboFormPanel<? extends DatabaseObject> parent, boolean read_only)
			throws Exception {
		super("Task", a, parent, false);

		name_ = new JTextField(20);
		desc_ = new JTextField(20);
		percent_done_ = new JSlider(0, 100);
		due_date_ = new DateField(this);
		priority_ = new JComboBox(TaskPriority.values());
		assign_to_me_ = new JCheckBox("Assign to me");
		assign_to_me_.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				assign_to_others_.setEnabled(!assign_to_me_.isSelected());
			}
		});
		assign_to_others_ = new JComboBox(/*OfficeProjectManager.instance().getCurrentProject()
						.getOffice().getAgents().toArray()*/);

		reminder_ = new JComboBox();
		reminder_.addItem(new Reminder(15, "15 minutes"));
		reminder_.addItem(new Reminder(30, "30 minutes"));
		reminder_.addItem(new Reminder(45, "45 minutes"));
		reminder_.addItem(new Reminder(60, "1 hour"));
		reminder_.addItem(new Reminder(120, "2 hours"));
		reminder_.addItem(new Reminder(1440, "1 day"));

		this.addFormItem("Name", name_);
		this.addFormItem("Description", desc_);
		this.addFormItem("Due Date", due_date_);
		this.addFormItem("Priority", priority_);
		this.addFormItem("", assign_to_me_);
		this.addFormItem("Assign Task", assign_to_others_);
		this.addFormItem("Reminder", reminder_);
		this.updateForm();
	}

	public void retrieveForm() {
		Task t = dbo_;
		t.setName(name_.getText());
		t.setDescription(desc_.getText());
		t.setDueDate(due_date_.getDate());
		t.setPercentComplete((Integer) percent_done_.getValue());
		t.setPriority((TaskPriority) priority_.getSelectedItem());
		if (assign_to_me_.isSelected())
			t.setAssignee(UserManager.instance().getCurrentUserID());
		else {
			if (assign_to_others_.getSelectedItem() != null)
				t.setAssignee(((Person) assign_to_others_.getSelectedItem()).getUserID());
		}
		t.setReminder(((Reminder) reminder_.getSelectedItem()).generateReminder());
	}

	public void updateForm() {
		Task t = dbo_;
		if (t.getName() != null)
			name_.setText(t.getName());
		if (t.getDescription() != null)
			desc_.setText(t.getDescription());
		if (t.getDueDate() != null)
			due_date_.setDate(t.getDueDate());
		if (t.getPriority() != null)
			priority_.setSelectedItem(t.getPriority());
		percent_done_.setValue(t.getPercentComplete());

		reminder_.setSelectedIndex(0);//TODO ????
	}

	private class Reminder {
		private int minutes_;
		private String english_;

		public Reminder(int minutes, String english) {
			this.minutes_ = minutes;
			this.english_ = english;
		}

		public CalendarItemReminder generateReminder() {
			long mseconds_prior = minutes_ * 60 * 1000;
			Timestamp t = new Timestamp(dbo_.getDueDate().getTime() - mseconds_prior);
			CalendarItemReminder r = new CalendarItemReminder(dbo_, t);
			return r;
		}

		public String toString() {
			return english_;
		}
	}
}

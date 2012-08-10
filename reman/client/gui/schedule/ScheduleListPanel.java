package reman.client.gui.schedule;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import reman.client.app.office_maintenance.CalendarItem;
import reman.client.app.office_maintenance.ScheduleListener;

/**
 * This class will display all calendar items in a table.
 * @author jonathan
 *
 */
public class ScheduleListPanel extends JSplitPane implements ScheduleListener {

	private TasksTable tasks_;
	private MeetingsTable meetings_;

	public ScheduleListPanel() {
		super(JSplitPane.HORIZONTAL_SPLIT);

		meetings_ = new MeetingsTable();
		tasks_ = new TasksTable();

		JScrollPane task_view = new JScrollPane(tasks_, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		task_view.setColumnHeaderView(new JLabel("Tasks"));
		JScrollPane meeting_view = new JScrollPane(meetings_, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		this.setLeftComponent(task_view);
		this.setRightComponent(meeting_view);
	}

	public void calendarItemAdded(CalendarItem c) {
		meetings_.update();
		tasks_.update();
	}

	public void calendarItemChanged(CalendarItem c) {
		meetings_.update();
		tasks_.update();
	}

	public void calendarItemRemoved(CalendarItem c) {
		meetings_.update();
		tasks_.update();
	}
}

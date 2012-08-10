package reman.client.gui.schedule;

import java.util.Calendar;

import org.noos.xing.mydoggy.AggregationPosition;
import org.noos.xing.mydoggy.Content;
import org.noos.xing.mydoggy.DockedTypeDescriptor;
import org.noos.xing.mydoggy.MultiSplitConstraint;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.ToolWindowType;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.content.MyDoggyMultiSplitContentManagerUI;

import reman.client.app.office_maintenance.Meeting;
import reman.client.app.office_maintenance.Task;
import reman.client.gui.FrameworkWindow;
import reman.client.gui.GUIBuilder;
import reman.client.gui.TaskViewPanel;

public class SchedulePanel extends MyDoggyToolWindowManager implements GUIBuilder {
	private CalendarPanel month_;
	private WeekPanel week_;
	private MeetingViewPanel meeting_view_;
	private TaskViewPanel task_view_;
	private ScheduleListPanel list_;

	public SchedulePanel() {
		MyDoggyMultiSplitContentManagerUI splitUI = new MyDoggyMultiSplitContentManagerUI();
		this.getContentManager().setContentManagerUI(splitUI);
		Content list = this.getContentManager().addContent("All", "All", null,
				list_ = new ScheduleListPanel(), "", new MultiSplitConstraint(AggregationPosition.DEFAULT));
		Content week = this.getContentManager().addContent("Week", "Week", null,
				week_ = new WeekPanel(), "", new MultiSplitConstraint(list, 0));
		Content month = this.getContentManager().addContent("Month", "Month", null,
				month_ = new CalendarPanel(), "", new MultiSplitConstraint(week, 0));
		//add week tab
		//add day tab
		week_.setSchedulePanelOwner(this);
		month_.setSchedulePanelOwner(this);

		meeting_view_ = new MeetingViewPanel();
		task_view_ = new TaskViewPanel();
		ToolWindow t;

		/* Meeting Panel */
		t = this.registerToolWindow(MeetingViewPanel.ID, MeetingViewPanel.ID, null, meeting_view_,
				ToolWindowAnchor.RIGHT);
		t.setType(ToolWindowType.DOCKED);
		t.getTypeDescriptor(DockedTypeDescriptor.class).setDockLength(425);
		t.setAvailable(true);
		t.setActive(false);

		/* Task Panel */
		t = this.registerToolWindow(TaskViewPanel.ID, TaskViewPanel.ID, null, task_view_,
				ToolWindowAnchor.RIGHT);
		t.setType(ToolWindowType.DOCKED);
		t.getTypeDescriptor(DockedTypeDescriptor.class).setDockLength(425);
		t.setAvailable(true);
		t.setActive(false);
	}

	public void updateDayDetails(Calendar date, boolean show) {

	}

	public void updateMeetingDetails(Meeting meeting, boolean show) {
		meeting_view_.setMeeting(meeting);
		meeting_view_.requestFocusInWindow();
		this.getToolWindow(MeetingViewPanel.ID).setActive(show);
	}

	public void updateTaskDetails(Task task, boolean show) {
		task_view_.setTask(task);
		task_view_.requestFocusInWindow();
		this.getToolWindow(TaskViewPanel.ID).setActive(show);
	}

	public void build(FrameworkWindow w) {
		w.registerContent("Schedule View", "Schedule View", null, this, "", true,
				new MultiSplitConstraint(AggregationPosition.LEFT));
	}
}

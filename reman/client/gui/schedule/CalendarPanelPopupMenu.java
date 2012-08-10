package reman.client.gui.schedule;

import java.awt.event.ActionEvent;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import reman.client.app.IconManager;
import reman.client.app.office_maintenance.Meeting;
import reman.client.app.office_maintenance.OfficeMaintenanceManager;
import reman.client.app.office_maintenance.Task;

public class CalendarPanelPopupMenu extends JPopupMenu {

	public CalendarPanelPopupMenu(final CalendarPanelUnitPanel owner) {
		final Calendar c = owner.getCalendar();
		JMenu task_menu = new JMenu("Tasks");
		task_menu.add(new JMenuItem(new AbstractAction("Add Task", IconManager.instance().getIcon(16,
				"actions", "list-add.png")) {
			public void actionPerformed(ActionEvent e) {
				Task new_task = new Task("New Task");
				OfficeMaintenanceManager.instance().addTask(new_task);
				owner.getOwner().updateTaskDetails(new_task, true);
			}
		}));
		task_menu.addSeparator();
		for (final Task m : owner.getTasks()) {
			owner.getOwner().updateTaskDetails(m, true);
		}

		JMenu meeting_menu = new JMenu("Meetings");
		meeting_menu.add(new JMenuItem(new AbstractAction("Add Meeting", IconManager.instance()
				.getIcon(16, "actions", "list-add.png")) {
			public void actionPerformed(ActionEvent e) {
				Meeting new_meeting = new Meeting("New Meeting", new Timestamp(c.getTimeInMillis()));
				OfficeMaintenanceManager.instance().addMeeting(new_meeting);
				owner.getOwner().updateMeetingDetails(new_meeting, true);
			}
		}));
		meeting_menu.addSeparator();
		for (final Meeting m : owner.getMeetings()) {
			JMenuItem meeting = new JMenuItem(new AbstractAction(m.getName()) {
				public void actionPerformed(ActionEvent e) {
					owner.getOwner().updateMeetingDetails(m, true);
				}
			});
		}

		String title = null;
		switch (owner.getUnitResolution()) {
		case Calendar.DAY_OF_YEAR:
			title = DateFormat.getDateInstance(DateFormat.SHORT).format(owner.getCalendar().getTime());
			break;
		case Calendar.MINUTE:
			title = DateFormat.getDateTimeInstance().format(owner.getCalendar().getTime());
		}
		JMenuItem today = new JMenuItem(title);
		today.setEnabled(false);
		this.add(today);
		this.add(task_menu);
		this.add(meeting_menu);
	}
}

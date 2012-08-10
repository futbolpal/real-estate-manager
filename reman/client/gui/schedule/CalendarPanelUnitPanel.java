package reman.client.gui.schedule;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import reman.client.app.office_maintenance.Meeting;
import reman.client.app.office_maintenance.OfficeMaintenanceManager;
import reman.client.app.office_maintenance.Task;

public abstract class CalendarPanelUnitPanel extends JPanel {
	private Calendar date_;
	private CalendarPanelPopupMenu menu_;
	protected ArrayList<Meeting> meetings_;
	protected ArrayList<Task> tasks_;
	protected CalendarPanel owner_;

	public CalendarPanelUnitPanel(CalendarPanel owner, Calendar date) {
		owner_ = owner;
		date_ = date;
		switch (this.getUnitResolution()) {
		case Calendar.DAY_OF_YEAR:
			meetings_ = OfficeMaintenanceManager.instance().getMeetingsOnDay(date_);
			tasks_ = OfficeMaintenanceManager.instance().getTasksDueOnDay(date_);
			break;
		case Calendar.MINUTE:
			meetings_ = OfficeMaintenanceManager.instance().getMeetingsAtTime(date_);
			tasks_ = OfficeMaintenanceManager.instance().getTasksDueOnDay(date_);
			break;
		}

		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					menu_.show(CalendarPanelUnitPanel.this, e.getPoint().x, e.getPoint().y);
				}
			}
		});
		menu_ = new CalendarPanelPopupMenu(this);
	}

	public ArrayList<Meeting> getMeetings() {
		return meetings_;
	}

	public ArrayList<Task> getTasks() {
		return tasks_;
	}

	public void setCalendar(Calendar c) {
		date_ = c;
	}

	public Calendar getCalendar() {
		return date_;
	}

	public CalendarPanel getOwner() {
		return owner_;
	}

	public abstract int getUnitResolution();
}

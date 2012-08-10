package reman.client.gui.schedule;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Calendar;

import javax.swing.JLabel;
import javax.swing.border.LineBorder;

import reman.client.app.office_maintenance.Meeting;

public class DayDetailsUnitPanel extends DayUnitPanel {

	private Color[] colors_ = { Color.red, Color.green, Color.blue };

	public DayDetailsUnitPanel(CalendarPanel owner, Calendar time) {
		super(owner, time);
		this.setOpaque(true);
		this.setBorder(new LineBorder(Color.BLACK));
	}

	public void build() {
		GridBagLayout gbl = new GridBagLayout();
		this.setLayout(gbl);
		this.setPreferredSize(new Dimension(100, 700));
		this.setMinimumSize(new Dimension(100, 700));
		Calendar c = (Calendar) this.getCalendar().clone();
		int day_start = ((WeekPanel) owner_).getDayStartHour();
		c.set(Calendar.HOUR_OF_DAY, day_start);
		c.set(Calendar.MINUTE, 0);
		int resolution = ((WeekPanel) owner_).getDayResolution();

		System.out.println(meetings_.size());
		for (int i = 0; i < this.getMeetings().size(); i++) {
			GridBagConstraints cc = new GridBagConstraints();
			cc.gridx = i;

			Meeting m = meetings_.get(i);
			Calendar ms = Calendar.getInstance();
			Calendar me = Calendar.getInstance();
			ms.setTimeInMillis(m.getEffectiveTimeRange().getBegin().getTime());
			me.setTimeInMillis(m.getEffectiveTimeRange().getEnd().getTime());

			int minute_day_start = day_start * 60;
			int minute_start = ms.get(Calendar.HOUR_OF_DAY) * 60 + ms.get(Calendar.MINUTE);
			int minute_end = me.get(Calendar.HOUR_OF_DAY) * 60 + me.get(Calendar.MINUTE);
			int duration_cells = (minute_end - minute_start) / resolution;
			cc.gridy = (minute_start - minute_day_start) / resolution;
			cc.gridheight = duration_cells;
			cc.fill = GridBagConstraints.BOTH;

			JLabel l = new JLabel("?");
			l.setOpaque(true);
			l.setBackground(Color.GREEN);
			l.setPreferredSize(new Dimension(50, 50));
			this.add(l, cc);
		}
	}

	public int getUnitResolution() {
		return Calendar.DAY_OF_YEAR;
	}
}

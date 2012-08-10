package reman.client.gui.schedule;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import reman.client.app.IconManager;
import reman.client.app.office_maintenance.CalendarItem;

public class WeekPanel extends CalendarPanel {
	private int grid_resolution_minutes_;
	private int hour_start_;
	private int hour_end_;
	private int week_start_;
	private WeekTable table_;

	public WeekPanel() {
		super();
	}

	protected void initParameters(Timestamp t) {
		super.initParameters(t);
		hour_start_ = 8;
		hour_end_ = 20;
		grid_resolution_minutes_ = 30;
		week_start_ = Calendar.SUNDAY;
		table_ = new WeekTable(this);
	}

	public int getDayStartHour() {
		return hour_start_;
	}

	public int getDayEndHour() {
		return hour_end_;
	}

	public void setDayStartHour(int hours24) {
		hour_start_ = hours24;
	}

	public void setDayEndHour(int hours24) {
		hour_end_ = hours24;
	}

	public void setDayResolution(int minutes) {
		this.grid_resolution_minutes_ = minutes;
	}

	public int getDayResolution() {
		return this.grid_resolution_minutes_;
	}

	public int getDurationMinutes() {
		return (hour_end_ - hour_start_) * 60;
	}

	public JPanel buildSelectionPanel() {

		JPanel weekselect = new JPanel();
		weekselect.setLayout(new BorderLayout());
		JButton prev_week = new JButton(IconManager.instance()
				.getIcon(16, "actions", "go-previous.png"));
		prev_week.setBorderPainted(false);
		prev_week.setFocusPainted(false);
		prev_week.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				date_.add(Calendar.WEEK_OF_YEAR, -1);
				dateChanged();
			}
		});
		JButton next_week = new JButton(IconManager.instance().getIcon(16, "actions", "go-next.png"));
		next_week.setBorderPainted(false);
		next_week.setFocusPainted(false);
		next_week.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				date_.add(Calendar.WEEK_OF_YEAR, 1);
				dateChanged();
			}
		});

		Calendar t = (Calendar) date_.clone();
		t.roll(Calendar.DAY_OF_YEAR, -date_.get(Calendar.DAY_OF_WEEK) + 1);
		int first_day_of_week = t.get(Calendar.DAY_OF_MONTH);
		t.roll(Calendar.DAY_OF_YEAR, 6);
		int last_day_of_week = t.get(Calendar.DAY_OF_MONTH);

		JPanel week_desc = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JLabel month = new JLabel("<html><h1>"
				+ date_.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + "</h1></html>");
		JLabel year = new JLabel("<html><h3>" + date_.get(Calendar.YEAR) + "</h3></html>");
		JLabel week = new JLabel("<html><h3>" + first_day_of_week + " - " + last_day_of_week
				+ "</h3></html>");
		week_desc.add(year);
		week_desc.add(month);
		week_desc.add(week);

		weekselect.add(prev_week, BorderLayout.WEST);
		weekselect.add(week_desc, BorderLayout.CENTER);
		weekselect.add(next_week, BorderLayout.EAST);
		return weekselect;
	}

	public void calendarItemChanged(CalendarItem e) {
		//do nothing.
	}

	public JPanel fillGrid() {
		table_.refresh();
		JPanel grid = new JPanel(new BorderLayout());
		JScrollPane view = new JScrollPane(table_);
		grid.add(view, BorderLayout.CENTER);
		return grid;
	}

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JFrame f = new JFrame();
		f.add(new WeekPanel());
		f.pack();
		f.setVisible(true);
	}

}

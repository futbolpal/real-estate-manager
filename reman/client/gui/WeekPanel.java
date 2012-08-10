package reman.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;

import reman.client.app.IconManager;
import reman.client.app.office_maintenance.Meeting;
import reman.client.app.office_maintenance.OfficeMaintenanceManager;
import reman.client.app.office_maintenance.Task;

public class WeekPanel extends CalendarPanel {
	private int grid_resolution_minutes_;
	private int time_start_;
	private int time_end_;
	private int week_start_;
	private DayUnitLabel[][] labels_;

	public WeekPanel() {
		super();
	}

	protected void initParameters(Timestamp t) {
		super.initParameters(t);
		time_start_ = 8;
		time_end_ = 20;
		grid_resolution_minutes_ = 30;
		week_start_ = Calendar.SUNDAY;
	}

	public void setDayStartTime(int hours24) {
		time_start_ = hours24;
	}

	public void setDayEndTime(int hours24) {
		time_end_ = hours24;
	}

	public void setDayResolution(int minutes) {
		this.grid_resolution_minutes_ = minutes;
	}

	public JPanel buildSelectionPanel() {

		JPanel weekselect = new JPanel();
		weekselect.setLayout(new BorderLayout());
		JButton prev_week = new JButton(new ImageIcon(this.getClass().getResource("go-previous.png")));
		prev_week.setBorderPainted(false);
		prev_week.setFocusPainted(false);
		prev_week.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				date_.add(Calendar.WEEK_OF_YEAR, -1);
				dateChanged();
			}
		});
		JButton next_week = new JButton(new ImageIcon(this.getClass().getResource("go-next.png")));
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

	public JPanel fillGrid() {
		JPanel grid = new JPanel(new GridLayout(0, 8));
		int rows = ((time_end_ - time_start_) * 60) / this.grid_resolution_minutes_;
		labels_ = new DayUnitLabel[rows][7];

		/* Put week days in first row */
		Calendar t = (Calendar) date_.clone();
		t.roll(Calendar.DAY_OF_YEAR, -date_.get(Calendar.DAY_OF_WEEK) + 1);
		grid.add(new JLabel("Time"));
		for (int i = 0; i < 7; i++) {
			JLabel l = new JLabel("<html>" + t.get(Calendar.DAY_OF_MONTH) + "<br><b>"
					+ t.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US) + "</b></html>");
			l.setHorizontalAlignment(SwingConstants.CENTER);
			grid.add(l);
			t.roll(Calendar.DAY_OF_YEAR, 1);
		}

		int dow = this.today_.get(Calendar.DAY_OF_WEEK) - 1;
		int hours = time_start_;
		int minutes = 0;
		EventGroup[] week = { new EventGroup(), new EventGroup(), new EventGroup(), new EventGroup(),
				new EventGroup(), new EventGroup(), new EventGroup() };
		Calendar c = (Calendar) date_.clone();
		for (int i = 0; i < rows; i++) {
			c.set(Calendar.HOUR_OF_DAY, hours);
			c.set(Calendar.MINUTE, minutes);
			Date d = c.getTime();
			grid.add(new JLabel(DateFormat.getTimeInstance(DateFormat.SHORT).format(d)));

			for (int j = 0; j < 7; j++) {
				c.set(Calendar.DAY_OF_WEEK, j + 1);

				ArrayList<Meeting> meetings_on_day = OfficeMaintenanceManager.instance().getMeetingsAtTime(
						c);
				ArrayList<Task> tasks_on_day = OfficeMaintenanceManager.instance().getTasksDueOnDay(c);

				DayUnitLabel dul = new DayUnitLabel((Calendar) c.clone(), meetings_on_day, tasks_on_day);
				grid.add(dul);
				labels_[i][j] = dul;

				if (j == dow && c.get(Calendar.WEEK_OF_YEAR) == today_.get(Calendar.WEEK_OF_YEAR))
					week[dow].addUnit(dul);
			}

			minutes = (minutes + this.grid_resolution_minutes_) % 60;
			if (minutes == 0)
				hours = (hours + 1) % 24;
		}

		for (DayUnitLabel l : week[dow].labels_)
			l.setBackground(new Color(.7f, .7f, .7f));

		return grid;
	}

	private class EventGroup {
		private ArrayList<DayUnitLabel> labels_;

		public EventGroup() {
			labels_ = new ArrayList<DayUnitLabel>();
		}

		public void addUnit(DayUnitLabel l) {
			labels_.add(l);
		}
	}

	private class DayUnitLabel extends JPanel {
		private Color[] colors_ = { Color.RED, Color.GREEN, Color.BLUE };
		private ArrayList<Meeting> meetings_;
		private ArrayList<Task> tasks_;
		private Calendar time_;
		private JPopupMenu menu_;

		public DayUnitLabel(Calendar time, ArrayList<Meeting> meetings, ArrayList<Task> tasks) {
			this.setBorder(new LineBorder(Color.BLACK));
			tasks_ = tasks;
			meetings_ = meetings;
			time_ = time;
			menu_ = new JPopupMenu();
			menu_.add(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(
					time_.getTime()));
			menu_.addSeparator();
			JMenu task_menu = new JMenu("Tasks");
			task_menu.add(new JMenuItem(new AbstractAction("Add Task", IconManager.instance().getIcon(16,
					"actions", "list-add.png")) {
				public void actionPerformed(ActionEvent e) {
					Task new_task = new Task("New Task");
					new_task.setDueDate(new Timestamp(time_.getTimeInMillis()));
					OfficeMaintenanceManager.instance().addTask(new_task);
					owner_.updateTaskDetails(new_task, true);
				}
			}));
			if (tasks_ != null) {
				task_menu.addSeparator();
				this.setLayout(new GridLayout(1, tasks_.size() + 1));
				for (int i = 0; i < tasks_.size(); i++) {
					final Task t = tasks_.get(i);
					JLabel t_bar = new JLabel();
					t_bar.setBackground(colors_[i % colors_.length]);
					t_bar.setOpaque(true);
					t_bar.setToolTipText(t.getQuickDescription());
					t_bar.addMouseListener(new MouseAdapter() {
						public void mouseClicked(MouseEvent e) {
							if (SwingUtilities.isRightMouseButton(e)) {
								menu_.show(DayUnitLabel.this, 0, 0);
							} else {
								owner_.updateTaskDetails(t, true);
							}
						}

						public void mouseEntered(MouseEvent e) {
							DayUnitLabel.this.setBorder(new LineBorder(Color.red));
						}

						public void mouseExited(MouseEvent e) {
							DayUnitLabel.this.setBorder(new LineBorder(Color.black));
						}
					});
					this.add(t_bar);
					task_menu.add(new JMenuItem(new AbstractAction(t.getName()) {
						public void actionPerformed(ActionEvent e) {
							owner_.updateTaskDetails(t, true);
						}
					}));
				}
			}
			menu_.add(task_menu);
			final JMenu meeting_menu = new JMenu("Meetings");
			meeting_menu.add(new JMenuItem(new AbstractAction("Add Meeting", IconManager.instance()
					.getIcon(16, "actions", "list-add.png")) {
				public void actionPerformed(ActionEvent e) {
					Meeting new_meeting = new Meeting("New Meeting", new Timestamp(time_.getTimeInMillis()));
					OfficeMaintenanceManager.instance().addMeeting(new_meeting);
					owner_.updateMeetingDetails(new_meeting, true);
				}
			}));
			if (meetings_ != null) {
				meeting_menu.addSeparator();
				for (final Meeting m : meetings_) {
					meeting_menu.add(new JMenuItem(new AbstractAction(m.getName()) {
						public void actionPerformed(ActionEvent e) {
							owner_.updateMeetingDetails(m, true);
						}
					}));
				}
			}
			menu_.add(meeting_menu);

			JLabel m_bar = new JLabel();
			if (meetings_.size() > 0) {
				m_bar.setOpaque(true);
				m_bar.setToolTipText(meetings_.size() + " meeting(s)");
				m_bar.setBackground(Color.ORANGE);
			}
			m_bar.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						menu_.show(DayUnitLabel.this, 0, 0);
					} else {
						if (meetings_.size() == 1)
							owner_.updateMeetingDetails(meetings_.get(0), true);
					}
				}

				public void mouseEntered(MouseEvent e) {
					DayUnitLabel.this.setBorder(new LineBorder(Color.red));
				}

				public void mouseExited(MouseEvent e) {
					DayUnitLabel.this.setBorder(new LineBorder(Color.black));
				}
			});
			this.add(m_bar);
		}
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

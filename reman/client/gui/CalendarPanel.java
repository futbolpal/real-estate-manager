package reman.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import reman.client.app.office_maintenance.CalendarItem;
import reman.client.app.office_maintenance.Meeting;
import reman.client.app.office_maintenance.OfficeMaintenanceManager;
import reman.client.app.office_maintenance.ScheduleListener;
import reman.client.app.office_maintenance.Task;
import reman.client.gui.schedule.SchedulePanel;

/**
 * This class provides a general GUI calendar component that can be used 
 * throughout the program
 * @author jonathan
 *
 */
public class CalendarPanel extends JPanel implements ScheduleListener {
	protected SchedulePanel owner_;
	protected Calendar today_;
	protected Calendar date_;
	protected JLabel curr_month_;
	protected JLabel curr_year_;
	protected JPanel curr_grid_;
	protected JPanel selection_panel_;
	private DayLabel selected_day_;
	private boolean show_details_;

	private ArrayList<CalendarPanelListener> listeners_;

	public CalendarPanel() {
		this(new Timestamp(System.currentTimeMillis()));
	}

	public CalendarPanel(boolean details) {
		show_details_ = details;
		listeners_ = new ArrayList<CalendarPanelListener>();
		initParameters(new Timestamp(System.currentTimeMillis()));
		OfficeMaintenanceManager.instance().addScheduleListener(this);
		this.setLayout(new BorderLayout());
		this.rebuild();
	}

	public CalendarPanel(Timestamp t) {
		show_details_ = true;
		listeners_ = new ArrayList<CalendarPanelListener>();
		initParameters(t);
		OfficeMaintenanceManager.instance().addScheduleListener(this);
		this.setLayout(new BorderLayout());
		this.rebuild();
	}

	public void rebuild() {
		if (selection_panel_ != null)
			this.remove(this.selection_panel_);
		if (curr_grid_ != null)
			this.remove(this.curr_grid_);
		curr_grid_ = fillGrid();
		selection_panel_ = this.buildSelectionPanel();
		this.add(selection_panel_, BorderLayout.NORTH);
		this.add(curr_grid_, BorderLayout.CENTER);
		this.validate();
	}

	public void setShowDetails(boolean flag) {
		show_details_ = flag;
	}

	public boolean isShowingDetails() {
		return this.show_details_;
	}

	public void setSchedulePanelOwner(SchedulePanel owner) {
		owner_ = owner;
	}

	protected void initParameters(Timestamp t) {
		today_ = Calendar.getInstance();
		today_.setTimeInMillis(System.currentTimeMillis());
		date_ = Calendar.getInstance();
		date_.setTimeInMillis(t.getTime());
		curr_month_ = new JLabel(date_.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US));
		curr_year_ = new JLabel(date_.get(Calendar.YEAR) + "");

	}

	protected void dateChanged() {
		curr_month_.setText(date_.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US));
		curr_year_.setText(date_.get(Calendar.YEAR) + "");
		this.rebuild();
	}

	protected JPanel buildSelectionPanel() {
		JPanel monthselect = new JPanel();
		monthselect.setLayout(new BorderLayout());
		JButton prev_month = new JButton(new ImageIcon(this.getClass().getResource("go-previous.png")));
		prev_month.setBorderPainted(false);
		prev_month.setFocusPainted(false);
		prev_month.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				date_.add(Calendar.MONTH, -1);
				dateChanged();
			}
		});
		JButton next_month = new JButton(new ImageIcon(this.getClass().getResource("go-next.png")));
		next_month.setBorderPainted(false);
		next_month.setFocusPainted(false);
		next_month.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				date_.add(Calendar.MONTH, 1);
				dateChanged();
			}
		});
		monthselect.add(prev_month, BorderLayout.WEST);
		monthselect.add(curr_month_, BorderLayout.CENTER);
		monthselect.add(next_month, BorderLayout.EAST);

		JPanel yearselect = new JPanel();
		yearselect.setLayout(new BorderLayout());
		JButton prev_year = new JButton(new ImageIcon(this.getClass().getResource("go-previous.png")));
		prev_year.setBorderPainted(false);
		prev_year.setFocusPainted(false);
		prev_year.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				date_.add(Calendar.YEAR, -1);
				dateChanged();
			}
		});
		JButton next_year = new JButton(new ImageIcon(this.getClass().getResource("go-next.png")));
		next_year.setBorderPainted(false);
		next_year.setFocusPainted(false);
		next_year.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				date_.add(Calendar.YEAR, 1);
				dateChanged();
			}
		});
		yearselect.add(prev_year, BorderLayout.WEST);
		yearselect.add(curr_year_, BorderLayout.CENTER);
		yearselect.add(next_year, BorderLayout.EAST);

		JPanel selectpanel = new JPanel();
		selectpanel.setLayout(new BorderLayout());
		selectpanel.add(monthselect, BorderLayout.WEST);
		selectpanel.add(yearselect, BorderLayout.EAST);
		return selectpanel;
	}

	protected JPanel fillGrid() {
		JPanel grid = new JPanel(new GridLayout(0, 7));

		Calendar curr_month_start = Calendar.getInstance();
		curr_month_start.set(Calendar.MONTH, date_.get(Calendar.MONTH));
		curr_month_start.set(Calendar.YEAR, date_.get(Calendar.YEAR));
		curr_month_start.set(Calendar.DAY_OF_MONTH, 1);

		/* Put week days in first row */
		grid.add(new JLabel("<html><b><u>Sun</u></b></html>"));
		grid.add(new JLabel("<html><b><u>Mon</u></b></html>"));
		grid.add(new JLabel("<html><b><u>Tue</u></b></html>"));
		grid.add(new JLabel("<html><b><u>Wed</u></b></html>"));
		grid.add(new JLabel("<html><b><u>Thu</u></b></html>"));
		grid.add(new JLabel("<html><b><u>Fri</u></b></html>"));
		grid.add(new JLabel("<html><b><u>Sat</u></b></html>"));

		int dow = curr_month_start.get(Calendar.DAY_OF_WEEK);
		for (int i = 0; i < dow - 1; i++)
			grid.add(new JLabel());

		while (curr_month_start.get(Calendar.MONTH) == date_.get(Calendar.MONTH)) {
			JLabel day = new DayLabel(curr_month_start);
			grid.add(day);
			curr_month_start.add(Calendar.DAY_OF_MONTH, 1);
		}
		return grid;
	}

	private void fireDateChanged() {
		for (CalendarPanelListener l : listeners_) {
			l.dateChangedEvent(date_);
		}
	}

	public void addListener(CalendarPanelListener l) {
		listeners_.add(l);
	}

	public Timestamp getSelectedDate() {
		return new Timestamp(date_.getTimeInMillis());
	}

	public void calendarItemAdded(CalendarItem c) {
		rebuild();
	}

	public void calendarItemRemoved(CalendarItem c) {
		rebuild();
	}

	public void calendarItemChanged(CalendarItem c) {
		System.out.println("Changed");
		rebuild();
	}

	private class DayLabel extends JLabel {
		private Color primary_;
		private Color selected_;

		public DayLabel(final Calendar date) {
			final int day = date.get(Calendar.DAY_OF_MONTH);
			primary_ = this.getBackground();
			selected_ = Color.DARK_GRAY;
			this.setBorder(new TitledBorder(String.valueOf(day)));
			this.setOpaque(false);
			this.setVerticalAlignment(SwingConstants.TOP);
			this.setVerticalTextPosition(SwingConstants.TOP);
			if (CalendarPanel.this.isShowingDetails()) {
				ArrayList<Meeting> meetings = OfficeMaintenanceManager.instance().getMeetingsOnDay(date);
				ArrayList<Task> tasks = OfficeMaintenanceManager.instance().getTasksDueOnDay(date);
				String details = new String();
				if (meetings.size() > 0)
					details = "<html>" + meetings.size() + " meetings<BR>";
				if (tasks.size() > 0)
					details += tasks.size() + " tasks due";
				this.setText(details);
				this.setFont(this.getFont().deriveFont(8f));
			}
			this.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (selected_day_ != null) {
						selected_day_.setBackground(selected_day_.primary_);
					}
					DayLabel.this.setBackground(selected_);
					selected_day_ = DayLabel.this;
					date_.set(Calendar.DAY_OF_MONTH, day);
					fireDateChanged();
				}
			});
			if (date.get(Calendar.DAY_OF_YEAR) == today_.get(Calendar.DAY_OF_YEAR)
					&& date.get(Calendar.YEAR) == today_.get(Calendar.YEAR)) {
				primary_ = Color.CYAN;
				this.setBackground(Color.CYAN);
			}
		}
	}
}
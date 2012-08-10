package reman.client.gui.schedule;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import reman.client.app.office_maintenance.CalendarItem;
import reman.client.app.office_maintenance.Meeting;
import reman.client.app.office_maintenance.OfficeMaintenanceManager;
import reman.client.app.office_maintenance.ScheduleListener;
import reman.client.app.office_maintenance.Task;
import reman.client.basetypes.TimeRange;

public class WeekTable extends JTable implements CalendarPanelListener, ScheduleListener {
	private static Color border_ = new Color(63, 125, 145);
	private static Color header_bg_ = new Color(231, 238, 236);
	private static Color header_fg_ = new Color(86, 125, 145);
	private static Color selected_day_ = new Color(245, 221, 145);
	private static Color today_ = new Color(223, 234, 244);

	private WeekPanel owner_;
	private Calendar chosen_date_;

	private ArrayList<MeetingPanel> meeting_panels_;

	/* Mouse Selection */
	private int selected_dow_;

	public WeekTable(WeekPanel owner) {
		owner_ = owner;
		owner_.addListener(this);
		chosen_date_ = owner_.getCurrentDate();
		meeting_panels_ = new ArrayList<MeetingPanel>();
		OfficeMaintenanceManager.instance().addScheduleListener(this);

		this.getTableHeader().setReorderingAllowed(false);
		this.setColumnSelectionAllowed(false);
		this.setRowSelectionAllowed(false);
		this.setModel(new MyTableModel());
		this.setRowHeight(40);
		this.setGridColor(border_);
		this.setShowGrid(true);
		//this.setIntercellSpacing(new Dimension(0, 0));
		this.addMouseListener(new MouseAdapter() {
			private Point start_;

			public void mousePressed(MouseEvent e) {
				start_ = e.getPoint();
				selected_dow_ = columnModel.getColumnIndexAtX(e.getX());
				WeekTable.this.refresh();
			}

			public void mouseReleased(MouseEvent e) {
				int range_px = 2;
				int dx = (int) start_.getX() - e.getX();
				int dy = (int) start_.getY() - e.getY();
				if (Math.abs(dy) > range_px) {
					WeekTable.this.createMeetingFromSelection();
				}
				WeekTable.this.getSelectionModel().clearSelection();
				WeekTable.this.repaint();
			}
		});
		this.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				WeekTable.this.repaint();
			}
		});
		for (int i = 0; i < this.getColumnCount(); i++) {
			TableColumn col = this.getColumnModel().getColumn(i);
			if (i == 0) {
				col.setHeaderRenderer(new TimeCellRenderer());
				col.setCellRenderer(new TimeCellRenderer());
			} else {
				col.setHeaderRenderer(new DayCellRenderer(i));
				col.setCellRenderer(new DayCellRenderer(i));
			}
		}

		this.setLayout(null);
		this.refresh();
	}

	public Calendar getTimeAtY(int y) {
		int row = y / this.getRowHeight();
		Calendar c = getHourAtRow(row);
		c.set(Calendar.MINUTE,
				(int) (((y % this.getRowHeight()) / (double) this.getRowHeight()) * 60.0));
		return c;
	}

	public Calendar getHourAtRow(int i) {
		MyTableModel m = (MyTableModel) this.getModel();
		return (Calendar) m.getValueAt(i, 0);
	}

	public void refresh() {
		chosen_date_ = owner_.getCurrentDate();
		for (MeetingPanel p : meeting_panels_)
			this.remove(p);
		meeting_panels_.clear();
		ArrayList<Meeting> meetings = OfficeMaintenanceManager.instance().getMeetingsOnWeek(
				chosen_date_);
		for (Meeting m : meetings) {
			MeetingPanel p = new MeetingPanel(this, m);
			this.add(p);
			meeting_panels_.add(p);
		}

		this.getTableHeader().repaint();
		this.repaint();
	}

	public void createMeetingFromSelection() {
		int[] rows = this.getSelectedRows();
		int col = this.getSelectedColumn();
		Calendar start = this.getHourAtRow(rows[0]);
		Calendar end = this.getHourAtRow(rows[rows.length - 1]);
		start.set(Calendar.DAY_OF_YEAR, chosen_date_.get(Calendar.DAY_OF_YEAR));
		start.add(Calendar.DAY_OF_YEAR, col - chosen_date_.get(Calendar.DAY_OF_WEEK));
		end.set(Calendar.DAY_OF_YEAR, chosen_date_.get(Calendar.DAY_OF_YEAR));
		end.add(Calendar.DAY_OF_YEAR, col - chosen_date_.get(Calendar.DAY_OF_WEEK));
		end.add(Calendar.HOUR_OF_DAY, 1);

		Meeting m = new Meeting();
		TimeRange r = new TimeRange(new Timestamp(start.getTimeInMillis()), new Timestamp(end
				.getTimeInMillis()));
		m.setEffectiveTimeRange(r);
		OfficeMaintenanceManager.instance().addMeeting(m);
	}

	public void dateChangedEvent(Calendar date) {
		this.refresh();
	}

	public void calendarItemAdded(CalendarItem c) {
		if (c instanceof Meeting) {
			Meeting m = (Meeting) c;
			if (isMeetingInRange(m)) {
				MeetingPanel p = new MeetingPanel(this, m);
				this.add(p);
				meeting_panels_.add(p);
			}
		}
		this.repaint();
	}

	public boolean isMeetingInRange(Meeting m) {
		Calendar start = Calendar.getInstance();
		Calendar wk_start = (Calendar) chosen_date_.clone();
		Calendar wk_end = (Calendar) chosen_date_.clone();
		wk_start.add(Calendar.DAY_OF_YEAR, -chosen_date_.get(Calendar.DAY_OF_WEEK) + 1);
		wk_end.add(Calendar.DAY_OF_YEAR, 7 - chosen_date_.get(Calendar.DAY_OF_WEEK));
		start.setTimeInMillis(m.getEffectiveTimeRange().getBegin().getTime());
		if (wk_start.get(Calendar.DAY_OF_YEAR) > start.get(Calendar.DAY_OF_YEAR)
				|| wk_end.get(Calendar.DAY_OF_YEAR) < start.get(Calendar.DAY_OF_YEAR)) {
			//out of range
			return false;
		}
		return true;
	}

	public void calendarItemChanged(CalendarItem c) {
		if (c instanceof Meeting)
			meeting_panels_.get(meeting_panels_.indexOf(new MeetingPanel(this, (Meeting) c))).update();
		this.repaint();
	}

	public void calendarItemRemoved(CalendarItem c) {
		if (c instanceof Meeting) {
			meeting_panels_.remove(c);
		}
		this.repaint();
	}

	public CalendarPanel getOwner() {
		return owner_;
	}

	protected void repaintMeetingPanels() {
		for (MeetingPanel p : meeting_panels_) {
			//this.remove(p);
			Meeting m = p.getMeeting();
			Calendar start = m.getStart();
			Calendar end = m.getEnd();
			int dow = start.get(Calendar.DAY_OF_WEEK);
			int hour = start.get(Calendar.HOUR_OF_DAY);
			int min = start.get(Calendar.MINUTE);
			int duration_min = (end.get(Calendar.HOUR_OF_DAY) - hour) * 60
					+ (end.get(Calendar.MINUTE) - min);

			//if (!this.isMeetingInRange(m)) {
			//this.remove(p);
			//	}

			int x = 3;
			for (int i = 0; i < dow; i++) {
				x += this.getColumnModel().getColumn(i).getWidth();
			}
			int w = this.getColumnModel().getColumn(dow).getWidth()
					/ OfficeMaintenanceManager.instance().getOverlappingMeetings(m).size() - 7;
			int y = this.getRowHeight() * hour + (int) ((min / 60.0) * this.getRowHeight()) + 3;
			int h = (int) (((1.0 / 60.0) * this.getRowHeight()) * duration_min) - 7;
			p.setBounds(x, y, w, h);
			for (MeetingPanel p2 : meeting_panels_) {
				if (p2 == p)
					continue;
				if (p2.getBounds().intersects(p.getBounds())) {
					p.setBounds(p.getX() + p2.getWidth() + 2, y, w, h);
				}
			}
			p.revalidate();
		}
	}

	public void paint(Graphics g) {
		repaintMeetingPanels();

		Graphics2D g2d = (Graphics2D) g;
		super.paint(g2d);
		int[] rows = this.getSelectedRows();
		int col = this.getSelectedColumn();

		if (rows.length > 0) {
			Color old = g2d.getColor();
			Font oldf = g2d.getFont();

			/* Draw box */
			g2d.setColor(new Color(0, 0, 255, 50));
			int x1 = col * this.getColumnModel().getColumn(0).getWidth();
			int w = this.getColumnModel().getColumn(0).getWidth();
			int y1 = rows[0] * this.getRowHeight();
			int y2 = rows[rows.length - 1] * this.getRowHeight() + this.getRowHeight();
			g2d.fillRect(x1, y1, w, y2 - y1);

			/* Draw times */
			Calendar end = this.getHourAtRow(rows[rows.length - 1]);
			end.roll(Calendar.HOUR_OF_DAY, true);
			String hour_start = DateFormat.getTimeInstance(DateFormat.SHORT).format(
					this.getHourAtRow(rows[0]).getTime());
			String hour_end = DateFormat.getTimeInstance(DateFormat.SHORT).format(end.getTime());
			g2d.setColor(Color.black);
			g2d.setFont(oldf.deriveFont(14f).deriveFont(Font.BOLD));
			g2d.drawString(hour_start, x1, y1 - 5);
			g2d.drawString(hour_end, x1, y2 + g2d.getFontMetrics().getHeight());

			g2d.setFont(oldf);
			g2d.setColor(old);
		}
	}

	private class TimeCellRenderer extends JPanel implements TableCellRenderer {
		private JLabel time_;

		public TimeCellRenderer() {
			time_ = new JLabel();
			time_.setVerticalAlignment(SwingConstants.CENTER);
			time_.setFont(time_.getFont().deriveFont(12f));
			this.setBackground(header_bg_);
			this.add(time_);
		}

		public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r,
				int c) {
			if (r < 0) {
				return new JLabel();
			}
			Calendar cal = (Calendar) v;
			String formatted = DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.getTime());
			time_.setText(formatted);
			return this;
		}
	}

	private class DayCellRenderer extends JPanel implements TableCellRenderer {

		private JPanel header_;
		private JPanel date_info_;
		private JLabel date_;
		private JLabel day_;
		private JPanel task_info_;
		private int dow_;
		private Color normal_clr_;
		private Border normal_bdr_;

		public DayCellRenderer(int dow) {
			normal_clr_ = this.getBackground();
			normal_bdr_ = new EmptyBorder(0, 0, 0, 0);

			dow_ = dow;
			date_ = new JLabel();
			date_.setFont(date_.getFont().deriveFont(12f));
			date_.setForeground(header_fg_);
			date_.setHorizontalAlignment(SwingConstants.CENTER);
			day_ = new JLabel();
			day_.setForeground(header_fg_);
			day_.setFont(date_.getFont().deriveFont(16f).deriveFont(Font.BOLD));
			day_.setHorizontalAlignment(SwingConstants.CENTER);

			date_info_ = new JPanel(new BorderLayout());
			date_info_.add(date_, BorderLayout.NORTH);
			date_info_.add(day_, BorderLayout.CENTER);

			task_info_ = new JPanel();

			header_ = new JPanel();
			header_.setLayout(new BoxLayout(header_, BoxLayout.Y_AXIS));
			header_.add(date_info_);
			header_.add(task_info_);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int r, int c) {
			/* Update labels */
			Calendar cal = (Calendar) chosen_date_.clone();
			cal.set(Calendar.DAY_OF_WEEK, dow_);
			date_.setText(cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " "
					+ cal.get(Calendar.DAY_OF_MONTH));
			day_.setText(cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()));
			for (Task t : OfficeMaintenanceManager.instance().getTasksDueOnDay(cal)) {
				JLabel l = new JLabel();
				l.setBackground(Color.blue);
				l.setBorder(new LineBorder(Color.blue.darker()));
				task_info_.add(l);
			}

			if (c == selected_dow_) {
				task_info_.setBackground(selected_day_);
				this.setBackground(selected_day_);
			} else if (c == chosen_date_.get(Calendar.DAY_OF_WEEK)) {
				task_info_.setBackground(today_);
				normal_clr_ = today_;
				this.setBackground(normal_clr_);
			} else {
				task_info_.setBackground(normal_clr_);
				this.setBackground(normal_clr_);
			}
			if (r < 0)
				return header_;
			return this;
		}
	}

	private class MyTableModel extends AbstractTableModel {

		public int getColumnCount() {
			return 8;
		}

		public int getRowCount() {
			return 24;
		}

		public Object getValueAt(int r, int c) {
			if (c == 0) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, r);
				cal.set(Calendar.MINUTE, 0);
				return cal;
			}
			return null;
		}
	}

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JFrame f = new JFrame();

		JScrollPane view = new JScrollPane(new WeekTable(null));
		f.add(view);
		f.setSize(500, 500);
		f.setVisible(true);
	}
}

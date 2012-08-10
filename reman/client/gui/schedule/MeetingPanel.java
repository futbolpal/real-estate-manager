package reman.client.gui.schedule;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DateFormat;
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import reman.client.app.office_maintenance.Meeting;
import reman.client.app.office_maintenance.OfficeMaintenanceManager;
import reman.client.gui.GradientPanel;

public class MeetingPanel extends GradientPanel {
	private static Color event_top_ = new Color(168, 194, 225);
	private static Color event_bottom_ = new Color(201, 217, 236);
	private static Color event_top_selected_ = new Color(255, 219, 103);
	private static Color event_bottom_selected_ = new Color(255, 233, 163);

	private Meeting meeting_;
	private JLabel description_;
	private JPopupMenu menu_;
	private JLabel top_;
	private JLabel bottom_;

	public MeetingPanel(final WeekTable owner, Meeting m) {
		super(event_top_, event_bottom_, Direction.VERTICAL);
		MouseAdapter ma = new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				JLabel c = (JLabel) e.getSource();
				c.setText("...");
			}

			public void mouseExited(MouseEvent e) {
				JLabel c = (JLabel) e.getSource();
				c.setText(" ");
			}
		};
		MouseMotionAdapter mma = new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				JLabel c = (JLabel) e.getSource();
				if (c.getName().equals(BorderLayout.NORTH)) {
					int y = e.getY() + (int) MeetingPanel.this.getBounds().getY();
					Calendar start = owner.getTimeAtY(y);
					start.set(Calendar.DAY_OF_YEAR, meeting_.getStart().get(Calendar.DAY_OF_YEAR));
					if (start.before(meeting_.getEnd())) {
						meeting_.setStart(start);
						OfficeMaintenanceManager.instance().fireCalendarItemChanged(meeting_);
					}
				} else if (c.getName().equals(BorderLayout.SOUTH)) {
					Rectangle r = MeetingPanel.this.getBounds();
					int y = e.getY() + r.y + r.height;
					Calendar end = owner.getTimeAtY(y);
					end.set(Calendar.DAY_OF_YEAR, meeting_.getEnd().get(Calendar.DAY_OF_YEAR));
					if (end.before(meeting_.getEnd())) {
						meeting_.setEnd(end);
						OfficeMaintenanceManager.instance().fireCalendarItemChanged(meeting_);
					}
				}
			}
		};
		top_ = new JLabel(" ");
		top_.setHorizontalAlignment(SwingConstants.CENTER);
		top_.addMouseListener(ma);
		top_.addMouseMotionListener(mma);
		top_.setPreferredSize(new Dimension(-1, 10));
		top_.setName(BorderLayout.NORTH);
		top_.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		bottom_ = new JLabel(" ");
		bottom_.setHorizontalAlignment(SwingConstants.CENTER);
		bottom_.addMouseListener(ma);
		bottom_.addMouseMotionListener(mma);
		bottom_.setPreferredSize(new Dimension(-1, 10));
		bottom_.setName(BorderLayout.NORTH);
		bottom_.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));

		meeting_ = m;
		description_ = new JLabel();
		description_.setVerticalAlignment(SwingConstants.TOP);
		menu_ = new JPopupMenu();
		menu_.add(new JMenuItem(new AbstractAction("Delete Meeting") {
			public void actionPerformed(ActionEvent e) {
				OfficeMaintenanceManager.instance().removeMeeting(meeting_);
			}
		}));

		this.setLayout(new BorderLayout());
		this.add(description_, BorderLayout.CENTER);
		this.add(top_, BorderLayout.NORTH);
		this.add(bottom_, BorderLayout.SOUTH);
		this.setBorder(new LineBorder(event_top_, 2));
		this.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				MeetingPanel.this.setColors(event_top_selected_, event_bottom_selected_);
				MeetingPanel.this.setBorder(new LineBorder(event_top_selected_, 2));
			}

			public void focusLost(FocusEvent e) {
				if (e.isTemporary())
					return;
				MeetingPanel.this.setColors(event_top_, event_bottom_);
				MeetingPanel.this.setBorder(new LineBorder(event_top_, 2));
			}
		});
		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				MeetingPanel.this.requestFocusInWindow();
				if (e.getClickCount() == 2) {
					owner.getOwner().updateMeetingDetails(meeting_, true);
				} else if (SwingUtilities.isRightMouseButton(e)) {
					menu_.show(MeetingPanel.this, e.getX(), e.getY());
				} else {
					owner.getOwner().updateMeetingDetails(meeting_, false);
				}
			}
		});
		this.update();
	}

	public Meeting getMeeting() {
		return meeting_;
	}

	public void update() {
		String desc = new String("<HTML>");
		desc += "<B>" + meeting_.getName() + "</B><HR>";
		desc += "<font size=1>";
		desc += "Start: "
				+ DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(
						meeting_.getStart().getTime());
		desc += "<BR>";
		desc += "End: "
				+ DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(
						meeting_.getEnd().getTime());
		desc += "</HTML>";
		description_.setText(desc);
	}

	public boolean equals(Object o) {
		if (o instanceof MeetingPanel) {
			return ((MeetingPanel) o).meeting_ == this.meeting_;
		} else if (o instanceof Meeting) {
			return o == this.meeting_;
		}
		return false;
	}
}

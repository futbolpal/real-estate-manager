package reman.client.gui.schedule;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import reman.client.app.office_maintenance.Meeting;
import reman.client.gui.forms.MeetingFormPanel;

public class MeetingViewPanel extends JPanel {
	public static final String ID = "Meeting Info";

	private JScrollPane view_;

	public MeetingViewPanel() {
		this.setLayout(new BorderLayout());
	}

	public void setMeeting(Meeting a) {
		if (view_ != null)
			this.remove(view_);
		view_ = new JScrollPane();
		try {
			view_.setViewportView(new MeetingFormPanel(a, null, false));
		} catch (Exception e) {
			e.printStackTrace();
			view_.setViewportView(new JLabel("<html><i>Error</i></html>"));
		}
		this.add(view_, BorderLayout.CENTER);
		this.validate();
	}
}

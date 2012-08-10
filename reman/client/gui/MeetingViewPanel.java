package reman.client.gui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.noos.xing.mydoggy.DockedTypeDescriptor;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.ToolWindowType;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;

import reman.client.app.office_maintenance.Meeting;
import reman.client.gui.forms.MeetingFormPanel;

public class MeetingViewPanel extends JPanel {
	public static final String ID = "Meeting Info";

	private JPanel view_;

	public MeetingViewPanel() {
		this.setLayout(new BorderLayout());
	}

	public void setMeeting(Meeting a) {
		if (view_ != null)
			this.remove(view_);
		try {
			view_ = new MeetingFormPanel(a, null, false);
		} catch (Exception e) {
			e.printStackTrace();
			view_ = new JPanel();
			view_.add(new JLabel("<html><i>Error</i></html>"));
		}
		this.add(view_, BorderLayout.CENTER);
		this.validate();
	}
}

package reman.client.gui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import reman.client.app.office_maintenance.Task;
import reman.client.gui.forms.TaskFormPanel;

public class TaskViewPanel extends JPanel {
	public static final String ID = "Task Info";

	private JPanel view_;

	public TaskViewPanel() {
		this.setLayout(new BorderLayout());
	}

	public void setTask(Task a) {
		if (view_ != null)
			this.remove(view_);
		try {
			view_ = new TaskFormPanel(a, null, false);
		} catch (Exception e) {
			e.printStackTrace();
			view_ = new JPanel();
			view_.add(new JLabel("<html><i>Error</i></html>"));
		}
		this.add(view_, BorderLayout.CENTER);
		this.validate();
	}
}

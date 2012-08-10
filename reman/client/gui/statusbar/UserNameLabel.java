package reman.client.gui.statusbar;

import javax.swing.JLabel;

public class UserNameLabel extends JLabel {
	private Long id_;
	public UserNameLabel(Long id) {
		super();
		id_ = id;
	}
	
	public UserNameLabel(String text, Long id) {
		super(text);
		id_ = id;
	}
	
	public Long getID() {
		return id_;
	}
}

package reman.client.gui;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import reman.client.app.IconManager;
import reman.client.gui.statusbar.ChatPanel;

public class ChatOptionPanel extends JPanel {
	private JCheckBox show_timestamp_;
	private ChatPanel parent_;
	private Long id_;
	
	public ChatOptionPanel(ChatPanel parent, Long id) {
		parent_ = parent;
		id_ = id;
		show_timestamp_ = new JCheckBox("Show Timestamp");
		this.setLayout(new BorderLayout());
		this.add(show_timestamp_, BorderLayout.WEST);
		JLabel icon_label = new JLabel();
		icon_label.setToolTipText("Close");
		icon_label.setIcon(IconManager.instance().getIcon(16, "emblems", "emblem-unreadable.png"));
		this.add(icon_label, BorderLayout.EAST);
		icon_label.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				//close the chat box
				parent_.removeChatWindow(id_);
			}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
		});
	}
	
	public boolean showTimestamp() {
		return show_timestamp_.isSelected();
	}
}

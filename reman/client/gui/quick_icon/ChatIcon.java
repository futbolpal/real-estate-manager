package reman.client.gui.quick_icon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import reman.client.app.Framework;
import reman.client.gui.statusbar.ChatPanel;

public class ChatIcon extends JLabel {

	private JComponent component_;
	private ChatPanel parent_;
	private JPopupMenu popup_;

	public ChatIcon(ImageIcon icon, String text, JComponent panel, ChatPanel parent) {
		this(icon, text, panel, parent, 300, 200);
	}

	public ChatIcon(ImageIcon icon, final String text, JComponent panel, ChatPanel parent, int w,
			int h) {
		component_ = panel;
		popup_ = new JPopupMenu();
		popup_.add(component_);
		component_.setPreferredSize(new Dimension(w, h));
		//this.setPreferredSize(new Dimension(25, 25));
		this.setIcon(icon);
		parent_ = parent;
		this.setText(text + "  ");
		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				showChatBox();
				resetHighlight();
			}
			public void mouseEntered(MouseEvent e) {
				Framework.instance().setStatus("Chat: " + text);
			}
		});
	}

	public JComponent getChatBox() {
		return component_;
	}
	
	public void removeChatBox() {
		popup_.setVisible(false);
	}
	
	public void showChatBox() {
		popup_.show(ChatIcon.this, 0, -(int) component_.getPreferredSize().getHeight() - 9);
	}
	
	public void setHighlight() {
		this.setForeground(Color.orange);
	}
	
	public void resetHighlight() {
		this.setForeground(Color.black);
	}
}

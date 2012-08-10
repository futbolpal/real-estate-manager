package reman.client.gui.statusbar;

import java.awt.Component;
import java.awt.Dimension;
import java.sql.SQLException;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToolBar;

import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.CustomDockableDescriptor;

import reman.client.app.listeners.ChatMessageListener;
import reman.client.gui.ChatBoxPanel;
import reman.client.gui.quick_icon.ChatIcon;
import reman.common.database.UserManager;
import reman.common.database.UserManager.UserInfo;
import reman.common.messaging.ChatMessage;

public class ChatPanel extends CustomDockableDescriptor implements ChatMessageListener {
	private JToolBar panel_;
	private Hashtable<Long, ChatIcon> chat_windows_;
	private String current_user_;

	public ChatPanel(MyDoggyToolWindowManager manager) {
		super(manager, ToolWindowAnchor.BOTTOM);;
		panel_ = new JToolBar();
		panel_.setPreferredSize(new Dimension(300, 25));

		this.setAnchor(ToolWindowAnchor.BOTTOM, 1000);

		chat_windows_ = new Hashtable<Long, ChatIcon>();
		try {
			current_user_ = UserManager.instance().getCurrentUserInfo().getDisplayName();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void removeChatWindow(Long uid) {
		ChatIcon icon = chat_windows_.get(uid);
		icon.removeChatBox();
		panel_.remove(icon);
		panel_.validate();
		panel_.repaint();
		chat_windows_.remove(uid);
	}

	public void chatMessageReceived(ChatMessage m) {
		Long sender = m.getSender();
		if (!chat_windows_.containsKey(sender)) {
			createWindow(m.getSender());
		}
		ChatIcon icon = chat_windows_.get(sender);
		icon.setHighlight();
		ChatBoxPanel panel = (ChatBoxPanel) icon.getChatBox();
		panel.displayMessage(m);
	}

	public void displayWindow(Long other_user) {
		if (!chat_windows_.containsKey(other_user)) {
			createWindow(other_user);
		}
		ChatIcon icon = chat_windows_.get(other_user);
		//set focus or something
		icon.showChatBox();
	}

	private void createWindow(Long sender) {
		UserInfo info = null;
		try {
			info = UserManager.instance().getUserInfo(sender);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (info != null) {
			ChatBoxPanel chatBox = new ChatBoxPanel(this, current_user_, info.getDisplayName(), sender);
			ChatIcon icon = new ChatIcon(new ImageIcon(ChatIcon.class
					.getResource("internet-group-chat.png")), info.getDisplayName(), chatBox, this);
			chat_windows_.put(sender, icon);
			panel_.add(icon);
			panel_.validate();
		}
	}

	public JComponent getRepresentativeAnchor(Component c) {
		return panel_;
	}

	public void updateRepresentativeAnchor() {
		panel_.validate();
	}

}

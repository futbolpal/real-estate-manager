package reman.client.gui.statusbar;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.CustomDockableDescriptor;

import reman.client.app.Framework;
import reman.client.app.listeners.ClientStatusListener;
import reman.client.gui.AnimationListener;
import reman.client.gui.BorderFadeAnimation;
import reman.common.database.UserManager;
import reman.common.database.UserManager.UserInfo;
import reman.common.messaging.ClientStatusMessage;

public class CoworkersOnlinePanel extends CustomDockableDescriptor implements ClientStatusListener,
		AnimationListener {
	private int users_online_;
	private ClientStatusDetailsPopup popup_;
	private BorderFadeAnimation animation_timer_;
	private JLabel label_;

	public CoworkersOnlinePanel(MyDoggyToolWindowManager owner) {
		super(owner, ToolWindowAnchor.BOTTOM);
		ImageIcon user_icon = new ImageIcon(this.getClass().getResource("system-users.png"));
		label_ = new JLabel();
		label_.setIcon(user_icon);
		label_.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (popup_ == null)
					popup_ = new ClientStatusDetailsPopup();
				popup_.show();
			}
		});
		Framework.instance().addClientStatusListener(this);
		this.clientStatusChanged(null);

		try {
			animation_timer_ = new BorderFadeAnimation(label_, this, null);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public synchronized void clientStatusChanged(ClientStatusMessage m) {
		if (m == null) {
			try {
				UserManager.instance().refreshUserTable();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			ArrayList<UserInfo> users = UserManager.instance().getUsersOnline();
			this.users_online_ = users.size();
		} else {
			Color c = null;
			switch (m.getUserStatus()) {
			case LOGGED_IN:
				this.users_online_++;
				c = Color.GREEN;
				break;
			case LOGGED_OUT:
				this.users_online_--;
				c = Color.RED;
				break;
			}
			animation_timer_.setStartColor(c);
			animation_timer_.start();
		}

		/* Exclude me from the online user count */
		label_.setText("Co-workers online: (" + (users_online_ - 1) + ")");
	}

	public void animationDone() {

	}

	private class ClientStatusDetailsPopup extends JPopupMenu {
		public ClientStatusDetailsPopup() {
		}

		public void show() {
			this.removeAll();

			JLabel header = new JLabel("Co-workers Online");
			header.setOpaque(true);
			header.setBackground(Color.darkGray);
			header.setForeground(Color.white);
			this.add(header);

			/* Add items for each user */
			ArrayList<UserInfo> users = UserManager.instance().getUsersOnline();
			for (UserInfo u : users) {
				if (u.getID() == UserManager.instance().getCurrentUserID())
					continue;

				UserNameLabel u_lbl = new UserNameLabel(u.getDisplayName(), u.getID());

				/* Set icon */
				switch (u.getStatus()) {
				case LOGGED_IN:
					break;
				case IDLE:
					u_lbl.setText("<html><i>" + u_lbl.getText() + "</i></html>");
					u_lbl.setForeground(Color.DARK_GRAY);
					break;
				}

				u_lbl.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						UserNameLabel label = (UserNameLabel) e.getSource();
						Framework.instance().getWindow().getChatPanel().displayWindow(label.getID());
					}
				});
				this.add(u_lbl);
			}

			int height = (int) this.getPreferredSize().getHeight();
			super.show(label_, 0, -height);
		}

	}

	public JComponent getRepresentativeAnchor(Component arg0) {
		return label_;
	}

	@Override
	public void updateRepresentativeAnchor() {
		// TODO Auto-generated method stub

	}
}

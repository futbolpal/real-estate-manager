package reman.client.gui.statusbar;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.CustomDockableDescriptor;

import reman.client.app.Framework;
import reman.client.app.listeners.DboChangedListener;
import reman.client.gui.AnimationListener;
import reman.client.gui.BorderFadeAnimation;
import reman.common.database.UserManager;
import reman.common.database.UserManager.UserInfo;
import reman.common.messaging.DboChangedMessage;

public class DboChangedPanel extends CustomDockableDescriptor implements DboChangedListener,
		AnimationListener {
	private JLabel label_;
	private DboChangedDetailPopup popup_;
	private BorderFadeAnimation animation_timer_;

	public DboChangedPanel(MyDoggyToolWindowManager owner) {
		super(owner, ToolWindowAnchor.BOTTOM);
		ImageIcon user_icon = new ImageIcon(this.getClass().getResource("edit-find-replace.png"));
		label_ = new JLabel();
		label_.setIcon(user_icon);
		label_.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (popup_ == null)
					popup_ = new DboChangedDetailPopup();
				popup_.show();
			}
		});
		Framework.instance().addDboChangedListener(this);
		label_.setText("<Waiting for events>");
		label_.setToolTipText("Click for details");

		try {
			animation_timer_ = new BorderFadeAnimation(label_, this, Color.RED);
		} catch (Exception e1) {
		}
	}

	public synchronized void dboChangedEvent(DboChangedMessage m) {
		String changed_by;
		try {
			UserInfo ui = UserManager.instance().getUserInfo(m.getSourceUserID());
			changed_by = ui.getDisplayName();
		} catch (SQLException e) {
			changed_by = "<Unknown>";
		}
		label_.setText("A change was made by: " + changed_by);
		animation_timer_.start();
	}

	public void animationDone() {
		label_.setText("<Waiting for events>");
	}

	private class DboChangedDetailPopup extends JPopupMenu {
		public DboChangedDetailPopup() {
		}

		public void show() {
			this.removeAll();

			JLabel header = new JLabel("Recent changes");
			header.setOpaque(true);
			header.setBackground(Color.darkGray);
			header.setForeground(Color.white);
			this.add(header);

			int height = (int) this.getPreferredSize().getHeight();
			super.show(label_, 0, -height);
		}

	}

	public JComponent getRepresentativeAnchor(Component c) {
		System.out.println(c);
		return label_;
	}

	public void updateRepresentativeAnchor() {
	}
}

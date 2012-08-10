package reman.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.border.EtchedBorder;

import org.flexdock.dockbar.Dockbar;
import org.flexdock.dockbar.DockbarManager;
import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;

import reman.client.gui.quick_icon.QuickIcon;
import reman.client.gui.statusbar.CoworkersOnlinePanel;
import reman.client.gui.statusbar.DboChangedPanel;

public class FrameworkStatusBar extends JMenuBar {
	private JMenuBar status_bar_;
	private JMenuBar quick_bar_;
	private JLabel notice_label_;
	private Dockbar dock_;

	public FrameworkStatusBar(FrameworkWindow owner) {
		status_bar_ = new JMenuBar();
		quick_bar_ = new JMenuBar();

		status_bar_.add(new CoworkersOnlinePanel());
		status_bar_.add(Box.createHorizontalStrut(10));
		status_bar_.add(new DboChangedPanel());
		status_bar_.add(Box.createHorizontalStrut(10));
		status_bar_.add(notice_label_ = new TextNotificationPanel());


		dock_ = DockbarManager.getInstance(owner).getBottomBar();
		Dockable force_show = DockingManager.registerDockable(new JLabel(), "");
		dock_.dock(force_show);

		this.setPreferredSize(new Dimension(-1, 30));
		this.setLayout(new BorderLayout());
		this.setBorder(new EtchedBorder());
		this.add(status_bar_, BorderLayout.WEST);
		this.add(dock_, BorderLayout.CENTER);
		this.add(quick_bar_, BorderLayout.EAST);
	}

	public void setStatus(String text) {
		notice_label_.setText(text);
	}

	public void addQuickIcon(QuickIcon q) {
		quick_bar_.add(q);
	}
}

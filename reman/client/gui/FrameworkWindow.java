package reman.client.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UnsupportedLookAndFeelException;

import org.noos.xing.mydoggy.Content;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.TabbedContentManagerUI.TabLayout;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.content.MyDoggyMultiSplitContentManagerUI;

import reman.client.app.Framework;
import reman.client.gui.quick_icon.QuickIcon;
import reman.client.gui.search.SearchBar;
import reman.client.gui.statusbar.ChatPanel;
import reman.client.gui.statusbar.CoworkersOnlinePanel;
import reman.client.gui.statusbar.DboChangedPanel;
import reman.client.gui.statusbar.MemoryMonitorDockableDescriptor;
import reman.client.gui.statusbar.QuickIconPanel;

public class FrameworkWindow extends JFrame {
	private ArrayList<Content> contents_;
	private MyDoggyToolWindowManager tool_manager_;
	private FrameworkMenuBar menu_;
	private TextNotificationPanel notice_label_;
	private CoworkersOnlinePanel coworkers_;
	private DboChangedPanel dbo_changed_;
	private QuickIconPanel quick_icon_;
	private ChatPanel chat_panel_;
	private GUIBuildManager gui_manager_;
	private SearchBar search_bar_;

	public FrameworkWindow() {
		contents_ = new ArrayList<Content>();
		gui_manager_ = new GUIBuildManager(this);

		tool_manager_ = new MyDoggyToolWindowManager();
		MyDoggyMultiSplitContentManagerUI splitUI = new MyDoggyMultiSplitContentManagerUI();
		tool_manager_.getContentManager().setContentManagerUI(splitUI);
		tool_manager_.getToolWindowManagerDescriptor().setNumberingEnabled(false);

		splitUI.setTabLayout(TabLayout.SCROLL);
		splitUI.setShowAlwaysTab(false);

		/*
		Content a = tool_manager_.getContentManager().addContent("a", "a", null, new JLabel("A"));
		Content b = tool_manager_.getContentManager().addContent("b", "b", null, new JLabel("B"), "b",
				new MultiSplitConstraint(AggregationPosition.BOTTOM));
		Content c = tool_manager_.getContentManager().addContent("c", "c", null, new JLabel("C"), "c",
				new MultiSplitConstraint(a, AggregationPosition.RIGHT));
		Content d = tool_manager_.getContentManager().addContent("d", "d", null, new JLabel("D"), "d",
				new MultiSplitConstraint(a, AggregationPosition.LEFT));
		Content e = tool_manager_.getContentManager().addContent("e", "e", null, new JLabel("E"), "e",
				new MultiSplitConstraint(d));
				*/

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(tool_manager_);

		coworkers_ = new CoworkersOnlinePanel(tool_manager_);
		coworkers_.setAvailable(true);

		dbo_changed_ = new DboChangedPanel(tool_manager_);
		dbo_changed_.setAvailable(true);

		MemoryMonitorDockableDescriptor memory = new MemoryMonitorDockableDescriptor(tool_manager_,
				ToolWindowAnchor.BOTTOM);
		memory.setAvailable(true);

		notice_label_ = new TextNotificationPanel(tool_manager_);
		notice_label_.setAvailable(true);

		quick_icon_ = new QuickIconPanel(tool_manager_);
		quick_icon_.setAvailable(true);
		quick_icon_.addQuickIcon(new QuickIcon(new ImageIcon(QuickIcon.class
				.getResource("accessories-calculator.png")), "Calculator", new CalculatorPanel()));
		quick_icon_.addQuickIcon(new QuickIcon(new ImageIcon(QuickIcon.class
				.getResource("office-calendar.png")), "Calendar", new CalendarPanel(false)));
		quick_icon_.addQuickIcon(new QuickIcon(new ImageIcon(QuickIcon.class
				.getResource("accessories-text-editor.png")), "Notepad", new NotepadPanel()));

		chat_panel_ = new ChatPanel(tool_manager_);
		chat_panel_.setAvailable(true);
		Framework.instance().addChatMessageListener(chat_panel_);

		search_bar_ = new SearchBar(tool_manager_);
		search_bar_.setAvailable(true);

		this.setJMenuBar(menu_ = new FrameworkMenuBar(tool_manager_));
		this.setSize(1000, 1000);
		this.setExtendedState(MAXIMIZED_BOTH);
		this.setVisible(true);
		this.setTitle("Real Estate Manager");
	}

	public void hideContent(Content t) {
		tool_manager_.getContentManager().removeContent(t);
	}

	public Content registerContent(String id, String title, Icon i, JComponent c, String tip,
			boolean visible, Object... constraints) {
		Content ct = tool_manager_.getContentManager().addContent(id, title, i, c, tip, constraints);
		contents_.add(ct);
		if (!visible)
			hideContent(ct);
		return ct;
	}

	public void build(FrameworkWindow w) {
		gui_manager_.build();
	}

	public ArrayList<Content> getContents() {
		return contents_;
	}

	public Content getContentByComponent(Component a) {
		return tool_manager_.getContentManager().getContentByComponent(a);
	}

	public void setStatus(String s) {
		notice_label_.setText(s);
	}

	public MyDoggyToolWindowManager getToolManager() {
		return tool_manager_;
	}

	public GUIBuildManager getContentManager() {
		return gui_manager_;
	}

	public ChatPanel getChatPanel() {
		return chat_panel_;
	}

	public FrameworkMenuBar getFrameworkMenuBar() {
		return menu_;
	}

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new FrameworkWindow();
	}
}

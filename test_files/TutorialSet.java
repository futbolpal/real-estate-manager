package test_files;

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.noos.xing.mydoggy.Content;
import org.noos.xing.mydoggy.ContentManager;
import org.noos.xing.mydoggy.ContentManagerUIListener;
import org.noos.xing.mydoggy.DockedTypeDescriptor;
import org.noos.xing.mydoggy.FloatingTypeDescriptor;
import org.noos.xing.mydoggy.SlidingTypeDescriptor;
import org.noos.xing.mydoggy.TabbedContentManagerUI;
import org.noos.xing.mydoggy.TabbedContentUI;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowActionHandler;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.ToolWindowManager;
import org.noos.xing.mydoggy.ToolWindowManagerDescriptor;
import org.noos.xing.mydoggy.ToolWindowType;
import org.noos.xing.mydoggy.event.ContentManagerUIEvent;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.CustomDockableDescriptor;

import reman.client.gui.TextNotificationPanel;

public class TutorialSet {
	private JFrame frame;
	private ToolWindowManager toolWindowManager;

	protected void setUp() {
		initComponents();
		initToolWindowManager();
	}

	protected void start() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Activate "Debug" Tool
				ToolWindow debugTool = toolWindowManager.getToolWindow("Debug");
				debugTool.setActive(true);

				frame.setVisible(true);
			}
		});
	}

	protected void initComponents() {
		// Init the frame
		this.frame = new JFrame("Sample App...");
		this.frame.setSize(640, 480);
		this.frame.setLocation(100, 100);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create a simple JMenuBar
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				frame.dispose();
			}
		});
		fileMenu.add(exitMenuItem);
		menuBar.add(fileMenu);
		this.frame.setJMenuBar(menuBar);
	}

	protected void initToolWindowManager() {
		// Create a new instance of MyDoggyToolWindowManager passing the frame.
		MyDoggyToolWindowManager myDoggyToolWindowManager = new MyDoggyToolWindowManager();
		this.toolWindowManager = myDoggyToolWindowManager;
		ToolWindowManagerDescriptor descriptor = toolWindowManager.getToolWindowManagerDescriptor();
		descriptor.setNumberingEnabled(false);
		descriptor.setCornerComponent(ToolWindowManagerDescriptor.Corner.SOUTH_WEST, new JLabel("SW"));
		descriptor.setCornerComponent(ToolWindowManagerDescriptor.Corner.SOUTH_EAST, new JLabel("SE"));
		descriptor.setCornerComponent(ToolWindowManagerDescriptor.Corner.NORD_WEST, new JLabel("NW"));
		descriptor.setCornerComponent(ToolWindowManagerDescriptor.Corner.NORD_EAST, new JLabel("NE"));

		toolWindowManager.registerToolWindow("Tool 6", "Title 6", null, new JButton("Hello World 6"),
				ToolWindowAnchor.BOTTOM);

		TextNotificationPanel mmd = new TextNotificationPanel();

		// Register a Tool.
		toolWindowManager.registerToolWindow("Debug", // Id
				"Debug Tool", // Title
				null, // Icon
				new JButton("Debug Tool"), // Component
				ToolWindowAnchor.LEFT); // Anchor

		setupDebugTool();

		// Made all tools available
		for (ToolWindow window : toolWindowManager.getToolWindows())
			window.setAvailable(true);

		initContentManager();

		// Add myDoggyToolWindowManager to the frame. MyDoggyToolWindowManager is an extension of a JPanel
		this.frame.getContentPane().add(myDoggyToolWindowManager);
	}

	protected void setupDebugTool() {
		ToolWindow debugTool = toolWindowManager.getToolWindow("Debug");

		DockedTypeDescriptor dockedTypeDescriptor = (DockedTypeDescriptor) debugTool
				.getTypeDescriptor(ToolWindowType.DOCKED);
		dockedTypeDescriptor.setDockLength(300);
		dockedTypeDescriptor.setPopupMenuEnabled(true);
		JMenu toolsMenu = dockedTypeDescriptor.getToolsMenu();
		toolsMenu.add(new AbstractAction("Hello World!!!") {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame, "Hello World!!!");
			}
		});
		dockedTypeDescriptor.setToolWindowActionHandler(new ToolWindowActionHandler() {
			public void onHideButtonClick(ToolWindow toolWindow) {
				JOptionPane.showMessageDialog(frame, "Hiding...");
				toolWindow.setVisible(false);
			}
		});
		dockedTypeDescriptor.setAnimating(true);
		dockedTypeDescriptor.setPreviewEnabled(true);
		dockedTypeDescriptor.setPreviewDelay(1500);
		dockedTypeDescriptor.setPreviewTransparentRatio(0.4f);

		SlidingTypeDescriptor slidingTypeDescriptor = (SlidingTypeDescriptor) debugTool
				.getTypeDescriptor(ToolWindowType.SLIDING);
		slidingTypeDescriptor.setEnabled(false);
		slidingTypeDescriptor.setTransparentMode(true);
		slidingTypeDescriptor.setTransparentRatio(0.8f);
		slidingTypeDescriptor.setTransparentDelay(0);
		slidingTypeDescriptor.setAnimating(true);

		FloatingTypeDescriptor floatingTypeDescriptor = (FloatingTypeDescriptor) debugTool
				.getTypeDescriptor(ToolWindowType.FLOATING);
		floatingTypeDescriptor.setEnabled(true);
		floatingTypeDescriptor.setLocation(150, 200);
		floatingTypeDescriptor.setSize(320, 200);
		floatingTypeDescriptor.setModal(false);
		floatingTypeDescriptor.setTransparentMode(true);
		floatingTypeDescriptor.setTransparentRatio(0.2f);
		floatingTypeDescriptor.setTransparentDelay(1000);
		floatingTypeDescriptor.setAnimating(true);
	}

	protected void initContentManager() {
		JTree treeContent = new JTree();

		ContentManager contentManager = toolWindowManager.getContentManager();
		Content content = contentManager.addContent("Tree Key", "Tree Title", null, // An icon
				treeContent);
		content.setToolTipText("Tree tip");

		setupContentManagerUI();

	}

	protected void setupContentManagerUI() {
		// By default a TabbedContentManagerUI is installed. 
		TabbedContentManagerUI contentManagerUI = (TabbedContentManagerUI) toolWindowManager
				.getContentManager().getContentManagerUI();
		contentManagerUI.setShowAlwaysTab(true);
		contentManagerUI.setTabPlacement(TabbedContentManagerUI.TabPlacement.BOTTOM);
		contentManagerUI.addContentManagerUIListener(new ContentManagerUIListener() {
			public boolean contentUIRemoving(ContentManagerUIEvent event) {
				return JOptionPane.showConfirmDialog(frame, "Are you sure?") == JOptionPane.OK_OPTION;
			}

			public void contentUIDetached(ContentManagerUIEvent event) {
				JOptionPane.showMessageDialog(frame, "Hello World!!!");
			}
		});

		TabbedContentUI contentUI = (TabbedContentUI) toolWindowManager.getContentManager().getContent(
				0).getContentUI();
		// Or you can use :
		// TabbedContentUI contentUI = contentManagerUI.getContentUI(toolWindowManager.getContentManager().getContent(0));
		// without the need of the cast

		contentUI.setCloseable(true);
		contentUI.setDetachable(true);
		contentUI.setTransparentMode(true);
		contentUI.setTransparentRatio(0.7f);
		contentUI.setTransparentDelay(1000);
	}

	public static class MemoryMonitorDockableDescriptor extends CustomDockableDescriptor {

		public MemoryMonitorDockableDescriptor(MyDoggyToolWindowManager manager, ToolWindowAnchor anchor) {
			super(manager, anchor);
			this.setAvailable(true);
		}

		public void updateRepresentativeAnchor() {
		}

		public JComponent getRepresentativeAnchor(Component parent) {
			if (this.representativeAnchor == null) {
				JPanel p = new JPanel();
				p.setLayout(new TableLayout(new double[][] { { 120, 1, 17 }, { -1 } }));
				p.add(new JLabel("HELLO?"), "0,0,FULL,FULL");
				this.representativeAnchor = p;
			}
			System.out.println("HERE?");
			return this.representativeAnchor;
		}
	}

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		TutorialSet test = new TutorialSet();
		try {
			test.setUp();
			test.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
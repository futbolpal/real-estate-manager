package reman.client.gui.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.noos.xing.mydoggy.AggregationPosition;
import org.noos.xing.mydoggy.Content;
import org.noos.xing.mydoggy.MultiSplitConstraint;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.CustomDockableDescriptor;

import reman.client.app.Framework;
import reman.client.app.IconManager;
import reman.client.app.office_maintenance.Meeting;
import reman.client.app.office_maintenance.Task;
import reman.client.basetypes.Agent;
import reman.common.database.exceptions.DatabaseException;

public class SearchBar extends CustomDockableDescriptor {

	private final String SEARCH_ID = "Search Results";
	private JTextField search_box_;
	private Content search_content_;
	private JPanel panel_;

	public SearchBar(MyDoggyToolWindowManager owner) {
		super(owner, ToolWindowAnchor.BOTTOM);
		search_content_ = null;
		panel_ = new JPanel();
		FlowLayout layout = new FlowLayout();
		layout.setVgap(0);
		panel_.setLayout(layout);
		panel_.setPreferredSize(new Dimension(300, 25));
		this.setAnchor(ToolWindowAnchor.BOTTOM, 1000);

		search_box_ = new JTextField(15);
		search_box_.setPreferredSize(new Dimension(80, 18));
		JButton search_button = new JButton("Search");
		search_button.setPreferredSize(new Dimension(85, 20));
		search_button.setIcon(IconManager.instance().getIcon(16, "actions", "system-search.png"));
		JLabel advanced = new JLabel("<html><u>Advanced...</u></html>");
		advanced.setForeground(Color.blue);

		panel_.add(search_box_);
		panel_.add(search_button);
		panel_.add(advanced);
		search_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!search_box_.getText().isEmpty()) {
					doSearch(search_box_.getText());
				}
			}
		});
		advanced.setCursor(new Cursor(Cursor.HAND_CURSOR));
		advanced.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				//simply open a blank search window
				doSearch("");
			}

			public void mouseEntered(MouseEvent arg0) {
				
			}

			public void mouseExited(MouseEvent arg0) {
				
			}

			public void mousePressed(MouseEvent arg0) {
			}

			public void mouseReleased(MouseEvent arg0) {
			}
		});
		search_box_.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (!search_box_.getText().isEmpty())) {
					doSearch(search_box_.getText());
				}

			}

			public void keyReleased(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
			}
		});
		//Testing only
		JButton test_button = new JButton("Test");
		test_button.setPreferredSize(new Dimension(85, 20));
		test_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
						testData();
			}});
		//panel_.add(test_button);
	}

	private void doSearch(String text) {
		//Content c = Framework.instance().getWindow().getToolManager().getContentManager().getContent(SEARCH_ID);
		
		/*if (search_content_ == null) {
			ResultPanel results = new ResultPanel();
			results.doSearch(text);
			search_content_ = Framework.instance().getWindow().registerContent(SEARCH_ID,
					"Search Results", null, results, "", true,
					new MultiSplitConstraint(AggregationPosition.RIGHT));
		} else {
			ResultPanel results = (ResultPanel) search_content_.getComponent();
			results.doSearch(text);
			search_content_.setSelected(true);
		}*/
		ResultPanel results = new ResultPanel();
		results.doSearch(text);
		try {
		search_content_ = Framework.instance().getWindow().registerContent(SEARCH_ID,
				"Search Results", null, results, "", true,
				new MultiSplitConstraint(AggregationPosition.RIGHT));
		
		} catch (java.lang.IllegalArgumentException e) {
			//search content already exists
			if (search_content_ != null)
				search_content_.setComponent(results);
		}
		search_content_.setSelected(true);
	}

	public JComponent getRepresentativeAnchor(Component arg0) {
		return panel_;
	}

	public void updateRepresentativeAnchor() {
		panel_.validate();
	}
	
	private void testData() {
		Task t = new Task("Task #1");
		t.setDescription("This is a bug that is assigned to William Sanville.");
		Task t2 = new Task("Task #2");
		t2.setDescription("This is a bug that is assigned to Scott.");
		Task t3 = new Task("Task #3");
		t3.setDescription("This is a bug that is assigned to William Sanville and Scott Mitchell.");
		Agent a = new Agent();
		a.setFirstName("William");
		a.setLastName("Sanville");
		a.setMiddleName("J");
		a.setTitleName("Sir");
		Agent a2 = new Agent();
		a2.setFirstName("Joe");
		a2.setLastName("Santos");
		Meeting m = new Meeting("Project Status Update");
		m.setDescription("Meeting to discuss progress on the CL-P redesign project.");
		try {
			t.commit();
			t2.commit();
			t3.commit();
			a.commit();
			a2.commit();
			m.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("done...hope it worked");
	}
}

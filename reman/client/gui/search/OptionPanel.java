package reman.client.gui.search;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import reman.client.app.search.NotSearchableException;
import reman.client.app.search.SearchManager;
import reman.client.app.search.SearchResult;

/**
 * GUI component that allows a user to enter in search text, as well as specify additional
 * parameters, such as which class to search and whether or not to search for exact matches.
 * @author Will
 *
 */
public class OptionPanel extends JPanel {
	private JTextField search_box_;
	private JCheckBox partial_matches_;
	private JComboBox search_types_;
	private JButton search_button_;
	private JButton help_button_;
	private ResultPanel parent_panel;
	private final String SEARCH_TEXT = "Search";
	private final String SEARCH_CLICKED = "Searching...";
	
	public OptionPanel(ResultPanel parent) {
		parent_panel = parent;
		JLabel lbl1 = new JLabel("Search for: ");
		JLabel lbl2 = new JLabel("Look in: ");
		search_box_ = new JTextField(20);
		partial_matches_ = new JCheckBox("Exact Matches", true);
		search_types_ = new JComboBox();
		addItems();
		search_button_ = new JButton(SEARCH_TEXT);
		search_button_.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (!search_box_.getText().isEmpty())
      		doSearch(search_box_.getText());
      	  }
      	});
		help_button_ = new JButton("Help");
		help_button_.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	JOptionPane.showMessageDialog(null, "Search Tips...");
  	  }
  	});
		this.add(lbl1);
		this.add(search_box_);
		this.add(lbl2);
		this.add(search_types_);
		this.add(partial_matches_);
		this.add(search_button_);
		//this.add(help_button_);
		this.setPreferredSize(new Dimension(275, 25));
	}
	
	public void doSearch(String text) {
		search_box_.setText(text);
		switchButton(true);
		ArrayList<SearchResult> results = null;
		if (search_types_.getSelectedIndex() == 0) {
			results = SearchManager.instance().search(search_box_.getText(), 
					partial_matches_.isSelected());
		} else {
			ComboBoxItem item = (ComboBoxItem)search_types_.getSelectedItem();
			try {
				results = SearchManager.instance().search(item.getItemClass(), search_box_.getText(), 
						partial_matches_.isSelected());
			} catch (NotSearchableException e) {
				
				e.printStackTrace();
			}
		}
		switchButton(false);
		parent_panel.refresh(search_box_.getText(), results);
	}
	
	/**
	 * Populates a drop down list with all classes that can be searched (all classes
	 * that have been registered with the SearchManager).
	 */
	private void addItems() {
		search_types_.addItem("Everything");
		Enumeration<Class> e = SearchManager.instance().getClasses();
		while (e.hasMoreElements()) {
			search_types_.addItem(new ComboBoxItem(e.nextElement()));
		}
	}
	
	public void setSearchText(String t) {
		search_box_.setText(t);
	}
	
	private void switchButton(boolean searching) {
		search_button_.setEnabled(!searching);
		if (searching) {
			search_button_.setText(SEARCH_CLICKED);
		} else {
			search_button_.setText(SEARCH_TEXT);
		}
	}
}

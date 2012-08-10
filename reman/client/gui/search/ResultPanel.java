package reman.client.gui.search;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import reman.client.app.search.SearchResult;

/**
 * GUI component that shows the results of a search.
 * @author Will
 *
 */
public class ResultPanel extends JPanel {
	private SearchTableModel model_;
	private JTable table_;
	private OptionPanel search_options_;

	public ResultPanel() {
		this("", new ArrayList<SearchResult>());
	}

	public ResultPanel(String text, ArrayList<SearchResult> results) {
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);

		model_ = new SearchTableModel(results);
		table_ = new JTable(model_);
		model_.addHeaderMouseListener(table_);
		model_.addRowMouseListener(table_);
		search_options_ = new OptionPanel(this);
		search_options_.setSearchText(text);
		JScrollPane scroll_pane = new JScrollPane(table_);
		scroll_pane.setPreferredSize(new Dimension(300, 500));
		this.add(search_options_, BorderLayout.NORTH);
		this.add(scroll_pane, BorderLayout.CENTER);
		this.setSize(500, 275);
	}

	/**
	 * Updates GUI components to show the text that was searched for and the list of
	 * results returned by the SearchManager.
	 * @param text The text that the user searched for.
	 * @param results The results of the search.
	 */
	public void refresh(String text, ArrayList<SearchResult> results) {
		search_options_.setSearchText(text);
		model_.refresh(results);
	}

	public void doSearch(String text) {
		if (!text.isEmpty())
			search_options_.doSearch(text);
	}
}

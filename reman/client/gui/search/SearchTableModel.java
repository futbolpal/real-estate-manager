package reman.client.gui.search;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import reman.client.app.search.SearchResult;

/**
 * GUI component that allows a list of SearchResults to be displayed graphically in a table to the
 * end user. This enables a user to sort the results based on any column of the table. It also
 * allows a user to double click a search result to retrieve the actual object and view more information
 * about the object.
 * @author Will
 *
 */
public class SearchTableModel extends AbstractTableModel {

	private final String[] COLUMN_NAMES = { "Description", "Type", "Modified Last", "Modified By" };
	private ArrayList<SearchResult> results_;
	private boolean ascending_;
	private int sort_index_;
	
	public SearchTableModel() {
		results_ = new ArrayList<SearchResult>();
		ascending_ = true;
		sort_index_ = 0;
	}
	
	public SearchTableModel(ArrayList<SearchResult> r) {
		results_ = r;
		ascending_ = true;
		sort_index_ = 0;
		Collections.sort(results_, new ResultComparer(ascending_, sort_index_));
	}
	
	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public int getRowCount() {
		return results_.size();
	}
	
	@Override
	public String getColumnName(int column) {
    return COLUMN_NAMES[column];
  }
	
	public void addResult(SearchResult r) {
		results_.add(r);
		Collections.sort(results_, new ResultComparer(ascending_, sort_index_));
		this.fireTableDataChanged();
	}

	public SearchResult getResult(int index) {
		return results_.get(index);
	}
	
	@Override
	public Object getValueAt(int arg0, int arg1) {
		SearchResult r = results_.get(arg0);
		switch (arg1)
		{
		case 0:
			return r.getName();
		case 1:
			return r.getType();
		case 2:
			return r.getModifiedDate();
		case 3:
			return r.getModifiedBy();
		}
		return null;
	}

	public Object getRowObject(int row) {
		return results_.get(row);
	}
	
	public void sort(int column) {
		if (sort_index_ == column)
			ascending_ = !ascending_;
		else
		{
			ascending_ = true;
			sort_index_ = column;
		}
			
		Collections.sort(results_, new ResultComparer(ascending_, sort_index_));
		this.fireTableDataChanged();
	}
	
	public void refresh(ArrayList<SearchResult> r) {
		if (r != null)
			results_ = r;
		else
			results_ = new ArrayList<SearchResult>();
		Collections.sort(results_, new ResultComparer(ascending_, sort_index_));
		this.fireTableDataChanged();
	}
	
	public void addHeaderMouseListener(final JTable table) {
    table.getTableHeader().addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent event) {
        int tableColumn = table.columnAtPoint(event.getPoint());
        int modelColumn = table.convertColumnIndexToModel(tableColumn);
        sort(modelColumn);
      }
    });
  }
	
	/* Allow user to retrieve object when double clicking on table row */
	public void addRowMouseListener(final JTable table) {
    table.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent event) {
      	if (event.getClickCount() < 2)
          return;
      	Point p = event.getPoint();
      	int row = table.rowAtPoint(p);
      	if (row < 0)
      		return;
        SearchTableModel model = (SearchTableModel)table.getModel();
        Object obj = model.getRowObject(row);
        if (obj instanceof SearchResult) {
        	SearchResult r = (SearchResult)obj;
        	JOptionPane.showMessageDialog(null, "Retrieve object id = " + r.getId() + " pid = " + r.getPid());
        }
      }
    });
  }
}

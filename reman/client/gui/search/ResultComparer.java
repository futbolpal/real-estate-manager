package reman.client.gui.search;

import java.util.Comparator;

import reman.client.app.search.SearchResult;

/**
 * The ResultComparer class is used to sort a list of SearchResult objects based on a given field.
 * @author Will
 *
 */
public class ResultComparer implements Comparator<SearchResult> {
	private Boolean ascending_;
	private int sort_index_;
	
	/**
	 * Creates a new ResultComparer and sets whether or not it should sort the list ascending or not,
	 * along with an integer to represent which field of the SearchResult object it should sort on.
	 * @param asc Whether or not the comparer should sort ascending.
	 * @param index The index of the field used to sort the objects.
	 */
	public ResultComparer(Boolean asc, int index) {
		ascending_ = asc;
		sort_index_ = index;
	}
	
	public int compare(SearchResult r1, SearchResult r2) {
		int result = 0;
		switch (sort_index_)
		{
		case 0:
			result = r1.getName().compareTo(r2.getName());
			break;
		case 1:
			result = r1.getType().compareTo(r2.getType());
			break;
		case 2:
			result = r1.getModifiedDate().compareTo(r2.getModifiedDate());
			break;
		case 3:
			result = r1.getModifiedBy().compareTo(r2.getModifiedBy());
			break;
		
		}
		
		if (!ascending_)
			result = -result;
		return result;
	}
}

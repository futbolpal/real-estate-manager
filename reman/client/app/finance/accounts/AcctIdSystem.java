package reman.client.app.finance.accounts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import reman.client.basetypes.IntegerRange;
import reman.common.database.DatabaseObject;

/**
 * Regulates the unique id distribution amongst accounts.  This class uses IntegerRange to decrease on the storage, and increase
 * processing speed when ids are relatively clustered.  This class provides means to reserve ids based on AcctType (currently 20000 ids per type).
 * @author Scott
 *
 */
public class AcctIdSystem extends DatabaseObject {
	public static final int ID_BREDTH = 20000;
	public static final int START_ID = 10000;

	/**
	 * This collection maintains all account ids that are currently in use
	 */
	private TreeSet<IntegerRange> used_ids_;

	/**
	 * All account ids that are available to be assigned to Asset accounts
	 */
	private TreeSet<IntegerRange> asset_available_ids_;

	/**
	 * All account ids that are available to be assigned to Liability accounts
	 */
	private TreeSet<IntegerRange> liability_available_ids_;

	/**
	 * All account ids that are available to be assigned to Equity accounts
	 */
	private TreeSet<IntegerRange> equity_available_ids_;

	public AcctIdSystem() {
		asset_available_ids_ = initAvailableIds(START_ID, ID_BREDTH + START_ID);
		liability_available_ids_ = initAvailableIds(ID_BREDTH + START_ID, 2 * ID_BREDTH + START_ID);
		equity_available_ids_ = initAvailableIds(2 * ID_BREDTH + START_ID, 3 * ID_BREDTH + START_ID);
		used_ids_ = new TreeSet<IntegerRange>(getAcctIdComparitor());
	}

	/**
	 * 
	 * @param acct_id
	 * @return True if <code>acct_id</code> is currently being used.
	 */
	public boolean isAcctIdInUse(int acct_id) {
		for (IntegerRange ir : this.used_ids_) {
			if (ir.isInRange(acct_id))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param acct_id
	 * @return The corresponding free set of ids, or null if id out of reserved range
	 */
	private TreeSet<IntegerRange> getReservedSet(int acct_id) {
		if (acct_id >= START_ID && acct_id < ID_BREDTH + START_ID)
			return asset_available_ids_;
		else if (acct_id >= ID_BREDTH + START_ID && acct_id < 2 * ID_BREDTH + START_ID)
			return liability_available_ids_;
		else if (acct_id >= 2 * ID_BREDTH + START_ID && acct_id < 3 * ID_BREDTH + START_ID)
			return equity_available_ids_;
		return null;
	}

	/**
	 * When an Account is registered with the finance engine and uses an id, this method is executed with that id.
	 * @param acct_id Id that is now in use.
	 */
	public void useId(int acct_id) {
		this.addIdToSet(acct_id, used_ids_);
		TreeSet<IntegerRange> reserved_id_set = this.getReservedSet(acct_id);
		if (reserved_id_set != null)
			this.removeIdFromSet(acct_id, reserved_id_set);
	}

	/**
	 * When an Account is unregistered with the finance engine and uses and id, this method is executed with that id.
	 * @param acct_id Id that is no longer being used.
	 */
	public void unUseId(int acct_id) {
		this.removeIdFromSet(acct_id, used_ids_);
		TreeSet<IntegerRange> reserved_id_set = this.getReservedSet(acct_id);
		if (reserved_id_set != null)
			this.addIdToSet(acct_id, reserved_id_set);
	}

	/**
	 * Removes <code>acct_id</code> from <code>set</code>.
	 * @param un_registered_acct
	 */
	private void removeIdFromSet(Integer acct_id, TreeSet<IntegerRange> set) {
		if (set.isEmpty() || acct_id == null)
			return;

		IntegerRange remove_ir = null, lower_range = null, upper_range = null;
		for (IntegerRange ir : set) {
			if (ir.isInRange(acct_id)) {
				remove_ir = ir;

				Integer lower_bound = ir.getBegin();
				Integer upper_bound = ir.getEnd();

				if (ir.getBegin().equals(acct_id))
					lower_bound = ir.getBegin() + 1;
				if (ir.getEnd().equals(acct_id))
					upper_bound = ir.getEnd() - 1;

				if (lower_bound <= acct_id - 1)
					lower_range = new IntegerRange(lower_bound, acct_id - 1);
				if (upper_bound >= acct_id + 1)
					upper_range = new IntegerRange(acct_id + 1, upper_bound);

				break;
			}
		}

		if (remove_ir != null)
			set.remove(remove_ir);
		if (lower_range != null)
			set.add(lower_range);
		if (upper_range != null)
			set.add(upper_range);

		this.mergeSharedRanges(set);
	}

	private TreeSet<IntegerRange> getFreeKeySet(AcctType a_type) {
		if (AcctType.getGenericType(a_type) == AcctType.ASSET)
			return this.asset_available_ids_;
		else if (AcctType.getGenericType(a_type) == AcctType.LIABILITY)
			return this.liability_available_ids_;
		else if (AcctType.getGenericType(a_type) == AcctType.EQUITY)
			return this.equity_available_ids_;
		return null;
	}

	/**
	 * This will incorporate <code>acct_id</code> into <code>set</code> collection, and attempt to merge and adjacent ranges in <code>set</code>.
	 * @param acct_id
	 */
	private void addIdToSet(Integer acct_id, TreeSet<IntegerRange> set) {

		/*add a singleton range and allow merge to clean up*/
		set.add(new IntegerRange(acct_id, acct_id));

		/*clean up potential IntegerRanges which share bounds*/
		this.mergeSharedRanges(set);
	}

	/**
	 * Obtain lowest available id from the collection corresponding to <code>a_type</code>.
	 * If a free id can not be found from 'a_type', then the next free integer id (out side of reserved ids for each AcctType) is returned
	 * @param a_type
	 * @return
	 */
	public Integer getNextAcctId(AcctType a_type) {
		Integer return_id = null;
		IntegerRange return_range = null;
		TreeSet<IntegerRange> return_q = this.getFreeKeySet(a_type);

		if (!return_q.isEmpty()) {
			/*obtain the first available id*/
			return_range = return_q.first();
			if (return_range != null) {
				return_id = return_range.getBegin();
				/*if the range is exhausted remove it, otherwise take the id found out of the range*/
				/*if (return_range.getBegin() == return_range.getEnd())
					return_q.remove(return_range);
				else
					return_range.setBegin(return_id + 1);*/
			}
		}

		/*this is extreme case, if all available ids are used, then find an alternative integer to give*/
		if (return_id == null) {
			IntegerRange total_reserved_ids = new IntegerRange(START_ID, START_ID + 3 * ID_BREDTH);
			Integer curr_id = new Integer(1);
			for (IntegerRange ir : this.used_ids_) {
				if (!ir.isInRange(curr_id)) {
					/*if curr_id is a number in reserved AcctType id range, find alternate id*/
					if (total_reserved_ids.isInRange(curr_id))
						curr_id = total_reserved_ids.getEnd() + 1;
					else
						return curr_id;
				} else
					curr_id = new Integer(ir.getEnd() + 1);
			}
		}

		return return_id;
	}

	/**
	 * Merges adjacent IntegerRanges which share bounds
	 * @param set
	 */
	private void mergeSharedRanges(TreeSet<IntegerRange> set) {
		if (set.size() > 1) {
			ArrayList<IntegerRange> cpy_set = null;
			do {
				/*since set is sorted, only need to traverse tree once to clean up overlapping ranges*/
				cpy_set = new ArrayList<IntegerRange>(set);
				for (int i = 0; i < set.size() - 1; i++) {
					IntegerRange curr_range = cpy_set.get(i);
					IntegerRange next_range = cpy_set.get(i + 1);

					/*if two adjacent set boundaries intersect, merge the sets*/
					if (next_range.getBegin().equals(curr_range.getEnd() + 1)
							|| curr_range.isInRange(next_range)) {
						Integer lowest_bound = (curr_range.getBegin() < next_range.getBegin()) ? curr_range
								.getBegin() : next_range.getBegin();
						Integer highest_bound = (curr_range.getEnd() > next_range.getEnd()) ? curr_range
								.getEnd() : next_range.getEnd();
						IntegerRange new_range = new IntegerRange(lowest_bound, highest_bound);
						set.remove(curr_range);
						set.remove(next_range);
						set.add(new_range);
					}
				}
			} while (cpy_set.size() != set.size());
		}
	}

	/**
	 * Ascending order by beginning integer value
	 * @return
	 */
	private Comparator<IntegerRange> getAcctIdComparitor() {
		return new Comparator<IntegerRange>() {
			public int compare(IntegerRange o1, IntegerRange o2) {
				return o1.getBegin() - o2.getBegin();
			}
		};
	}

	/**
	 * Initialize key lists. Adds integers from begin to end to list.
	 * @param begin
	 * @param end
	 * @param array
	 */
	private TreeSet<IntegerRange> initAvailableIds(int begin, int end) {
		TreeSet<IntegerRange> array = new TreeSet<IntegerRange>(getAcctIdComparitor());
		array.add(new IntegerRange(begin, end - 1));

		return array;
	}
}
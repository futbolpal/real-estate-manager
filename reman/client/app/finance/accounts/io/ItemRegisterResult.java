package reman.client.app.finance.accounts.io;

import java.util.HashSet;

/**
 * This class is used internally by the <code>AccountImportExport</code> class to ensure proper importing of Account objects and
 * associated AcctActionCategory objects.
 * @author Scott
 *
 */
public class ItemRegisterResult {

	private HashSet<String> dependent_item_names_;
	private FileItem item_;
	private boolean registered_;

	public boolean equals(Object o) {
		if (o instanceof ItemRegisterResult) {
			ItemRegisterResult result = (ItemRegisterResult) o;
			return item_.equals(result);
		}
		return false;
	}

	public ItemRegisterResult(FileItem item) {
		this.item_ = item;
		this.dependent_item_names_ = new HashSet<String>();
		this.registered_ = false;
	}

	public String getItemName() {
		return this.item_.getName();
	}

	public boolean isRegistered() {
		return this.registered_;
	}

	public void setRegistered() {
		this.registered_ = true;
	}

	/**
	 * Duplicate names not allowed.
	 * @param dependent_item_name
	 * @return
	 */
	public boolean addDependentItem(String dependent_item_name) {
		return this.dependent_item_names_.add(dependent_item_name);
	}

	public boolean removeDependentItem(String dependent_item_name) {
		return this.dependent_item_names_.remove(dependent_item_name);
	}

	public HashSet<String> getDependentNames() {
		return new HashSet<String>(this.dependent_item_names_);
	}

	public FileItem getItem() {
		return this.item_;
	}
}

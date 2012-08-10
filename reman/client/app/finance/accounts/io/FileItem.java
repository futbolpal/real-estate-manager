package reman.client.app.finance.accounts.io;

import java.util.HashSet;

/**
 * This class is used as a place holder for AcctActionCategory and Account items stored on file.  This is the interface needed
 * to import/export Accounts and AcctActionCategory objects from/to file.
 * @author Scott
 *
 */
public class FileItem {
	private String name_;
	private Integer id_;
	private String type_;
	private String normal_balance_;
	private double balance_;
	private String parent_name_;
	private HashSet<String> owners_;
	private String cash_category_;
	private String close_to_acct_;
	private String close_to_cat_;
	private String close_from_cat_;
	private String close_to_journal_;
	private String time_scale_;

	public FileItem(String name, Integer id, String type, String normal_balance, double balance,
			String parent_name, String close_to_acct, String close_from_cat, String close_to_cat,
			String close_to_journal, String time_scale, HashSet<String> owners, String cash_category) {
		this.name_ = name;
		this.id_ = id;
		this.type_ = type;
		this.normal_balance_ = normal_balance;
		this.balance_ = balance;
		this.parent_name_ = parent_name;
		this.close_to_acct_ = close_to_acct;
		this.close_from_cat_ = close_from_cat;
		this.close_to_cat_ = close_to_cat;
		this.close_to_journal_ = close_to_journal;
		this.time_scale_ = time_scale;
		this.cash_category_ = cash_category;
		if (owners == null)
			owners_ = new HashSet<String>();
		else
			owners_ = owners;
	}

	public String getName() {
		return this.name_;
	}

	public Integer getId() {
		return this.id_;
	}

	public String getType() {
		return this.type_;
	}

	public String getNormalBalance() {
		return this.normal_balance_;
	}

	public double getBalance() {
		return this.balance_;
	}

	public String getParent() {
		return this.parent_name_;
	}

	public HashSet<String> getOwners() {
		return new HashSet<String>(this.owners_);
	}

	public String getCloseToCategory() {
		return this.close_to_cat_;
	}

	public String getCloseFromCategory() {
		return this.close_from_cat_;
	}

	public String getCloseToAcct() {
		return this.close_to_acct_;
	}

	public String getCloseToJournal() {
		return this.close_to_journal_;
	}

	public String getTimeScale() {
		return this.time_scale_;
	}

	public String getCashCategory() {
		return this.cash_category_;
	}

	public boolean equals(Object o) {
		if ((o instanceof FileItem)) {
			FileItem fi = (FileItem) o;
			if (this.name_.equals(fi.name_)
					&& ((this.parent_name_ == null && fi.parent_name_ == null) || (this.parent_name_ != null && this.parent_name_
							.equals(fi.parent_name_))) && fi.owners_.equals(this.owners_))
				return true;
		}
		return false;
	}
}

package reman.client.basetypes;

import java.sql.Timestamp;

public abstract class Person extends BusinessEntity {
	private String title_;
	private String first_;
	private String middle_;
	private String last_;
	private Gender gender_;
	private Timestamp dob_;
	private long user_id_;

	public Person() {
		title_ = new String();
		first_ = new String();
		middle_ = new String();
		last_ = new String();
		user_id_ = -1;
	}

	public String getFirstName() {
		return first_;
	}

	public String getMiddleName() {
		return middle_;
	}

	public String getLastName() {
		return last_;
	}

	public String getTitle() {
		return title_;
	}

	public String getName() {
		return title_ + " " + first_ + " " + middle_ + " " + last_;
	}

	public Gender getGender() {
		return gender_;
	}

	public Timestamp getDOB() {
		return dob_;
	}

	@Override
	public void setName(String name) {
		System.err.println("Person.class: Use individual name set methods");
	}

	public void setFirstName(String fname) {
		first_ = fname;
	}

	public void setLastName(String lname) {
		last_ = lname;
	}

	public void setMiddleName(String mname) {
		middle_ = mname;
	}

	public void setTitleName(String tname) {
		title_ = tname;
	}

	public void setGender(Gender g) {
		gender_ = g;
	}

	public void setDOB(Timestamp dob) {
		dob_ = dob;
	}

	public void setUserID(long id) throws Exception {
		if (this.user_id_ < 0)
			throw new Exception("This object is already assigned");
		this.user_id_ = id;
	}

	public long getUserID() {
		return this.user_id_;
	}
}

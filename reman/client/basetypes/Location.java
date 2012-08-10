package reman.client.basetypes;

import java.util.ArrayList;

import reman.common.database.DatabaseObject;

public class Location extends DatabaseObject {
	private ArrayList<String> address_;
	private String city_;
	private State state_;
	private Country country_;
	private int zip_;

	public Location() {
		this.address_ = new ArrayList<String>();
	}

	public void addAddressLine(String address) {
		address_.add(address);
	}

	public void setState(State s) {
		state_ = s;
	}

	public void setCountry(Country c) {
		country_ = c;
	}

	public void setZip(int zip) {
		zip_ = zip;
	}

	public void setCity(String city) {
		city_ = city;
	}

	public State getState() {
		return state_;
	}

	public ArrayList<String> getAdddress() {
		return address_;
	}

	public Country getCountry() {
		return country_;
	}

	public int getZip() {
		return zip_;
	}
	
	public String getShortAddress() {
		if (this.address_.size() > 0)
			return this.address_.get(0);
		return name_;
	}

	public String getCity() {
		return city_;
	}

	public String getMailingAddressHTML() {
		String mailing = "<HTML>" + this.getName() + "<BR>";
		for (String s : address_)
			mailing += s + "<BR>";
		mailing += city_ + "<BR>";
		mailing += state_ + " " + zip_ + "<BR>";
		mailing += country_;
		mailing += "</HTML>";
		return mailing;
	}

	public String toString() {
		String str = this.getName() + ", ";
		for (String s : address_)
			str += s + ", ";
		str += city_ + ", ";
		str += state_ + " " + zip_ + ", ";
		str += country_;
		return str;
	}
}

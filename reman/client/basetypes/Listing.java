package reman.client.basetypes;

import java.util.ArrayList;

public class Listing extends RealEstateAction {
	private ArrayList<Commission> pot_commissions_;
	private double list_price_;

	public Listing(Property prop) {
		super(prop);
		this.pot_commissions_ = new ArrayList<Commission>();
		this.list_price_ = 0;
	}

	public void addCommission(Commission c) {
		this.pot_commissions_.add(c);
	}

	public ArrayList<Commission> getCommissions() {
		return this.pot_commissions_;
	}

	public double getPrice() {
		return this.list_price_;
	}

	public void setListPrice(double price) {
		this.list_price_ = price;
	}
}

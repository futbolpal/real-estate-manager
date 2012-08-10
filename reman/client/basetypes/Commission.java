package reman.client.basetypes;

import reman.common.database.DatabaseObject;

public class Commission extends DatabaseObject {
	private Agent agent_;
	private double percent_amt_;
	private double flat_amt_;

	private Commission() {
		this.percent_amt_ = 0;
		this.flat_amt_ = 0;
	}

	public Commission(Agent a) {
		this();
		this.agent_ = a;
	}

	public void setAgent(Agent a) {
		this.agent_ = a;
	}

	public Agent getAgent() {
		return agent_;
	}

	public void setPercent(double percent) {
		this.percent_amt_ = percent;
	}

	public double getPercent() {
		return percent_amt_;
	}

	public void setFlatAmount(double amt) {
		this.flat_amt_ = amt;
	}

	public double getFlatAmount() {
		return flat_amt_;
	}

	public static Commission createFlatRateCommission(Agent a, double amt) {
		Commission c = new Commission();
		c.agent_ = a;
		c.flat_amt_ = amt;
		return c;
	}

	public static Commission createPercentRateCommission(Agent a, double amt) {
		Commission c = new Commission();
		c.agent_ = a;
		c.percent_amt_ = amt;
		return c;
	}
	/*
	 * Remax %5 fee taxen before split
	 * E & O Withheld (Error and Ommission)
	 * Rapp -> commission amount received is split 30% (because of slow times for agent)*/

}

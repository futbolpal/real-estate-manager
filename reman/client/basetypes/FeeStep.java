package reman.client.basetypes;

import reman.common.database.DatabaseObject;

public class FeeStep extends DatabaseObject {
	private DoubleRange amt_applicable_;
	private boolean percentile_range_;
	private boolean percential_amt_;
	private double amt_;

	/**
	 * 
	 * @param gross_amt_applicable
	 * @param amt
	 * @param is_percentile_range If <code>amt_applicable</code> is a percentile range a flat amount range.
	 * @param is_percentile_amt If <code>amt</code> should be considered as a percentile when computing fee step, or as a flat fee.
	 */
	public FeeStep(DoubleRange amt_applicable, double amt, boolean is_percentile_range,
			boolean is_percentile_amt) {
		this.amt_applicable_ = amt_applicable;
		this.amt_ = amt;
		this.percentile_range_ = is_percentile_range;
		this.percential_amt_ = is_percentile_amt;
	}

	public boolean isPercentileStep() {
		return this.percential_amt_;
	}

	public void setPercentileStep(boolean percent_step) {
		this.percential_amt_ = percent_step;
	}

	public double getStepAmount() {
		return this.amt_;
	}

	public void setStepAmount(double amt) {
		this.amt_ = amt;
	}

	public boolean isPercentileRange() {
		return this.percentile_range_;
	}

	public void setPercentileRange(boolean percentile) {
		this.percentile_range_ = percentile;
	}

	/**
	 * Obtain the final contribution from this fee step toward the containing fee.
	 * @param total_amt The total amount which the fee step is applying to
	 * @return Obtain the final contribution from this fee step toward the containing fee.
	 */
	public double calculate(double total_amt) {
		DoubleRange applicable_range;
		double result = 0;
		if (percentile_range_) {
			applicable_range = new DoubleRange(amt_applicable_.getBegin() * total_amt, amt_applicable_
					.getEnd()
					* total_amt);
		} else {
			applicable_range = new DoubleRange(amt_applicable_.getBegin(), amt_applicable_.getEnd());
		}

		if (applicable_range.isInRange(total_amt)) {
			if (percential_amt_) {
				result = total_amt * this.amt_;
			} else {
				result = this.amt_;
			}
			
			/*TODO: prorate?
			 * 			partial charge?*/
		}

		return result;
	}
}

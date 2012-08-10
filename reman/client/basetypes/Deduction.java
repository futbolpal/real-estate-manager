package reman.client.basetypes;


public class Deduction extends Expense {

	protected Deduction() {
	}

	public Deduction(String name, String descripiton, TimeRange when_effective) {
		super(name, descripiton, when_effective);
	}

	@Override
	public boolean add(ExpenseItem item) {
		if (item instanceof DeductionItem)
			return super.add(item);
		return false;
	}

	@Override
	public boolean remove(ExpenseItem item) {
		if (item instanceof DeductionItem)
			return super.remove(item);
		return false;
	}
}

package reman.client.basetypes;

import java.util.ArrayList;

import reman.client.app.finance.journals.JournalEntryLineItem;
import reman.common.database.DatabaseObject;

public class Expense extends DatabaseObject {

	private ArrayList<ExpenseItem> items_;
	private TimeRange effective_time_;

	protected Expense() {
	}

	public Expense(String name, String descripiton, TimeRange when_effective) {
		this.name_ = name;
		this.description_ = descripiton;
		this.effective_time_ = when_effective;
		this.items_ = new ArrayList<ExpenseItem>();
	}

	public void setEffectiveTime(TimeRange range) {
		this.effective_time_ = range;
	}

	public TimeRange getEffectiveTime() {
		return this.effective_time_;
	}

	public boolean add(ExpenseItem item) {
		return this.items_.add(item);
	}

	public boolean remove(ExpenseItem item) {
		return this.items_.remove(item);
	}

	public ArrayList<ExpenseItem> getItems() {
		return new ArrayList<ExpenseItem>(this.items_);
	}

	public ArrayList<JournalEntryLineItem> evaluate() {

		ArrayList<JournalEntryLineItem> line_items = new ArrayList<JournalEntryLineItem>();

		for (ExpenseItem item : items_) {
			if (item.getEffectiveTime().isActive()) {
				line_items.addAll(item.evaluate());
			}
		}

		return line_items;
	}
}

package reman.client.basetypes;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;

import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.LoggedInException;
import reman.common.database.exceptions.NotPersistedException;

public class ExpenseManager extends DatabaseObject {
	private ArrayList<Expense> default_expenses_;
	private ArrayList<Fee> default_fees_;

	public ExpenseManager() {
		default_expenses_ = new ArrayList<Expense>();
		default_fees_ = new ArrayList<Fee>();
	}

	public boolean register(Fee f) throws DatabaseException, SQLException {
		this.retrieve();

		boolean success = false;
		try {
			if (this.lock()) {
				if (!default_fees_.contains(f))
					success = default_fees_.add(f);

				if (success)
					this.commit();
			}
		} catch (LoggedInException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			this.unlock();
		}
		return success;
	}

	public boolean register(Expense ex) throws DatabaseException, SQLException {
		this.retrieve();

		boolean success = false;
		try {
			if (this.lock()) {
				if (!default_expenses_.contains(ex))
					success = default_expenses_.add(ex);

				if (success)
					this.commit();
			}
		} catch (LoggedInException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			this.unlock();
		}
		return success;
	}

	public ArrayList<Fee> getDefaultFees() throws DatabaseException, SQLException {
		this.retrieve();

		return new ArrayList<Fee>(this.default_fees_);
	}

	public ArrayList<Deduction> getDefaultDeductions() throws DatabaseException, SQLException {
		this.retrieve();

		ArrayList<Deduction> deductions = new ArrayList<Deduction>();

		for (Expense e : this.default_expenses_) {
			if (e instanceof Deduction)
				deductions.add((Deduction) e);
		}

		return deductions;
	}

	public ArrayList<Expense> getDefaultExpenses() throws DatabaseException, SQLException {
		this.retrieve();

		ArrayList<Expense> expenses = new ArrayList<Expense>();

		for (Expense e : this.default_expenses_) {
			if (e.getClass() == Expense.class)
				expenses.add((Deduction) e);
		}

		return expenses;
	}

	public ArrayList<Expense> getAllExpenses() throws DatabaseException, SQLException {
		this.retrieve();

		return new ArrayList<Expense>(this.default_expenses_);
	}

	public boolean unRegister(Expense ex) throws DatabaseException, SQLException {
		this.retrieve();

		boolean success = false;
		try {
			if (this.lock()) {
				success = default_expenses_.remove(ex);

				if (success)
					this.commit();
			}
		} catch (LoggedInException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			this.unlock();
		}
		return success;
	}

	public boolean unRegister(Fee f) throws DatabaseException, SQLException {
		this.retrieve();

		boolean success = false;
		try {
			if (this.lock()) {
				success = default_fees_.remove(f);

				if (success)
					this.commit();
			}
		} catch (LoggedInException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			this.unlock();
		}
		return success;
	}

	@Override
	public void retrieve() throws DatabaseException, SQLException {
		try {
			super.retrieve();
		} catch (NotPersistedException e) {//ignore
		}
	}

}

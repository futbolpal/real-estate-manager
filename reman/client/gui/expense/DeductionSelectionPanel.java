package reman.client.gui.expense;

import java.sql.SQLException;
import java.util.Collection;

import reman.client.basetypes.Deduction;
import reman.common.database.OfficeProjectManager;
import reman.common.database.exceptions.DatabaseException;

public class DeductionSelectionPanel extends ExpenseSelectorPanel<Deduction> {

	public DeductionSelectionPanel(String title, Collection<Deduction> selected, int width, int heigth)
			throws DatabaseException, SQLException {
		super(title, OfficeProjectManager.instance().getCurrentProject().getExpenseManager()
				.getDefaultDeductions(), selected, width, heigth);
	}
}

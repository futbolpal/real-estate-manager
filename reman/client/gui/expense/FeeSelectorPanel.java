package reman.client.gui.expense;

import java.sql.SQLException;
import java.util.Collection;

import reman.client.basetypes.Fee;
import reman.common.database.OfficeProjectManager;
import reman.common.database.exceptions.DatabaseException;

public class FeeSelectorPanel extends ExpenseSelectorPanel<Fee> {

	public FeeSelectorPanel(String title, Collection<Fee> selected, int width, int heigth) throws DatabaseException, SQLException {
		super(title, OfficeProjectManager.instance().getCurrentProject().getExpenseManager()
				.getDefaultFees(), selected, width, heigth);
	}
}

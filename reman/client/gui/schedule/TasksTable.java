package reman.client.gui.schedule;

import java.text.DateFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import reman.client.app.office_maintenance.OfficeMaintenanceManager;
import reman.client.app.office_maintenance.Task;

public class TasksTable extends JTable {

	public TasksTableModel model_;

	public TasksTable() {
		this.setModel(model_ = new TasksTableModel());
	}

	public void update() {
		((TasksTableModel) this.getModel()).update();
	}

	private class TasksTableModel extends AbstractTableModel {
		private String[] columns_ = { "Due Date", "Name", "Description", "Priority", "Category",
				"Progresss", "Assigned To", "Created By" };

		public TasksTableModel() {

		}

		public int getColumnCount() {
			return columns_.length;
		}

		public int getRowCount() {
			return OfficeMaintenanceManager.instance().getTasks().size();
		}

		public String getColumnName(int c) {
			return columns_[c];
		}

		public void update() {
			this.fireTableDataChanged();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Task m = OfficeMaintenanceManager.instance().getTasks().get(rowIndex);
			switch (columnIndex) {
			case 0:
				return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(
						new Date(m.getDueDate().getTime()));
			case 1:
				return m.getName();
			case 2:
				return m.getDescription();
			case 3:
				return m.getPriority().toString();
			case 4:
				return m.getCategory() == null ? "Unclassified" : m.getCategory();
			case 5:
				return m.getPercentComplete();
			case 6:
				return "";
			case 7:
				return "";
			}
			return null;
		}
	}
}

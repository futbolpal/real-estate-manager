package reman.client.gui;

import java.text.DateFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import reman.client.app.office_maintenance.Meeting;
import reman.client.app.office_maintenance.OfficeMaintenanceManager;

public class MeetingsTable extends JTable {

	private MeetingsTableModel model_;

	public MeetingsTable() {
		this.setModel(model_ = new MeetingsTableModel());
	}

	public void update() {
		((MeetingsTableModel) this.getModel()).update();
	}

	private class MeetingsTableModel extends AbstractTableModel {
		private String[] columns_ = { "Start Time", "End Time", "Location", "Created By" };

		public MeetingsTableModel() {

		}

		public int getColumnCount() {
			return columns_.length;
		}

		public int getRowCount() {
			return OfficeMaintenanceManager.instance().getMeetings().size();
		}

		public String getColumnName(int c) {
			return columns_[c];
		}

		public void update() {
			this.fireTableDataChanged();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Meeting m = OfficeMaintenanceManager.instance().getMeetings().get(rowIndex);
			switch (columnIndex) {
			case 0:
				return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(
						new Date(m.getEffectiveTimeRange().getBegin().getTime()));
			case 1:
				return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(
						new Date(m.getEffectiveTimeRange().getEnd().getTime()));
			case 2:
				return m.getLocation().getName();
			case 3:
				return "";
			}
			return null;
		}
	}

}

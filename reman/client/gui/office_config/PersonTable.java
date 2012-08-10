package reman.client.gui.office_config;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import com.lowagie.text.Font;

import reman.client.basetypes.Agent;
import reman.client.basetypes.ContactEntity;
import reman.client.basetypes.Email;
import reman.client.basetypes.Person;
import reman.client.basetypes.Phone;
import reman.client.gui.DboTable;
import reman.common.database.DatabaseObject;
import reman.common.database.OfficeProjectManager;
import reman.common.database.exceptions.DatabaseException;

public class PersonTable extends DboTable {

	public PersonTable() {
		this.setModel(new PersonTableModel());
		this.setRowHeight(25);
		this.getColumnModel().getColumn(4).setCellRenderer(new ListCellRenderer());
		this.getColumnModel().getColumn(4).setCellEditor(new ListCellEditor());
		this.getColumnModel().getColumn(5).setCellRenderer(new ListCellRenderer());
		this.getColumnModel().getColumn(5).setCellEditor(new ListCellEditor());
	}

	public DatabaseObject getDatabaseObjectAtRow(int r) {
		if (r < 0)
			return null;
		return OfficeProjectManager.instance().getCurrentProject().getOffice().getMembers().get(r);
	}

	public int getRowOfDatabaseObject(DatabaseObject o) {
		return OfficeProjectManager.instance().getCurrentProject().getOffice().getMembers().indexOf(o);
	}

	public void commitListOwner() throws SQLException, DatabaseException {
		OfficeProjectManager.instance().getCurrentProject().getOffice().commit();
	}

	public boolean isCellEditable(int r, int c) {
		return true;
	}

	public void update() {
		((PersonTableModel) this.getModel()).fireTableDataChanged();
	}

	private class ListCellRenderer implements TableCellRenderer {
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			ArrayList<ContactEntity> e = (ArrayList<ContactEntity>) value;
			final JComboBox c = new JComboBox(e.toArray());
			c.insertItemAt("<New Entry>", 0);
			c.setSelectedIndex(0);
			if (isSelected) {
				c.setBackground(PersonTable.this.getSelectionBackground());
				c.setBackground(PersonTable.this.getSelectionForeground());
			}
			return c;
		}
	}

	private class ListCellEditor extends DefaultCellEditor {

		public ListCellEditor() {
			super(new JComboBox());
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
				final int row, final int column) {
			ArrayList<ContactEntity> list = (ArrayList<ContactEntity>) value;
			final JComboBox c = new JComboBox(list.toArray());
			c.insertItemAt("<New Entry>", 0);
			c.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (c.getSelectedIndex() != 0)
						return;
					FocusListener f = new FocusListener() {
						public void focusGained(FocusEvent e) {
							JTextField f = (JTextField) e.getSource();
							if (f.getForeground().equals(Color.GRAY)) {
								f.setText("");
								f.setForeground(Color.BLACK);
								f.setFont(f.getFont().deriveFont(Font.NORMAL));
							}
						}

						public void focusLost(FocusEvent e) {
							JTextField f = (JTextField) e.getSource();
							if (f.getText().equals("")) {
								f.setText(f.getName());
								f.setForeground(Color.GRAY);
								f.setFont(f.getFont().deriveFont(Font.ITALIC));
							}
						}
					};
					final JPopupMenu m = new JPopupMenu();
					final JTextField name = new JTextField("Name", 20);
					name.setName(name.getText());
					name.addFocusListener(f);
					name.setForeground(Color.GRAY);
					name.setFont(name.getFont().deriveFont(Font.ITALIC));
					final JTextField desc = new JTextField("Description", 20);
					desc.setName(desc.getText());
					desc.addFocusListener(f);
					desc.setForeground(Color.GRAY);
					desc.setFont(desc.getFont().deriveFont(Font.ITALIC));
					final JTextField value = new JTextField("Value", 20);
					value.setName(value.getText());
					value.addFocusListener(f);
					value.setForeground(Color.GRAY);
					value.setFont(value.getFont().deriveFont(Font.ITALIC));
					final JButton done = new JButton("Done");
					done.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							ContactEntity ce = null;
							switch (column) {
							case 4:
								ce = new Phone();
								((Phone) ce).setNumber(value.getText());
								break;
							case 5:
								ce = new Email();
								((Email) ce).setAddress(value.getText());
								break;
							}
							if (ce != null) {
								ce.setName(name.getText());
								ce.setDescription(desc.getText());

								Person p = (Person) PersonTable.this.getDatabaseObjectAtRow(row);
								p.addContact(ce);
								try {
									p.commit();
									PersonTable.this.update();
									m.setVisible(false);
								} catch (Exception e1) {
									done.setEnabled(false);
									m.add("Commit failed");
								}
							}
						}
					});
					m.add(name);
					m.add(desc);
					m.add(value);
					m.add(done);
					m.show(c, 0, c.getHeight());
				}
			});
			return c;
		}
	}

	private class PersonTableModel extends AbstractTableModel {
		private String[] columns_ = { "Broker of Record", "Last Name", "First Name", "Role", "Phone",
				"Email" };

		public int getColumnCount() {
			return columns_.length;
		}

		public String getColumnName(int i) {
			return columns_[i];
		}

		public int getRowCount() {
			return OfficeProjectManager.instance().getCurrentProject().getOffice().getMembers().size();
		}

		public Class<?> getColumnClass(int c) {
			switch (c) {
			case 0:
				return Boolean.class;
			case 1:
				return String.class;
			case 2:
				return String.class;
			case 3:
				return String.class;
			case 4:
				return ArrayList.class;
			case 5:
				return ArrayList.class;
			}
			return null;
		}

		public void setValueAt(Object o, int r, int c) {
			Person d = (Person) getDatabaseObjectAtRow(r);
			switch (c) {
			case 0:
				if (d instanceof Agent) {
					OfficeProjectManager.instance().getCurrentProject().getOffice().setBrokerOfRecord(
							(Agent) d);
					try {
						OfficeProjectManager.instance().getCurrentProject().getOffice().commit();
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (DatabaseException e) {
						e.printStackTrace();
					}
					PersonTable.this.update();
				}
				break;
			case 1:
				d.setLastName(o.toString());
				return;
			case 2:
				d.setFirstName(o.toString());
				return;
			}
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Person pp = (Person) getDatabaseObjectAtRow(rowIndex);
			switch (columnIndex) {
			case 0:
				return OfficeProjectManager.instance().getCurrentProject().getOffice().getBrokerOfRecord() == pp;
			case 1:
				return pp.getLastName();
			case 2:
				return pp.getFirstName();
			case 3:
				return pp.getClass().getSimpleName();
			case 4: {
				ArrayList<ContactEntity> phones = new ArrayList<ContactEntity>();
				ArrayList<ContactEntity> all = pp.getContacts();
				for (ContactEntity e : all) {
					if (e instanceof Phone)
						phones.add(e);
				}
				return phones;
			}
			case 5: {
				ArrayList<ContactEntity> emails = new ArrayList<ContactEntity>();
				ArrayList<ContactEntity> all = pp.getContacts();
				for (ContactEntity e : all) {
					if (e instanceof Email)
						emails.add(e);
				}
				return emails;
			}

			}
			return null;
		}
	}
}

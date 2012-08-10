package reman.client.app.finance.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctActionCategory;
import reman.client.app.finance.accounts.AcctAmount;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.finance.journals.Journal;
import reman.client.app.finance.journals.JournalEntry;
import reman.client.app.finance.journals.JournalEntryLineItem;
import reman.client.gui.DateField;
import reman.client.gui.forms.DboFormPanel;
import reman.common.database.UserManager;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.DatabaseObjectException;
import reman.common.database.exceptions.LoggedInException;

public class JournalEntryPanel extends JSplitPane {

	private JournalEntryTable entry_table_;
	private JPanel options_;
	private DboFormPanel<JournalEntryLineItem> line_item_form_;
	private DateField occurred_;
	private JTextField description_;
	private JComboBox journals_combo_;

	private ArrayList<SaveListener<JournalEntry>> listeners_;

	/**
	 * Provides GUI functionality to create a new JournalEntry.
	 */
	public JournalEntryPanel() {
		this(null);
	}

	/**
	 * This panel will handle the GUI interactions for JournalEntry objects.  If <code>je</code> is not null it will be displayed, and if possible
	 * it will be editable.  If <code>je</code> is null then a new JournalEntry object can be created and registered with the financial engine.
	 * @param je
	 */
	public JournalEntryPanel(JournalEntry je) {
		super(JSplitPane.VERTICAL_SPLIT);
		this.setPreferredSize(new Dimension(500, -1));

		occurred_ = new DateField(null);
		description_ = new JTextField();
		this.listeners_ = new ArrayList<SaveListener<JournalEntry>>();

		JButton add_entry = new JButton("Add Entry");
		add_entry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateDescriptionPanel(null);
			}
		});
		JButton del_entry = new JButton("Remove Entry");
		del_entry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = entry_table_.getSelectedRow();
				JournalEntryTableModel m = (JournalEntryTableModel) entry_table_.getModel();
				m.removeLineItem(row);
			}
		});
		journals_combo_ = new JComboBox();
		try {
			Collection<Journal> journals = FinanceManager.instance().getJournalManager().getJournals()
					.values();
			for (Journal j : journals) {
				journals_combo_.addItem(j);
			}
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		JButton save_to_journal = new JButton("Save to Journal");
		save_to_journal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JournalEntryTableModel m = (JournalEntryTableModel) entry_table_.getModel();
				JournalEntry je = m.getJournalEntry();
				je.setDescription(description_.getText());
				je.setOccurredTime(occurred_.getDate());
				if (je.getLineItems().size() > 0) {
					Journal selected_journal = (Journal) journals_combo_.getSelectedItem();
					if (selected_journal != null) {
						try {
							Integer journal_key = FinanceManager.instance().getJournalManager().addJournalEntry(
									selected_journal, je);

							System.out.println("Entry id in journal: " + journal_key);

							if (journal_key != null) {
								makeGUIReadOnly();
								fireJournalEntrySave(je);
							}

						} catch (FinanceException e1) {
							// TODO Auto-generated catch block
							JOptionPane.showMessageDialog(JournalEntryPanel.this, e1.getMessage());
							e1.printStackTrace();
						} catch (DatabaseException e1) {
							// TODO Auto-generated catch block
							JOptionPane.showMessageDialog(JournalEntryPanel.this, e1.getMessage());
							e1.printStackTrace();
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							JOptionPane.showMessageDialog(JournalEntryPanel.this, e1.getMessage());
							e1.printStackTrace();
						}
					}
				}
			}
		});

		options_ = new JPanel();
		options_.setLayout(new FlowLayout(FlowLayout.LEFT));
		options_.add(add_entry);
		options_.add(del_entry);
		options_.add(new JLabel("Journal"));
		options_.add(journals_combo_);
		options_.add(save_to_journal);
		JournalEntryTableModel m = (je == null) ? new JournalEntryTableModel()
				: new JournalEntryTableModel(je);
		entry_table_ = new JournalEntryTable(m);
		if (this.isReadOnly()) {
			this.makeGUIReadOnly();
		}

		entry_table_.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}

				int row = entry_table_.getSelectedRow();
				JournalEntryTableModel m = (JournalEntryTableModel) entry_table_.getModel();
				updateDescriptionPanel(m.getLineItem(row));
			}
		});

		entry_table_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		entry_table_.setPreferredSize(new Dimension(-1, 400));
		entry_table_.setAutoCreateRowSorter(true);
		entry_table_.setRowHeight(25);

		JScrollPane entry_view = new JScrollPane(entry_table_, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JPanel entry_details = new JPanel();
		description_.setPreferredSize(new Dimension(300, 20));
		if (je != null) {
			occurred_.setDate(je.getOccurredTime());
			description_.setText(je.getDescription());
		}
		entry_details.setLayout(new FlowLayout(FlowLayout.LEFT));
		entry_details.add(new JLabel("Occurred"));
		entry_details.add(occurred_);
		entry_details.add(new JLabel("Description"));
		entry_details.add(description_);

		JPanel top_wrap = new JPanel(new BorderLayout());
		top_wrap.add(options_, BorderLayout.NORTH);
		top_wrap.add(entry_details, BorderLayout.SOUTH);

		JPanel wrap = new JPanel(new BorderLayout());
		wrap.add(top_wrap, BorderLayout.NORTH);
		wrap.add(entry_view, BorderLayout.CENTER);
		this.setTopComponent(wrap);
		this.setDefaultBottom();
	}

	private void setDefaultBottom() {
		this
				.setBottomComponent(new JLabel(
						"<html><body><div ALIGN=\"center\"><i>Select an entry to view details</i></div></body></html>"));
		this.setDividerLocation(.9);
	}

	public boolean isReadOnly() {
		JournalEntryTableModel m = (JournalEntryTableModel) entry_table_.getModel();
		try {
			return !m.getJournalEntry().allowedMoreCommits();
		} catch (LoggedInException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	private void makeGUIReadOnly() {
		for (Component c : options_.getComponents())
			c.setEnabled(false);
		occurred_.setEnabled(false);
		description_.setEditable(false);
		if (line_item_form_ != null)
			line_item_form_.setReadOnly(true);
		setBottomComponent(new JLabel("<html><i>Select an entry to view details</i></html>"));
		this.setDividerLocation();
	}

	private void setDividerLocation() {
		super.setDividerLocation(.5);
	}

	public void setSelectedJournal(Journal j) {
		journals_combo_.setSelectedItem(j);
	}

	public boolean addListener(SaveListener<JournalEntry> e) {
		return this.listeners_.add(e);
	}

	public boolean removeListener(SaveListener<JournalEntry> e) {
		return this.listeners_.remove(e);
	}

	public void updateDescriptionPanel(JournalEntryLineItem line_item) {

		try {
			/*if (this.line_item_form_ != null) {
				try {
					line_item_form_.cancel();
				} catch (NotPersistedException e) {
				}
			}*/
			JPanel controls = new JPanel();

			/*adding a new line item*/
			if (line_item == null) {
				JournalEntryLineItem default_entry = new JournalEntryLineItem(null, new AcctAmount(0,
						TransactionType.CREDIT), null, null);
				this.line_item_form_ = new JournalEntryLineItemFormPanel("New Line Item", default_entry,
						null, false);
				JButton add = new JButton("Add");
				add.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							JournalEntryTableModel m = (JournalEntryTableModel) entry_table_.getModel();
							line_item_form_.retrieveForm();
							m.addLineItem(line_item_form_.getDatabaseObject());
							/*line_item_form_.setDatabaseObject(new JournalEntryLineItem(null, new AcctAmount(0,
									TransactionType.CREDIT), null, null));*/
							/*TODO close the line_item_form_ panel*/
							setDefaultBottom();
							//line_item_form_.updateForm();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				});
				controls.add(add);
			} else {
				this.line_item_form_ = line_item.getFormPanel("Line Item Details", null, this.isReadOnly());
				JButton update = new JButton("Update");
				update.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							line_item_form_.retrieveForm();
							JournalEntryTableModel m = (JournalEntryTableModel) entry_table_.getModel();
							m.fireTableDataChanged();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				});
				controls.add(update);
			}

			if (!this.isReadOnly())
				line_item_form_.add(controls, BorderLayout.SOUTH);

			//line_item_form_.setPreferredSize(new Dimension(-1, this.getHeight() / 2));
			this.setDividerLocation();
			JScrollPane form_scroller = new JScrollPane(line_item_form_,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			this.setBottomComponent(form_scroller);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private class JournalEntryTable extends JTable {
		public JournalEntryTable(TableModel m) {
			super(m);
		}

		/*public TableCellRenderer getCellRenderer(int row, int column) {
			switch (column) {
			case 1:
				return new TableCellRenderer() {
					public Component getTableCellRendererComponent(JTable table, Object value,
							boolean isSelected, boolean hasFocus, int row, int column) {
						JProgressBar b = new JProgressBar();
						if (value != null) {
							b.setStringPainted(true);
							b.setValue((Integer) value);
						}
						return b;
					}
				};
			case 2:
				return new TableCellRenderer() {
					public Component getTableCellRendererComponent(JTable table, Object value,
							boolean isSelected, boolean hasFocus, int row, int column) {
						DateField f = new DateField();
						if (value != null)
							f.setDate((Timestamp) value);
						return f;
					}
				};
			}
			return super.getCellRenderer(row, column);
		}*/
	}

	private void fireJournalEntrySave(JournalEntry e) {
		for (SaveListener<JournalEntry> l : this.listeners_)
			l.newNode(e, this);
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		final JFrame f = new JFrame();
		f.setLayout(new BorderLayout());

		try {
			System.out.println(UserManager.instance().login("Scott", "test"));
			//FinanceManager.instance().getAccountManager().retrieve();
			FinanceManager.instance().getJournalManager().createJournal("General Journal");
			FinanceManager.instance().getAccountManager().importAccts("TestAccts.xml");
			FinanceManager.instance().turnOffInitializationPhase();
			JournalEntry je = new JournalEntry("Test", null);
			Account cash = FinanceManager.instance().getAccountManager().getAccount("Cash");
			Account a_r = FinanceManager.instance().getAccountManager().getAccount("Accounts Receivable");
			je.addLineItem(new JournalEntryLineItem(cash, new AcctAmount(22, TransactionType.DEBIT),
					"test", cash.getAllCategories().toArray(new AcctActionCategory[0])[0]));
			je.addLineItem(new JournalEntryLineItem(a_r, new AcctAmount(22, TransactionType.CREDIT),
					"test", null));
			JournalEntryPanel jep = new JournalEntryPanel(je);
			f.add(jep);
		} catch (Exception e) {
			e.printStackTrace();
			UserManager.instance().logout();
		}
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
}

package reman.client.app.finance.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.noos.xing.mydoggy.AggregationPosition;
import org.noos.xing.mydoggy.DockedTypeDescriptor;
import org.noos.xing.mydoggy.MultiSplitConstraint;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.ToolWindowType;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.content.MyDoggyTabbedContentManagerUI;

import reman.client.app.Framework;
import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AccountManager;
import reman.client.app.finance.exceptions.NameAlreadyExistsException;
import reman.client.app.finance.journals.Journal;
import reman.client.app.finance.journals.JournalEntry;
import reman.client.app.finance.journals.JournalManager;
import reman.client.app.listeners.DboChangedListener;
import reman.client.gui.DateField;
import reman.common.database.UserManager;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.LoggedInException;
import reman.common.messaging.DboChangedMessage;

public class JournalManagerPanel extends MyDoggyToolWindowManager implements DboChangedListener,
		SaveListener<JournalEntry> {

	private JComboBox journals_;
	private JournalTable journal_display_;
	private int entry_panel_count_;
	private Hashtable<Component, ToolWindow> entry_tool_map_;

	public JournalManagerPanel() {
		journal_display_ = new JournalTable(new JournalManagerTableModel());
		initPanel();
	}

	public JournalManagerPanel(Journal journal) {
		journal_display_ = new JournalTable(new JournalManagerTableModel(journal));
		initPanel();
	}

	private void initPanel() {
		Framework.instance().addDboChangedListener(this);

		MyDoggyTabbedContentManagerUI splitUI = new MyDoggyTabbedContentManagerUI();
		this.getContentManager().setContentManagerUI(splitUI);
		entry_panel_count_ = 0;
		entry_tool_map_ = new Hashtable<Component, ToolWindow>();

		journal_display_.setAutoCreateRowSorter(true);
		journal_display_.setRowHeight(30);
		journals_ = new JComboBox();
		journals_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Journal selected_journal = (Journal) journals_.getSelectedItem();
				journal_display_.setModel(new JournalManagerTableModel(selected_journal));
			}
		});

		JPanel new_journal_panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final JTextField new_journal_name = new JTextField(10);
		JButton new_journal = new JButton("Create Journal");
		new_journal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Journal new_j = new Journal(new_journal_name.getText());
				try {
					if ((new_j = FinanceManager.instance().getJournalManager().registerJournal(new_j)) != null) {
						new_journal_name.setText("");
					} else {
						JOptionPane.showMessageDialog(null, "Failed to create journal '"
								+ new_journal_name.getText() + "'.  Please try again later.");
					}
				} catch (NameAlreadyExistsException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (LoggedInException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (DatabaseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		new_journal_panel.add(new JLabel("Create Journal"));
		new_journal_panel.add(new_journal_name);
		new_journal_panel.add(new_journal);

		JButton add_entry = new JButton("Add Entry");
		add_entry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/*pop the journal entry panel with this journal selected*/
				Journal selected_journal = (Journal) journals_.getSelectedItem();
				if (selected_journal != null) {
					openToolWindow(new JournalEntry("", null), selected_journal);
				}
			}
		});

		JButton view_entry = new JButton("View Entry");
		view_entry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JournalManagerTableModel m = (JournalManagerTableModel) journal_display_.getModel();
				JournalEntry selected_entry = m.getEntry(journal_display_.getSelectedRow());
				Journal selected_journal = (Journal) journals_.getSelectedItem();
				if (selected_entry != null) {
					openToolWindow(selected_entry, selected_journal);
				}
			}
		});

		JPanel options = new JPanel(new FlowLayout(FlowLayout.CENTER));
		options.add(new JLabel("Journal"));
		options.add(journals_);
		options.add(add_entry);
		options.add(view_entry);

		JScrollPane table_wrap = new JScrollPane(this.journal_display_,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JPanel content_panel = new JPanel(new BorderLayout());
		content_panel.add(options, BorderLayout.NORTH);
		content_panel.add(table_wrap, BorderLayout.CENTER);
		content_panel.add(new_journal_panel, BorderLayout.SOUTH);

		this.getContentManager().addContent("Journal Manager", "Journal Manager", null, content_panel,
				"", new MultiSplitConstraint(AggregationPosition.DEFAULT));

		this.initJournals();
	}

	private void initJournals() {
		try {
			Collection<Journal> all_journals = FinanceManager.instance().getJournalManager()
					.getJournals().values();
			for (Journal j : all_journals)
				this.journals_.addItem(j);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (this.journals_.getItemCount() > 0)
			this.journals_.setSelectedIndex(0);
	}

	private void openToolWindow(JournalEntry entry, Journal selected_journal) {
		JournalEntryPanel panel = new JournalEntryPanel(entry);
		panel.setSelectedJournal(selected_journal);

		/*construct tool*/
		boolean new_entry = true;
		try {
			new_entry = entry.allowedMoreCommits();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String title = new_entry ? "New Journal Entry" : "Journal Entry";
		String tool_id = new_entry ? "New Journal Entry " + this.entry_panel_count_++ : entry
				.getOccurredTime().toString();//DateFormat.getInstance().format(entry.getOccurredTime());
		if (selected_journal != null)
			tool_id = selected_journal.toString() + " " + tool_id;
		ToolWindow t = this.getToolWindow(tool_id);

		if (t == null) {
			panel.addListener(this);
			JScrollPane panel_scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			t = this.registerToolWindow(tool_id, title, null, panel_scroll, ToolWindowAnchor.RIGHT);
			t.setType(ToolWindowType.SLIDING);
			t.getTypeDescriptor(DockedTypeDescriptor.class).setDockLength(550);
			t.setAvailable(true);
			entry_tool_map_.put(panel, t);
		}
		/*make visible*/
		Object id = this.getActiveToolWindowId();
		if (id != null) {
			this.getToolWindow(id).setActive(false);
		}
		t.setActive(true);
	}

	private void closeToolWindow(Component panel) {
		ToolWindow t = this.entry_tool_map_.get(panel);
		if (t != null) {
			t.setAvailable(false);
			//this.unregisterToolWindow(t.getId());
			this.entry_tool_map_.remove(panel);
			if (panel instanceof JournalEntryPanel) {
				JournalEntryPanel p = (JournalEntryPanel) panel;
				p.removeListener(this);
			}
		}
	}

	private class JournalTable extends JTable {
		public JournalTable(TableModel m) {
			super(m);
		}

		public TableCellRenderer getCellRenderer(int row, int column) {
			switch (column) {
			case 1:
				return new TableCellRenderer() {
					public Component getTableCellRendererComponent(JTable table, Object value,
							boolean isSelected, boolean hasFocus, int row, int column) {
						DateField df = new DateField(null);
						if (value != null) {
							Timestamp ts = (Timestamp) value;
							df.setDate(ts);
						}
						df.setEditable(false);
						return df;
					}
				};
			}
			return super.getCellRenderer(row, column);
		}
	}

	@Override
	public void dboChangedEvent(DboChangedMessage m) {
		if (m.getDatabaseObject() instanceof Journal) {
			Journal selection = (Journal) this.journals_.getSelectedItem();
			Journal j = (Journal) m.getDatabaseObject();
			if (selection != null && j.getName().equals(selection.getName())) {
				this.journal_display_.repaint();
			} else {
				boolean found_journal = false;
				for (int i = 0; i < this.journals_.getItemCount(); i++) {
					Journal curr_j = (Journal) this.journals_.getItemAt(i);
					if (j.getName().equals(curr_j.getName())) {
						found_journal = true;
						break;
					}
				}
				if (!found_journal) {
					this.journals_.addItem(j);
				}
			}
		}
	}

	@Override
	public void newNode(JournalEntry new_node, Component source) {
		this.closeToolWindow(source);
	}

	public static void main(String[] args) throws Exception {
		final JFrame f = new JFrame();

		System.out.println(UserManager.instance().login("Scott", "test"));
		Journal gen_journal = FinanceManager.instance().getJournalManager().createJournal(
				"General Journal");
		FinanceManager.instance().getJournalManager().createJournal("Test Journal");
		/*	
			FinanceManager.instance().getAccountManager().importAccts("TestAccts.xml");
			FinanceManager.instance().turnOffInitializationPhase();
			JournalEntry je = new JournalEntry("Test", null);
			Account cash = FinanceManager.instance().getAccountManager().getAccount("Cash");
			Account a_r = FinanceManager.instance().getAccountManager().getAccount("Accounts Receivable");
			je.addLineItem(new JournalEntryLineItem(cash, new AcctAmount(22, TransactionType.DEBIT),
					"test", cash.getAllCategories().toArray(new AcctActionCategory[0])[0]));
			je.addLineItem(new JournalEntryLineItem(a_r, new AcctAmount(22, TransactionType.CREDIT),
					"test", null));

			gen_journal.addJournalEntry(je);*/

		AccountManager am = FinanceManager.instance().getAccountManager();
		Account cash = am.getAccount("Cash");
		JournalManager jm = FinanceManager.instance().getJournalManager();
		gen_journal = jm.getJournal("General Journal");

		f.add(new JournalManagerPanel(gen_journal));

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
	}
}

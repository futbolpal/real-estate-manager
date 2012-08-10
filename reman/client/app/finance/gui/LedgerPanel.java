package reman.client.app.finance.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeSelectionModel;

import org.noos.xing.mydoggy.AggregationPosition;
import org.noos.xing.mydoggy.DockedTypeDescriptor;
import org.noos.xing.mydoggy.MultiSplitConstraint;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.ToolWindowType;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.content.MyDoggyMultiSplitContentManagerUI;
import org.xml.sax.SAXException;

import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctActionCategory;
import reman.client.app.finance.accounts.AcctAmount;
import reman.client.app.finance.journals.Journal;
import reman.client.app.finance.journals.JournalEntry;
import reman.client.app.finance.journals.JournalEntryLineItem;
import reman.client.app.finance.ledger.Ledger;
import reman.client.gui.DateField;
import reman.common.database.UserManager;
import reman.common.database.exceptions.DatabaseException;

public class LedgerPanel extends MyDoggyToolWindowManager {

	private JTree account_tree_;
	private Hashtable<Ledger, ToolWindow> ledger_tool_map_;

	public LedgerPanel() {
		MyDoggyMultiSplitContentManagerUI splitUI = new MyDoggyMultiSplitContentManagerUI();
		this.getContentManager().setContentManagerUI(splitUI);

		ledger_tool_map_ = new Hashtable<Ledger, ToolWindow>();

		account_tree_ = new JTree(new AccountTreeModel("All Accounts"));
		account_tree_.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		JScrollPane tree_scroll = new JScrollPane(account_tree_,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JPanel options = new JPanel();

		JButton view = new JButton("View Ledger");
		JButton close = new JButton("Close Ledger");
		options.add(view);
		options.add(close);
		
		view.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object selection = account_tree_.getSelectionPath().getLastPathComponent();
				if (selection instanceof Account) {
					Account acct = (Account) selection;
					try {
						Ledger ledger = acct.getLedger();
						if (ledger != null) {
							openToolWindow(ledger);
						}
					} catch (DatabaseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});

		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object selection = account_tree_.getSelectionPath().getLastPathComponent();
				if (selection instanceof Account) {
					Account acct = (Account) selection;
					try {
						Ledger ledger = acct.getLedger();
						if (ledger != null) {
							closeToolWindow(ledger);
						}
					} catch (DatabaseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.add(tree_scroll, BorderLayout.CENTER);
		wrapper.add(options, BorderLayout.SOUTH);

		this.getContentManager().addContent("General Ledger", "General Ledger", null, wrapper,
				"Select an account to view corresponding ledger.",
				new MultiSplitConstraint(AggregationPosition.DEFAULT));
	}

	private void openToolWindow(Ledger ledger) {
		String title = ledger.getName();
		String tool_id = title;
		ToolWindow t = this.getToolWindow(tool_id);
		if (t == null) {
			LedgerTable table_ = new LedgerTable(new LedgerTableModel(ledger));
			table_.setAutoCreateRowSorter(true);
			table_.setRowHeight(30);
			table_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane table_scroll = new JScrollPane(table_, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			t = this.registerToolWindow(tool_id, title, null, table_scroll, ToolWindowAnchor.RIGHT);
			t.setType(ToolWindowType.SLIDING);
			t.getTypeDescriptor(DockedTypeDescriptor.class).setDockLength(400);
			t.setAvailable(true);
			ledger_tool_map_.put(ledger, t);
		}

		/*make visible*/
		Object id = this.getActiveToolWindowId();
		if (id != null) {
			this.getToolWindow(id).setActive(false);
		}
		t.setActive(true);
	}

	private void closeToolWindow(Ledger ledger) {
		ToolWindow t = this.ledger_tool_map_.get(ledger);
		if (t != null) {
			t.setAvailable(false);
			this.ledger_tool_map_.remove(ledger);
		}
	}

	private class LedgerTable extends JTable {
		public LedgerTable(TableModel m) {
			super(m);
		}

		public TableCellRenderer getCellRenderer(int row, int column) {
			switch (column) {
			case 0:
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

	public static void main(String[] args) throws DatabaseException, SQLException, SAXException,
			IOException {
		UserManager.instance().login("Scott", "test");
		FinanceManager.instance().getAccountManager().importAccts("TestAccts.xml");

		JFrame f = new JFrame();

		Account ap = FinanceManager.instance().getAccountManager().getAccount("Accounts Payable");
		Account cash = FinanceManager.instance().getAccountManager().getAccount("Cash");
		Journal gen_journal = FinanceManager.instance().getJournalManager().createJournal(
				"General Journal");
		Ledger l = ap.getLedger();
		JournalEntry je = new JournalEntry("testing entry", null);
		AcctActionCategory cat_ap = ap.getRootActionCategories().values().toArray(
				new AcctActionCategory[0])[0];
		AcctActionCategory cat_cash = cash.getRootActionCategories().values().toArray(
				new AcctActionCategory[0])[0];
		je.addLineItem(new JournalEntryLineItem(ap, new AcctAmount(5, TransactionType.CREDIT), "test1",
				cat_ap));
		je.addLineItem(new JournalEntryLineItem(ap, new AcctAmount(5, TransactionType.CREDIT), "test2",
				cat_ap));
		je.addLineItem(new JournalEntryLineItem(ap, new AcctAmount(5, TransactionType.CREDIT), "test3",
				cat_ap));
		je.addLineItem(new JournalEntryLineItem(cash, new AcctAmount(15, TransactionType.DEBIT),
				"test4", cat_cash));
		gen_journal.addJournalEntry(je);

		f.add(new LedgerPanel());

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setVisible(true);
	}

}

package reman.client.app.finance.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

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
import reman.client.app.finance.accounts.AcctType;
import reman.client.gui.forms.DboFormPanel;
import reman.common.database.UserManager;
import reman.common.database.exceptions.DatabaseException;

public class AccountManagerPanel extends MyDoggyToolWindowManager implements
		FormSaveListener<Account> {

	private JTree account_tree_;
	private int account_panel_count_;
	private Hashtable<DboFormPanel<Account>, ToolWindow> acct_tool_map_;
	private Hashtable<Account, DboFormPanel<Account>> acct_panel_map_;

	public AccountManagerPanel(Account acct) {
		this(new AccountTreeModel(acct));
	}

	public AccountManagerPanel(String root_msg) {
		this(new AccountTreeModel(root_msg));
	}

	private AccountManagerPanel(AccountTreeModel m) {
		MyDoggyMultiSplitContentManagerUI splitUI = new MyDoggyMultiSplitContentManagerUI();
		this.getContentManager().setContentManagerUI(splitUI);

		acct_tool_map_ = new Hashtable<DboFormPanel<Account>, ToolWindow>();
		acct_panel_map_ = new Hashtable<Account, DboFormPanel<Account>>();
		account_panel_count_ = 0;
		account_tree_ = new JTree(m);
		JScrollPane tree_scroll = new JScrollPane(account_tree_,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		//account_tree_.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		//account_tree_.addTreeSelectionListener(this);

		JPanel manager_options = new JPanel(new FlowLayout(FlowLayout.CENTER));

		JButton view_acct = new JButton("View Account");
		JButton close_view = new JButton("Close View");
		JButton add_child = new JButton("Add Child Account");
		JButton remove_acct = new JButton("Remove Account(s)");

		view_acct.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Account selection = (Account) account_tree_.getLastSelectedPathComponent();
				if (selection != null) {
					updateAccountForm(selection);
				}
			}
		});

		close_view.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Account selection = (Account) account_tree_.getLastSelectedPathComponent();
				if (selection != null) {
					closeToolWindow(selection);
				}
			}
		});

		add_child.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Account selected_acct = AccountTreeModel.getSelectedLastAccount(account_tree_);
				Account new_acct = new Account(null, "", selected_acct,
						(selected_acct == null) ? AcctType.ASSET : selected_acct.getAcctType(),
						(selected_acct == null) ? TransactionType.CREDIT : selected_acct.getBalanceSystem()
								.getNormalBalance(), (selected_acct == null) ? null : selected_acct.getTimeScale(),
						(selected_acct == null) ? null : selected_acct.getCashCategory());
				updateAccountForm(new_acct);
			}
		});

		remove_acct.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Account selected_acct = AccountTreeModel.getSelectedLastAccount(account_tree_);
				if (selected_acct != null) {

					/*remove a collection of accounts*/
					Collection<Account> accts = getSelectedAccounts();
					for (Account acct : accts) {
						try {
							if (FinanceManager.instance().getAccountManager().removeAccount(acct)) {
								AccountTreeModel m = (AccountTreeModel) account_tree_.getModel();
								m.nodeRemoved(acct, null);
							}
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (DatabaseException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		});

		manager_options.add(view_acct);
		manager_options.add(close_view);
		manager_options.add(add_child);
		manager_options.add(remove_acct);

		JPanel left_wrap = new JPanel(new BorderLayout());
		left_wrap.add(tree_scroll, BorderLayout.CENTER);
		left_wrap.add(manager_options, BorderLayout.SOUTH);

		this.getContentManager().addContent("Account Manager", "Account Manager", null, left_wrap, "",
				new MultiSplitConstraint(AggregationPosition.DEFAULT));
	}

	private Collection<Account> getSelectedAccounts() {
		Collection<Account> selected_accts = new ArrayList<Account>();
		TreePath[] paths = this.account_tree_.getSelectionPaths();
		for (TreePath path : paths) {
			Account selected_acct = (Account) path.getLastPathComponent();
			if (selected_acct != null) {
				selected_accts.add(selected_acct);
			}
		}
		return selected_accts;
	}

	private void updateAccountForm(Account acct) {

		try {
			/*construct tool*/
			boolean new_acct = (acct.getName() == null || acct.getName().equals("")) ? true : false;
			String title = new_acct ? "New Account" : acct.getName();
			String tool_id = new_acct ? "New Account " + this.account_panel_count_++ : acct.getName();
			ToolWindow t = this.getToolWindow(tool_id);
			if (t == null) {
				AccountFormPanel panel = new AccountFormPanel(title, acct, null, false);
				panel.addListener(this);
				JScrollPane panel_scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				/*t = acct_view_manager_.getContentManager().addContent(tool_id, title, null, panel_scroll,
						"Account Details", new MultiSplitConstraint(AggregationPosition.RIGHT));*/
				t = this.registerToolWindow(tool_id, title, null, panel_scroll, ToolWindowAnchor.RIGHT);
				t.setSelected(true);
				t.setType(ToolWindowType.SLIDING);
				t.getTypeDescriptor(DockedTypeDescriptor.class).setDockLength(500);
				t.setAvailable(true);
				acct_tool_map_.put(panel, t);
				acct_panel_map_.put(panel.getDatabaseObject(), panel);
			}

			/*make visible*/
			Object id = this.getActiveToolWindowId();
			if (id != null) {
				this.getToolWindow(id).setActive(false);
			}
			t.setActive(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void closeToolWindow(Account acct) {
		DboFormPanel<Account> panel = this.acct_panel_map_.get(acct);
		if (panel != null)
			this.closeToolWindow(panel);
	}

	private void closeToolWindow(DboFormPanel<Account> panel) {
		ToolWindow t = this.acct_tool_map_.get(panel);
		if (t != null) {
			t.setAvailable(false);
			this.acct_tool_map_.remove(panel);
			if (panel instanceof AccountFormPanel) {
				AccountFormPanel p = (AccountFormPanel) panel;
				p.removeListener(this);
			}
		}
	}

	@Override
	public void newNode(Account new_acct, DboFormPanel<Account> source) {
		AccountTreeModel m = (AccountTreeModel) this.account_tree_.getModel();
		TreePath path = m.getPathToRoot(new_acct);
		m.nodeInserted(new_acct, path);
		this.account_tree_.setSelectionPath(path);
		this.closeToolWindow(source);
		//this.account_tree_.scrollPathToVisible(path);
	}

	@Override
	public void replaceNode(Account old_acct, Account new_acct, DboFormPanel<Account> source) {
		AccountTreeModel m = (AccountTreeModel) this.account_tree_.getModel();
		m.valueForPathChanged(this.account_tree_.getSelectionPath(), new_acct);
	}

	public static void main(String[] args) throws SQLException, DatabaseException, SAXException,
			IOException {
		if (UserManager.instance().login("Scott", "test")) {
			FinanceManager.instance().getAccountManager().importAccts("TestAccts.xml");

			JFrame f = new JFrame();

			Account ex = FinanceManager.instance().getAccountManager().getAccount("Expenses");
			System.out.println(ex);

			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.pack();
			f.setVisible(true);
		}
	}
}

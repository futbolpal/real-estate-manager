package reman.client.app.finance.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.TreePath;

import reman.client.app.finance.DatabaseObjectBatch;
import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctActionCategory;
import reman.client.app.finance.accounts.AcctBalanceSystem;
import reman.client.app.finance.accounts.AcctTimeScale;
import reman.client.app.finance.accounts.AcctType;
import reman.client.app.finance.accounts.CashCategory;
import reman.client.app.finance.accounts.TemporaryAccount;
import reman.client.app.finance.journals.Journal;
import reman.client.gui.forms.DboFormPanel;
import reman.common.database.DatabaseObject;
import reman.common.database.UserManager;
import reman.common.database.exceptions.DatabaseException;

public class AccountFormPanel extends DboFormPanel<Account> implements
		FormSaveListener<AcctActionCategory> {

	private JTextField acct_id_;
	private JTextField name_;
	private JTextField description_;
	private JTree parent_;
	private JComboBox acct_type_;
	private JComboBox time_scale_;
	private JTree close_to_acct_;
	private JComboBox close_to_journal_;
	private JComboBox cash_categories_;
	private JTree action_categories_;

	private ArrayList<FormSaveListener<Account>> save_listeners_;

	private DboFormPanel<AcctBalanceSystem> balance_system_panel_;

	/**
	 * To create a new account, pass null for <code>acct</code>.  Account information can not be edited, it is
	 * verified and registered with the AccountManager.
	 * @param name
	 * @param acct
	 * @param parent
	 * @param read_only
	 * @throws Exception
	 */
	public AccountFormPanel(String name, Account acct, DboFormPanel<? extends DatabaseObject> parent,
			boolean read_only) throws Exception {
		super(name, acct, parent, (FinanceManager.instance().isInitializationPhase()) ? read_only
				: true);
		this.setAutoCommit(false);
		this.save_listeners_ = new ArrayList<FormSaveListener<Account>>();
		this.close_to_acct_ = new JTree(new AccountTreeModel("All Accounts"));
		JScrollPane close_to_scroll = new JScrollPane(close_to_acct_,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.parent_ = new JTree(new AccountTreeModel("All Accounts"));
		JScrollPane parent_scroll = new JScrollPane(parent_, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.addFormItem("Account Id", this.acct_id_ = new JTextField(20));
		this.addFormItem("Account Name", this.name_ = new JTextField(20));
		this.addFormItem("Parent Account", parent_scroll);
		this.addFormItem("Account Description", this.description_ = new JTextField(20));
		this.addFormItem("Account Type", this.acct_type_ = new JComboBox(AcctType.values()));
		this
				.addFormItem("Account Time Scale", this.time_scale_ = new JComboBox(AcctTimeScale.values()));
		this.addFormItem("Cash Category", this.cash_categories_ = new JComboBox(CashCategory.values()));
		this.addFormItem("Close To Account", close_to_scroll);
		this.addFormItem("Close To Journal", this.close_to_journal_ = new JComboBox());
		this.action_categories_ = new JTree(new AcctActionCategoryTreeModel(acct));
		//this.action_categories_.setPreferredSize(new Dimension(-1, 300));
		JScrollPane tree_view = new JScrollPane(this.action_categories_,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.addFormItem("Action Categories", tree_view);

		this.acct_type_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AcctType type = (AcctType) acct_type_.getSelectedItem();
				if (AcctType.isTemporaryType(type)) {
					time_scale_.setSelectedIndex(-1);
					time_scale_.setEnabled(false);
					close_to_acct_.setEnabled(true);
					close_to_journal_.setEnabled(true);
				} else {
					if (acct_type_.isFocusable()) {
						time_scale_.setEnabled(true);
						close_to_journal_.setEnabled(false);
						close_to_acct_.setEnabled(false);
						close_to_acct_.setSelectionPath(null);
					}
				}
			}
		});

		if (!read_only) {
			this.action_categories_.addMouseListener(new CategoryMouseListener());
		}
		this.addFormItem(this.balance_system_panel_ = AcctBalanceSystem.getFormPanel(
				"Account Balance System", acct.getBalanceSystem(), this, this.isReadOnly()));
		this.balance_system_panel_.setAutoCommit(false);

		this.updateForm();

		JButton save;
		//JButton update;
		JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		/*controls_.add(update = new JButton("Update View"));
		update.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					dbo_.retrieve();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (DatabaseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				updateForm();
			}
		});*/
		if (!this.isReadOnly()) {
			controls.add(save = new JButton("Save Changes"));

			save.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveForm();
				}
			});
		}
		this.add(controls, BorderLayout.SOUTH);
	}

	public boolean addListener(FormSaveListener<Account> l) {
		return this.save_listeners_.add(l);
	}

	public boolean removeListener(FormSaveListener<Account> l) {
		return this.save_listeners_.remove(l);
	}

	private void fireReplaceAccount(Account old, Account new_acct) {
		for (FormSaveListener<Account> l : this.save_listeners_)
			l.replaceNode(old, new_acct, this);
	}

	private void fireNewAccount(Account new_acct) {
		for (FormSaveListener<Account> l : this.save_listeners_)
			l.newNode(new_acct, this);
	}

	public void saveForm() {
		if (this.isReadOnly())
			return;

		this.balance_system_panel_.retrieveForm();

		//NON SETABLE ATTRIBUTES
		Integer id = null;
		try {
			id = Integer.valueOf(this.acct_id_.getText());
		} catch (NumberFormatException e) {
		}
		if (id != null && id < 0)
			id = null;
		String acct_name = this.name_.getText();
		AcctType a_type = (AcctType) this.acct_type_.getSelectedItem();
		AcctTimeScale t_scale = (AcctTimeScale) this.time_scale_.getSelectedItem();
		CashCategory cash_cat = (CashCategory) this.cash_categories_.getSelectedItem();
		Account parent = AccountTreeModel.getSelectedLastAccount(this.parent_);
		Account close_to_acct = AccountTreeModel.getSelectedLastAccount(this.close_to_acct_);
		Journal close_to_journal = (Journal) this.close_to_journal_.getSelectedItem();
		//NON SETABLE ATTRIBUTES

		String description = description_.getText();
		double balance = this.balance_system_panel_.getDatabaseObject().getBalance();
		TransactionType normal_balance = this.balance_system_panel_.getDatabaseObject()
				.getNormalBalance();

		Account new_acct = null;
		if (AcctType.isTemporaryType(a_type)) {
			new_acct = new TemporaryAccount(id, acct_name, parent, balance, close_to_acct, a_type,
					normal_balance, close_to_journal, null, null, cash_cat);
		} else {
			new_acct = new Account(id, acct_name, parent, a_type, balance, normal_balance, t_scale,
					cash_cat);
		}
		new_acct.setDescription(description);
		try {
			boolean registered = FinanceManager.instance().getAccountManager().isAccountRegistered(dbo_);
			if ((registered && FinanceManager.instance().getAccountManager().replaceAccount(dbo_,
					new_acct, true))
					|| (!registered && FinanceManager.instance().getAccountManager()
							.registerAccount(new_acct))) {
				//this.balance_system_panel_.setDatabaseObject(new_acct.getBalanceSystem());
				try {
					this.balance_system_panel_ = new BalanceSystemFormPanel("Account Balance", new_acct
							.getBalanceSystem(), this, this.isReadOnly());
					this.balance_system_panel_.setAutoCommit(false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (registered)
					this.fireReplaceAccount(dbo_, new_acct);
				else
					this.fireNewAccount(new_acct);

				dbo_ = new_acct;
				JOptionPane.showMessageDialog(null, "New Account: '" + dbo_ + "'.");
			} else {
				//TODO: error registering new account and replacing current acct
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.updateForm();
	}

	@Override
	public void retrieveForm() {

	}

	private void updateAvailableJournals() {
		this.close_to_journal_.removeAllItems();
		try {
			Collection<Journal> all_journals = FinanceManager.instance().getJournalManager()
					.getJournals().values();
			for (Journal j : all_journals)
				this.close_to_journal_.addItem(j);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateForm() {
		this.updateAvailableJournals();
		if (dbo_.getAcctId() != null)
			this.acct_id_.setText(String.valueOf(dbo_.getAcctId()));
		if (dbo_.getName() != null)
			this.name_.setText(dbo_.getName());
		if (dbo_.getAcctType() != null)
			this.acct_type_.setSelectedItem(dbo_.getAcctType());
		if (dbo_.getDescription() != null)
			this.description_.setText(dbo_.getDescription());
		AccountTreeModel m = (AccountTreeModel) this.parent_.getModel();
		this.parent_.setSelectionPath(m.getPathToRoot(dbo_.getParent()));

		/*combo boxes allowed to be null values*/
		this.time_scale_.setSelectedItem(dbo_.getTimeScale());
		this.cash_categories_.setSelectedItem(dbo_.getCashCategory());
		if (dbo_ instanceof TemporaryAccount) {
			TemporaryAccount temp_acct = (TemporaryAccount) dbo_;
			m = (AccountTreeModel) this.close_to_acct_.getModel();
			this.close_to_acct_.setSelectionPath(m.getPathToRoot(temp_acct.getCloseToAccount()));
			this.close_to_journal_.setSelectedItem(temp_acct.getCloseToJournal());
		}

		//TODO fire category tree changed
		this.balance_system_panel_.updateForm();
	}

	private class CategoryMouseListener extends MouseAdapter {
		private JPopupMenu m;
		private JMenuItem add_new_child;
		private JMenuItem add_reference_child;
		private JMenuItem edit_category;
		private JMenuItem remove_category;
		private boolean in_menu_;

		public CategoryMouseListener() {
			m = new JPopupMenu();
			m.addMouseListener(new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					in_menu_ = true;
				}

				public void mouseExited(MouseEvent e) {
					in_menu_ = false;
				}
			});
			in_menu_ = true;
			add_new_child = new JMenuItem("Add Child Category");
			add_reference_child = new JMenuItem("Add Reference Category");
			edit_category = new JMenuItem("Edit Category");
			remove_category = new JMenuItem("Remove Category");

			add_new_child.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					AcctActionCategory selected_cat = getSelectedCategory(action_categories_
							.getSelectionPath());

					AcctActionCategory cat = new AcctActionCategory(null, selected_cat,
							(selected_cat == null) ? TransactionType.DEBIT : selected_cat.getBalanceSystem()
									.getNormalBalance());

					CategoryMouseListener.this.lauchNewCategoryWindow("New Category", cat);
				}
			});

			edit_category.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					AcctActionCategory selected_cat = getSelectedCategory(action_categories_
							.getSelectionPath());
					if (selected_cat == null)
						return;

					CategoryMouseListener.this.lauchNewCategoryWindow(selected_cat.getName(), selected_cat);
				}
			});

			remove_category.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					AcctActionCategory selected_cat = getSelectedCategory(action_categories_
							.getSelectionPath());
					if (selected_cat == null)
						return;

					if (selected_cat.getOwnerAccts().size() > 1) {
						if (JOptionPane
								.showConfirmDialog(
										AccountFormPanel.this,
										"This category is shared between multiple accounts. Do you want these changes to effect all account?",
										"Shared Category", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
							return;
						}
					}

					DatabaseObjectBatch<Account> batch = new DatabaseObjectBatch<Account>();
					for (String acct_name : selected_cat.getOwnerAccts()) {
						Account acct = null;
						try {
							acct = FinanceManager.instance().getAccountManager().getAccount(acct_name);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						if (acct != null) {
							batch.addToBatch(acct);
						}
					}

					AcctActionCategoryTreeModel m = (AcctActionCategoryTreeModel) AccountFormPanel.this.action_categories_
							.getModel();
					ArrayList<Account> failed_remove = new ArrayList<Account>();
					ArrayList<Account> success_accts = new ArrayList<Account>();
					Collection<DatabaseObject> failed_lock = batch.lockBatch();
					if (failed_lock.size() == 0) {
						for (Account acct : batch.getBatch()) {
							try {
								if (acct.removeActionCategory(selected_cat)) {
									m.nodeRemoved(selected_cat, null);
									success_accts.add(acct);
								} else
									failed_remove.add(acct);
							} catch (DatabaseException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (SQLException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						if (failed_remove.size() > 0) {
							String failed_acct_names = DatabaseObjectBatch.getDatabaseObjectNames(failed_remove);
							JOptionPane.showMessageDialog(AccountFormPanel.this, "Failed to remove Category '"
									+ selected_cat + "' from the following Accounts:" + failed_acct_names + ".",
									"Removal Error", JOptionPane.ERROR_MESSAGE);
						}
						if (success_accts.size() > 0) {
							String acct_names = DatabaseObjectBatch.getDatabaseObjectNames(success_accts);
							JOptionPane.showMessageDialog(AccountFormPanel.this, "Removed Category '"
									+ selected_cat + "' from the following Accounts:" + acct_names + ".",
									"Removal Status", JOptionPane.INFORMATION_MESSAGE);
						}
					} else {
						String failed_acct_names = DatabaseObjectBatch.getDatabaseObjectNames(failed_lock);
						JOptionPane.showMessageDialog(AccountFormPanel.this,
								"Could not obtain a lock on the following Accounts:" + failed_acct_names + ".",
								"Lock Error", JOptionPane.ERROR_MESSAGE);
					}
					batch.unlockBatch();
				}
			});

			/*TODO: give warning if selected category is shared with another account and not root of current account.
			 * this will alter the category tree*/

			/*TODO: add action listener to deal with selected node which could be 1)null 2)root 3)ActionCategory*/
			m.add(edit_category);
			m.add(remove_category);
			m.add(add_new_child);
			m.add(add_reference_child);
		}

		protected void lauchNewCategoryWindow(String name, AcctActionCategory category) {
			JFrame test = new JFrame();
			//test.addWindowStateListener(AccountFormPanel.this);
			test.setLayout(new BorderLayout());
			ActionCategoryFormPanel cat_panel;
			try {
				cat_panel = new ActionCategoryFormPanel(name, dbo_, category, null, false);
				/*listen to save events made by the panel*/
				cat_panel.addListener(AccountFormPanel.this);
				test.add(cat_panel, BorderLayout.CENTER);

				test.setVisible(true);
			} catch (DatabaseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		public AcctActionCategory getSelectedCategory(TreePath tp) {
			AcctActionCategory return_val = null;
			if (tp != null) {
				Object selected_object = tp.getLastPathComponent();
				if (selected_object != null) {
					if (selected_object instanceof AcctActionCategory)
						return_val = (AcctActionCategory) selected_object;
				}
			}
			return return_val;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			/*right click*/
			if (SwingUtilities.isRightMouseButton(e)) {
				m.setLocation(e.getLocationOnScreen());
				m.setVisible(true);
			} else {
				this.killMenu();
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (!in_menu_) {
				this.killMenu();
			}
		}

		private void killMenu() {
			m.setVisible(false);
			in_menu_ = true;
		}
	}

	@Override
	public void newNode(AcctActionCategory new_category, DboFormPanel<AcctActionCategory> source) {
		AcctActionCategoryTreeModel m = (AcctActionCategoryTreeModel) this.action_categories_
				.getModel();
		TreePath path = m.getPathToRoot(new_category);
		m.nodeInserted(new_category, path);
		this.action_categories_.setSelectionPath(path);
		//this.action_categories_.scrollPathToVisible(path);
	}

	@Override
	public void replaceNode(AcctActionCategory old_acct, AcctActionCategory new_category,
			DboFormPanel<AcctActionCategory> source) {
		AcctActionCategoryTreeModel m = (AcctActionCategoryTreeModel) this.action_categories_
				.getModel();
		m.valueForPathChanged(this.action_categories_.getSelectionPath(), new_category);
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		final JFrame f = new JFrame();
		try {
			System.out.println(UserManager.instance().login("Scott", "test"));
			Account test_p = new Account(10, "Test Parent", null, AcctType.ASSET, TransactionType.CREDIT,
					AcctTimeScale.LONG_TERM, null);
			Account test_c = new Account(11, "Test Child", test_p, AcctType.ASSET, 1234.1,
					TransactionType.CREDIT, AcctTimeScale.LONG_TERM, null);
			Account test_p_1 = new Account(10, "Test P Replacement", null, AcctType.ASSET,
					TransactionType.CREDIT, AcctTimeScale.LONG_TERM, null);
			Account test_c_1 = new Account(11, "Test Replacement", test_p, AcctType.ASSET, 112.1,
					TransactionType.CREDIT, AcctTimeScale.LONG_TERM, null);
			FinanceManager.instance().getAccountManager().registerAccount(test_p);
			FinanceManager.instance().getAccountManager().registerAccount(test_c);
			AcctActionCategory cat_p = new AcctActionCategory("root cat", null, TransactionType.CREDIT);
			AcctActionCategory cat_c = new AcctActionCategory("child 1 cat", cat_p,
					TransactionType.CREDIT);
			test_c.addActionCategory(cat_p);
			test_c.addActionCategory(cat_c);
			FinanceManager.instance().getAccountManager().replaceAccount(test_c, test_c_1, true);
			FinanceManager.instance().getAccountManager().replaceAccount(test_p, test_p_1, true);
			Account result_c = FinanceManager.instance().getAccountManager().getAccount("Test Child");
			Account result_c_1 = FinanceManager.instance().getAccountManager().getAccount(
					"Test Replacement");
			Account result_p_1 = FinanceManager.instance().getAccountManager().getAccount(
					"Test P Replacement");
			final AccountFormPanel afp = new AccountFormPanel(result_c_1.getName(), result_c_1, null,
					false);
			f.setLayout(new BorderLayout());
			f.add(afp, BorderLayout.CENTER);

			f.setVisible(true);
			f.pack();
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		} catch (Exception e) {
			UserManager.instance().logout();
			throw e;
		} finally {

		}
	}

}

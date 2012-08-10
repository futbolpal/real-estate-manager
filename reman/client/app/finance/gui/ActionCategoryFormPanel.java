package reman.client.app.finance.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctActionCategory;
import reman.client.app.finance.accounts.AcctBalanceSystem;
import reman.client.app.finance.accounts.AcctTimeScale;
import reman.client.app.finance.accounts.AcctType;
import reman.client.gui.forms.DboFormPanel;
import reman.common.database.UserManager;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.LoggedInException;

public class ActionCategoryFormPanel extends DboFormPanel<AcctActionCategory> {

	private Account subject_acct_;

	private JTextField name_;
	private JComboBox parent_;
	private DefaultListModel owner_accounts_model_;
	private JList owner_accounts_;
	private DboFormPanel<AcctBalanceSystem> balance_system_;
	private JPanel controls_;
	private ArrayList<FormSaveListener<AcctActionCategory>> save_listeners_;

	public ActionCategoryFormPanel(String object_name, Account acct, AcctActionCategory category,
			DboFormPanel<?> parent, boolean read_only) throws DatabaseException, SQLException, Exception {
		this(object_name, category, parent, read_only);
		this.subject_acct_ = acct;
	}

	public ActionCategoryFormPanel(String object_name, AcctActionCategory category,
			DboFormPanel<?> parent, boolean read_only) throws Exception {
		super(object_name, category, parent,
				(FinanceManager.instance().isInitializationPhase()) ? read_only : false);

		this.save_listeners_ = new ArrayList<FormSaveListener<AcctActionCategory>>();

		this.addFormItem("Category Name", this.name_ = new JTextField(20));
		this.addFormItem("Parent Category", this.parent_ = new JComboBox());
		this.owner_accounts_model_ = new DefaultListModel();
		this.owner_accounts_ = new JList(owner_accounts_model_);
		this.owner_accounts_.setPreferredSize(new Dimension(200, 75));
		this.owner_accounts_.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		this.owner_accounts_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.owner_accounts_.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					/*TODO: pop-up with the option to edit/view account*/
					Object o = e.getSource();
					o.toString();
				}
			}
		});
		JScrollPane list_container = new JScrollPane(this.owner_accounts_);
		this.addFormItem("Account(s)", list_container);
		this.addFormItem(this.balance_system_ = AcctBalanceSystem.getFormPanel(
				"Category Balance System", category.getBalanceSystem(), parent, this.isReadOnly()));

		this.initControls();
		this.updateForm();
	}

	private void initControls() {
		JButton save;
		JButton update;

		controls_ = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		if (!this.isReadOnly()) {
			controls_.add(save = new JButton("Save"));
			controls_.add(update = new JButton("Update View"));

			save.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					retrieveForm();
				}
			});

			update.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						getDatabaseObject().retrieve();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (DatabaseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
		}

		this.add(controls_, BorderLayout.SOUTH);
	}

	public boolean addListener(FormSaveListener<AcctActionCategory> l) {
		return this.save_listeners_.add(l);
	}

	public boolean removeListener(FormSaveListener<AcctActionCategory> l) {
		return this.save_listeners_.remove(l);
	}

	private void fireReplaceCategory(AcctActionCategory old, AcctActionCategory new_acct) {
		for (FormSaveListener<AcctActionCategory> l : this.save_listeners_)
			l.replaceNode(old, new_acct,this);
	}

	private void fireNewCategory(AcctActionCategory new_acct) {
		for (FormSaveListener<AcctActionCategory> l : this.save_listeners_)
			l.newNode(new_acct,this);
	}

	@Override
	public void retrieveForm() {
		if (this.isReadOnly())
			return;

		this.balance_system_.retrieveForm();
		String name = this.name_.getText();
		double balance = this.balance_system_.getDatabaseObject().getBalance();
		TransactionType normal_balance = this.balance_system_.getDatabaseObject().getNormalBalance();
		AcctActionCategory parent = (AcctActionCategory) this.parent_.getSelectedItem();

		AcctActionCategory new_cat = new AcctActionCategory(name, parent, normal_balance, balance);

		if (dbo_.getOwnerAccts().size() > 1) {
			if (JOptionPane
					.showConfirmDialog(
							this,
							"This category is shared between multiple accounts. Do you want these changes to effect all account?",
							"Shared Category", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
				return;
			}
		}
		try {
			if (this.subject_acct_ == null) {
				/*editing cat, or replacing*/
				if (this.dbo_.replaceActionCategory(new_cat)) {
					this.fireReplaceCategory(dbo_, new_cat);
					dbo_ = new_cat;
				}
			} else {
				/*operate in the realm of the subject account*/
				if (subject_acct_.isValidCategory(dbo_)) {
					if (subject_acct_.replaceActionCategory(dbo_, new_cat)) {
						this.fireReplaceCategory(dbo_, new_cat);
						dbo_ = new_cat;
					}
				} else if (subject_acct_.addActionCategory(new_cat)) {
					this.fireNewCategory(new_cat);
					dbo_ = new_cat;
				}
			}
		} catch (LoggedInException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateForm() {
		if (dbo_.getName() != null)
			this.name_.setText(dbo_.getName());
		this.parent_.removeAllItems();
		if (this.subject_acct_ != null) {
			this.parent_.addItem(null);
			Collection<AcctActionCategory> acct_cats = this.subject_acct_.getAllCategories();
			for (AcctActionCategory cat : acct_cats) {
				this.parent_.addItem(cat);
			}
		} else {
			this.parent_.addItem(dbo_.getParent());
		}
		this.parent_.setSelectedItem(dbo_.getParent());
		this.owner_accounts_model_.clear();
		for (String owner_acct : dbo_.getOwnerAccts())
			this.owner_accounts_model_.addElement(owner_acct);
		this.balance_system_.updateForm();
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		final JFrame f = new JFrame();

		try {
			System.out.println(UserManager.instance().login("Scott", "test"));

			Account test_p = new Account(10, "Test Parent", null, AcctType.ASSET, TransactionType.CREDIT,
					AcctTimeScale.LONG_TERM,null);
			Account test_c = new Account(12, "Test other", null, AcctType.ASSET, TransactionType.CREDIT,
					AcctTimeScale.LONG_TERM,null);
			FinanceManager.instance().getAccountManager().registerAccount(test_p);
			FinanceManager.instance().getAccountManager().registerAccount(test_c);
			AcctActionCategory cat_p = new AcctActionCategory("root cat", null, TransactionType.CREDIT);
			AcctActionCategory cat_c = new AcctActionCategory("child 1 cat", cat_p,
					TransactionType.CREDIT);
			test_p.addActionCategory(cat_p);
			test_p.addActionCategory(cat_c);

			test_c.addActionCategory(cat_c);

			final ActionCategoryFormPanel cfp = new ActionCategoryFormPanel(cat_c.getName(), test_p,
					cat_c, null, false);
			f.setLayout(new BorderLayout());
			f.add(cfp, BorderLayout.CENTER);

			f.setVisible(true);
			f.pack();
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		} catch (Exception e) {
			UserManager.instance().logout();
			throw e;
		}
	}
}

package reman.client.app.finance.gui;

import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctActionCategory;
import reman.client.app.finance.accounts.AcctAmount;
import reman.client.app.finance.accounts.AcctBalanceSystem;
import reman.client.app.finance.accounts.exceptions.InvalidAmountException;
import reman.client.app.finance.accounts.exceptions.InvalidCategoryException;
import reman.client.app.finance.journals.JournalEntryLineItem;
import reman.client.gui.forms.DboFormPanel;

public class JournalEntryLineItemFormPanel extends DboFormPanel<JournalEntryLineItem> {

	private JTextField description_;
	private JTree accounts_;
	private JComboBox categories_;

	private DboFormPanel<AcctBalanceSystem> amount_;

	public JournalEntryLineItemFormPanel(String object_name, JournalEntryLineItem o,
			DboFormPanel<?> parent, boolean read_only) throws Exception {
		super(object_name, o, parent, read_only);
		this.setAutoCommit(false);
		
		this.accounts_ = new JTree(new AccountTreeModel("All Accounts"));
		this.accounts_.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		JScrollPane account_scoller = new JScrollPane(accounts_, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.addFormItem("Account", account_scoller);
		this.addFormItem("Category", this.categories_ = new JComboBox());
		this.addFormItem(this.amount_ = new BalanceSystemFormPanel("Amount", new AcctBalanceSystem(o
				.getAmount().getTransactionType(), o.getAmount()), this, this.read_only_));
		this.addFormItem("Description", this.description_ = new JTextField(40));

		this.accounts_.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				updateCategories();
			}
		});

		this.updateForm();
	}

	private void updateCategories() {
		this.categories_.removeAllItems();
		Account selected_acct = (Account) AccountTreeModel.getSelectedLastAccount(this.accounts_);
		if (selected_acct != null) {
			Collection<AcctActionCategory> categories = selected_acct.getAllCategories();
			for (AcctActionCategory cat : categories) {
				this.categories_.addItem(cat);
			}
		}
	}

	@Override
	public void retrieveForm() {
		String description = this.description_.getText();
		Account acct = (Account) AccountTreeModel.getSelectedLastAccount(this.accounts_);
		AcctActionCategory cat = (AcctActionCategory) this.categories_.getSelectedItem();

		this.amount_.retrieveForm();
		TransactionType normal_balance = this.amount_.getDatabaseObject().getNormalBalance();
		double balance = this.amount_.getDatabaseObject().getBalance();

		dbo_.setDescription(description);
		dbo_.setAccount(acct);
		try {
			dbo_.setCategory(cat);
		} catch (InvalidCategoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAmountException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dbo_.setAmount(new AcctAmount(balance, normal_balance));
	}

	@Override
	public void updateForm() {
		this.amount_.updateForm();
		if (dbo_.getAccount() != null) {
			AccountTreeModel m = (AccountTreeModel) this.accounts_.getModel();
			TreePath path = m.getPathToRoot(dbo_.getAccount());
			this.accounts_.setSelectionPath(path);
		}
		if (dbo_.getCategory() != null)
			this.categories_.setSelectedItem(dbo_.getCategory());
		if (dbo_.getDescription() != null)
			this.description_.setText(dbo_.getDescription());
	}
}

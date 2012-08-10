package reman.client.app.finance.gui;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.accounts.AcctActionCategory;
import reman.client.app.finance.equations.exceptions.MathException;
import reman.client.app.finance.journals.JournalEntryTemplateLineItem;
import reman.client.gui.forms.DboFormPanel;

public class JounralEntryTemplateLineItemPanel extends DboFormPanel<JournalEntryTemplateLineItem> {

	private JTextField description_;
	private JTextField eq_;
	private JComboBox normal_balance_;
	private JTree account_apply_to_;
	private JTree cat_apply_to_;

	public JounralEntryTemplateLineItemPanel(String object_name, JournalEntryTemplateLineItem item,
			DboFormPanel<?> parent, boolean read_only) throws Exception {
		super(object_name, item, parent, read_only);

		this.addFormItem("Description", this.description_ = new JTextField(20));
		this.addFormItem("Equation", this.eq_ = new JTextField(30));
		this.addFormItem("Normal Contribution", this.normal_balance_ = new JComboBox(TransactionType
				.values()));

		this.account_apply_to_ = new JTree(new AccountTreeModel("All Accounts"));
		JScrollPane acct_tree_scroll = new JScrollPane(account_apply_to_,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.addFormItem("Account", acct_tree_scroll);

		this.cat_apply_to_ = new JTree(new AcctActionCategoryTreeModel(new AcctActionCategory(
				"Categories", null, TransactionType.CREDIT)));
		cat_apply_to_.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		cat_apply_to_.setPreferredSize(new Dimension(-1, 200));
		final JScrollPane cat_tree_scroll = new JScrollPane(cat_apply_to_,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.addFormItem("Category", cat_tree_scroll);

		this.account_apply_to_.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.account_apply_to_.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				Account acct = AccountTreeModel.getSelectedLastAccount(account_apply_to_);

				if (acct != null) {
					cat_apply_to_ = new JTree(new AcctActionCategoryTreeModel(acct));
				} else {
					cat_apply_to_.setSelectionPath(null);
				}
			}
		});

		this.updateForm();
	}

	@Override
	public void retrieveForm() {
		dbo_.setDescription(description_.getText());

		String t = eq_.getText();
		try {
			dbo_.setEquation(eq_.getText());
		} catch (MathException e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}

		TransactionType norm = (TransactionType) this.normal_balance_.getSelectedItem();
		if (norm != null)
			dbo_.setNormalBalance(norm);

		Account acct = AccountTreeModel.getSelectedLastAccount(this.account_apply_to_);
		if (acct != null)
			dbo_.setApplyToAccount(acct);

		AcctActionCategory cat = AcctActionCategoryTreeModel
				.getSelectedLastCategory(this.cat_apply_to_);
		if (cat != null)
			dbo_.setApplyToCategory(cat);
	}

	@Override
	public void updateForm() {
		if (dbo_.getDescription() != null)
			this.description_.setText(dbo_.getDescription());

		String t = dbo_.getEquation().toString();
		if (dbo_.getEquation() != null) {
			this.eq_.setText(dbo_.getEquation().toString());
		}

		if (dbo_.getNormalBalance() != null) {
			this.normal_balance_.setSelectedItem(dbo_.getNormalBalance());
		}
		AccountTreeModel m = (AccountTreeModel) this.account_apply_to_.getModel();
		this.account_apply_to_.setSelectionPath(m.getPathToRoot(dbo_.getApplyToAccount()));

		AcctActionCategoryTreeModel mo = (AcctActionCategoryTreeModel) this.cat_apply_to_.getModel();
		this.cat_apply_to_.setSelectionPath(mo.getPathToRoot(dbo_.getApplyToCategory()));
	}

	public void setEquationReadOnly(boolean read_only) {
		eq_.setEditable(!read_only);
	}
}

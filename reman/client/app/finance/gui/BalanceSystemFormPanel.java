package reman.client.app.finance.gui;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.AcctAmount;
import reman.client.app.finance.accounts.AcctBalanceSystem;
import reman.client.gui.forms.DboFormPanel;

public class BalanceSystemFormPanel extends DboFormPanel<AcctBalanceSystem> {

	private JComboBox normal_balance_;
	private JTextField balance_;

	public BalanceSystemFormPanel(String object_name, AcctBalanceSystem o, DboFormPanel<?> parent,
			boolean read_only) throws Exception {
		super(object_name, o, parent, read_only);

		this.addFormItem("Normal Balance", this.normal_balance_ = new JComboBox(TransactionType
				.values()));
		this.addFormItem("Balance", this.balance_ = new JTextField(15));
	}

	@Override
	public void retrieveForm() {

		TransactionType normal_bal = (TransactionType) this.normal_balance_.getSelectedItem();
		double bal = 0;
		try {
			bal = Double.parseDouble(this.balance_.getText());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dbo_.setNormalBalance(normal_bal);
		dbo_.setBalance(bal);
	}

	@Override
	public void updateForm() {
		this.normal_balance_.setSelectedItem(dbo_.getNormalBalance());
		this.balance_.setText(String.valueOf(dbo_.getBalance()));
	}
}

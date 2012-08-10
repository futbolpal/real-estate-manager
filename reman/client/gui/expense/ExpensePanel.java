package reman.client.gui.expense;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import reman.client.app.finance.TransactionType;
import reman.client.app.finance.gui.JounralEntryTemplateLineItemPanel;
import reman.client.app.finance.journals.JournalEntryTemplateLineItem;
import reman.client.basetypes.Expense;
import reman.client.basetypes.TimeRange;
import reman.client.gui.forms.DboFormPanel;
import reman.common.database.UserManager;

public class ExpensePanel extends DboFormPanel<Expense> {

	private JTextField code_;
	private JTextField description_;
	private JTextField price_;
	private TimeRangePanel date_range_;
	private JCheckBox tax_applicable_;
	private JTextField tax_rate_;

	private JounralEntryTemplateLineItemPanel expense_info_;
	private JounralEntryTemplateLineItemPanel tax_info_;

	public ExpensePanel(String title, Expense e, DboFormPanel<?> parent, boolean read_only)
			throws Exception {
		super(title, e, parent, read_only);

		this.addFormItem("Code", this.code_ = new JTextField(10));
		this.addFormItem("Price", this.price_ = new JTextField(10));
		this.addFormItem("Description", this.description_ = new JTextField(30));
		this.addFormItem(this.date_range_ = new TimeRangePanel("Effective Dates", dbo_
				.getEffectiveTime(), this, this.read_only_));

		this.addFormItem(this.expense_info_ = new JounralEntryTemplateLineItemPanel("Expense Finances",
				new JournalEntryTemplateLineItem("", null, TransactionType.CREDIT, null, null), this,
				this.read_only_));

		this.addFormItem("Tax Applicable", this.tax_applicable_ = new JCheckBox());
		this.tax_applicable_.setSelected(true);
		
		this.addFormItem("Tax Rate", this.tax_rate_ = new JTextField(10));

		this.addFormItem(this.tax_info_ = new JounralEntryTemplateLineItemPanel("Tax Finances",
				new JournalEntryTemplateLineItem("", null, TransactionType.CREDIT, null, null), this,
				this.read_only_));

		this.tax_applicable_.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tax_rate_.setVisible(tax_applicable_.isSelected());
				tax_info_.setVisible(tax_applicable_.isSelected());
			}
		});
		
		this.expense_info_.setEquationReadOnly(true);
		this.tax_info_.setEquationReadOnly(true);
		
		this.updateForm();
	}

	@Override
	public void retrieveForm() {
		// TODO Auto-generated method stub
		dbo_.setDescription(description_.getText());
		dbo_.setCode(code_.getText());
		try {
			dbo_.setPrice(Double.parseDouble(price_.getText()));
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
		dbo_.setTaxed(this.tax_applicable_.isSelected());
		if (this.tax_rate_.isVisible()) {
			try {
				dbo_.setTaxRate(Double.parseDouble(this.tax_rate_.getText()));
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, e.getMessage());
			}
		}

		this.date_range_.retrieveForm();
		this.expense_info_.retrieveForm();
		if (tax_info_.isVisible())
			this.tax_info_.retrieveForm();
	}

	@Override
	public void updateForm() {
		if (dbo_.getDescription() != null)
			this.description_.setText(dbo_.getDescription());
		if (dbo_.getCode() != null)
			this.code_.setText(dbo_.getCode());

		this.price_.setText(Double.toString(dbo_.getPrice()));
		this.tax_applicable_.setSelected(dbo_.isTaxed());

		if (this.tax_rate_.isVisible()) {
			this.tax_rate_.setText(Double.toString(dbo_.getTaxRate()));
		}

		this.date_range_.updateForm();
		this.expense_info_.updateForm();
		if (tax_info_.isVisible())
			this.tax_info_.updateForm();
	}

	public static void main(String[] args) throws Exception {
		UserManager.instance().login("Scott", "test");

		JFrame f = new JFrame();

		JScrollPane test = new JScrollPane(new ExpensePanel("Test", new Expense("TA", "testing", .34,
				TransactionType.CREDIT, null, false, 0, null, TransactionType.CREDIT, null, null,
				new TimeRange()), null, false), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		f.add(test);

		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

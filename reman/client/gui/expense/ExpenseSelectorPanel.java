package reman.client.gui.expense;

import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JPanel;

import reman.client.basetypes.Expense;
import reman.client.gui.basic_gui.BasicSelectionPanel;

public abstract class ExpenseSelectorPanel<T extends Expense> extends JPanel {
	protected BasicSelectionPanel<T> selector_;

	public ExpenseSelectorPanel(String title, Collection<T> options, Collection<T> selected,
			int width, int heigth) {

		this.selector_ = new BasicSelectionPanel<T>(title, options, selected, width, heigth, false);

	}

	public static void main(String[] args) {
		JFrame f = new JFrame();

		/*OfficeProjectManager.instance().getCurrentProject().getExpenseManager().register(new Fee("testing"));
		ExpenseSelectorPanel<Deduction> p = new ExpenseSelectorPanel<Deduction>("test",
				new ArrayList<Deduction>(), 400, 400);*/

		//f.add(p);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

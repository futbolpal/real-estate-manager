package reman.client.gui.calculator;

import java.awt.event.ActionListener;

import javax.swing.JButton;

public class OperatorButton extends JButton {
	private String operator_;
	public OperatorButton(String op, ActionListener l) {
		super(op);
		operator_ = op;
		this.addActionListener(l);
	}
	
	public String getOperator() {
		return operator_;
	}
}

package reman.client.gui.calculator;

import java.awt.event.ActionListener;

import javax.swing.JButton;

public class NumericButton extends JButton {
	private int number_;
	
	public NumericButton(int n, ActionListener l) {
		super(String.format("%d", n));
		number_ = n;
		this.addActionListener(l);
	}
	
	public int getNumber() {
		return number_;
	}
}

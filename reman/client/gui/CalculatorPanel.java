package reman.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import reman.client.app.finance.equations.Equation;
import reman.client.app.finance.equations.exceptions.MathException;
import reman.client.gui.calculator.NumericButton;
import reman.client.gui.calculator.OperatorButton;

public class CalculatorPanel extends JPanel implements ActionListener {
	private String expression, current_entry;

	private JTextField screen;

	public CalculatorPanel() {
		expression = "";
		current_entry = "";
		screen = new JTextField();
		refreshScreen();
		screen.setPreferredSize(new Dimension(-1, 40));
		screen.setFont(screen.getFont().deriveFont(16f));
		screen.setEditable(false);

		JPanel pad = new JPanel(new GridLayout(5, 4));

		NumericButton num_0 = new NumericButton(0, this);
		NumericButton num_1 = new NumericButton(1, this);
		NumericButton num_2 = new NumericButton(2, this);
		NumericButton num_3 = new NumericButton(3, this);
		NumericButton num_4 = new NumericButton(4, this);
		NumericButton num_5 = new NumericButton(5, this);
		NumericButton num_6 = new NumericButton(6, this);
		NumericButton num_7 = new NumericButton(7, this);
		NumericButton num_8 = new NumericButton(8, this);
		NumericButton num_9 = new NumericButton(9, this);
		OperatorButton div = new OperatorButton("/", this);
		OperatorButton mul = new OperatorButton("*", this);
		OperatorButton add = new OperatorButton("+", this);
		OperatorButton min = new OperatorButton("-", this);
		OperatorButton eql = new OperatorButton("=", this);
		JButton bksp = new JButton("Bksp");
		JButton ce = new JButton("CE");
		JButton clr = new JButton("Clr");
		JButton pos_neg = new JButton("+/-");
		JButton dec = new JButton(".");
		dec.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				screen.setForeground(Color.BLACK);
				if (current_entry.indexOf(".") == -1) {
					if ((current_entry.length() == 0) && isNewEntry())
						current_entry = "0.";
					else
						current_entry += ".";
					refreshScreen();
				}
			}
		});
		bksp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				screen.setForeground(Color.BLACK);
				if (current_entry.length() > 0) {
				 	current_entry = current_entry.substring(0, current_entry.length() - 1);
				 	refreshScreen();
				} else if (expression.length() > 0) {
					expression = expression.substring(0, expression.length() - 1);
					refreshScreen();
				}
			}
		});
		clr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				screen.setForeground(Color.BLACK);
				expression = "";
				current_entry = "";
				refreshScreen();
			}
		});
		ce.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				screen.setForeground(Color.BLACK);
				current_entry = "";
				refreshScreen();
			}
		});
		pos_neg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (current_entry.startsWith("-"))
					current_entry = current_entry.substring(1, current_entry.length());
				else
					current_entry = "-" + current_entry;
				refreshScreen();
			}
		});

		pad.add(bksp);
		pad.add(ce);
		pad.add(clr);
		pad.add(pos_neg);
		pad.add(num_7);
		pad.add(num_8);
		pad.add(num_9);
		pad.add(div);
		pad.add(num_4);
		pad.add(num_5);
		pad.add(num_6);
		pad.add(mul);
		pad.add(num_1);
		pad.add(num_2);
		pad.add(num_3);
		pad.add(min);
		pad.add(num_0);
		pad.add(dec);
		pad.add(eql);
		pad.add(add);

		this.setLayout(new BorderLayout());
		this.add(screen, BorderLayout.NORTH);
		this.add(pad, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		screen.setForeground(Color.BLACK);
		Object source = e.getSource();
		if (source instanceof NumericButton) {
			NumericButton btn = (NumericButton) source;
			current_entry += String.format("%d", btn.getNumber());
			refreshScreen();
			
		} else if (source instanceof OperatorButton) {
			OperatorButton btn = (OperatorButton)source;
			if (btn.getOperator().equals("=")) {
				expression += current_entry;
				current_entry = "";
				Double result = null;
				try {
					Equation eq = new Equation(expression);
					result = eq.getValue();
				} catch (MathException e1) {
					//no need to do anything
				}
				if (result != null) {
					expression = formatResult(result);
					current_entry = "";
					refreshScreen();
				} else {
					//error
					screen.setForeground(Color.RED);
				}
			} else {
				expression += current_entry + btn.getOperator();
				current_entry = "";
				refreshScreen();
			}
		}
	}
	
	private void refreshScreen() {
		String s = expression + current_entry;
		if (s.isEmpty())
			screen.setText("0");
		else
			screen.setText(s);
	}
	
	private String formatResult(Double result) {
		String s = result.toString();
		if (s.endsWith(".0"))
			return s.substring(0, s.length() - 2);
		else
			return s;
	}

	private boolean isNewEntry() {
		if (expression.length() > 0) {
			String s = expression.substring(expression.length() - 1, expression.length());
			if ((s.equals("+")) || (s.equals("-")) || (s.equals("/"))
					|| (s.equals("*")))
				return true;
			else
				return false;
		}
		return true;
	}
	
}

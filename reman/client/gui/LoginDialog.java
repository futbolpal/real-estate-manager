package reman.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import reman.client.gui.forms.FormPanel;
import reman.common.database.UserManager;

/**
 * This prevents a blocking modal dialog to log the user in.  
 * @author jonathan
 *
 */
public class LoginDialog extends JDialog {
	private JTextField user_;
	private JPasswordField pass_;
	private JLabel status_;

	private JButton login_;
	private JButton close_;
	private JButton register_;

	private LoginDialog() {
		user_ = new JTextField(20);
		pass_ = new JPasswordField(20);
		status_ = new JLabel();

		login_ = new JButton("Log in");
		login_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final String username = user_.getText();
				final String password = String.valueOf(pass_.getPassword());
				Thread t = new Thread(new Runnable() {
					public void run() {
						try {
							boolean login = UserManager.instance().login(username, password);
							if (login) {
								LoginDialog.this.dispose();
							} else {
								status_.setForeground(Color.red);
								status_.setText("Login failed.  Try again.");
								login_.setEnabled(true);
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				});
				t.start();
				login_.setEnabled(false);
				status_.setForeground(Color.GREEN);
				status_.setText("Attempting to login...");

			}
		});

		close_ = new JButton("Close");
		close_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		register_ = new JButton("Register...");
		register_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new RegistrationDialog();
			}
		});

		FormPanel form = new FormPanel();
		form.addFormItem("Username", user_);
		form.addFormItem("Password", pass_);
		form.addFormItem("", status_);

		JPanel controls = new JPanel();
		controls.setLayout(new FlowLayout(FlowLayout.RIGHT));
		controls.add(register_);
		controls.add(Box.createHorizontalStrut(25));
		controls.add(login_);
		controls.add(close_);

		this.setModal(true);
		this.setLayout(new BorderLayout());
		this.add(form, BorderLayout.CENTER);
		this.add(controls, BorderLayout.SOUTH);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.getRootPane().setDefaultButton(login_);
		this.setSize(450, 200);
		this.setLocationRelativeTo(this);
		this.setVisible(true);
	}

	/**
	 * This function creates a modal blocking dialog that waits for the user
	 * to present their login credentials.  
	 * @return true if login was successful, false otherwise.
	 */
	public static boolean login() {
		LoginDialog ld = new LoginDialog();
		return true;
	}
}

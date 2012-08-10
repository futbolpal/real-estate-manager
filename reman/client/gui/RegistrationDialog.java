package reman.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import reman.client.gui.forms.FormPanel;
import reman.common.database.OfficeProjectManager;
import reman.common.database.UserManager;

/**
 * This is the registration dialog for the creating new users.  A new user can either
 * create a new office project or enter the code of an existing office project 
 * @author jonathan
 *
 */
public class RegistrationDialog extends JDialog {
	private JTextField username_;
	private JPasswordField password_1_;
	private JPasswordField password_2_;
	private JTextField display_name_;
	private JTextField project_code_;
	private JCheckBox new_office_;
	private JLabel status_;

	private JButton cancel_;
	private JButton register_;

	public RegistrationDialog() {
		username_ = new JTextField(20);
		password_1_ = new JPasswordField(20);
		password_2_ = new JPasswordField(20);
		display_name_ = new JTextField(20);
		project_code_ = new JTextField(20);
		new_office_ = new JCheckBox();
		status_ = new JLabel();

		FormPanel form = new FormPanel();
		form.addFormSeparator("Login Credentials");
		form.addFormItem("Username", username_);
		form.addFormItem("Password", password_1_);
		form.addFormItem("Confirm Password", password_2_);
		form.addFormSeparator("User Preferences");
		form.addFormItem("Display Name", display_name_);
		form.addFormSeparator("Office Information");
		form.addFormItem("New office", new_office_);
		form.addFormItem("Office Project Code", project_code_);

		cancel_ = new JButton("Cancel");
		cancel_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		register_ = new JButton("Register");
		register_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				final String username = username_.getText();
				final String password1 = String.valueOf(password_1_.getPassword());
				final String password2 = String.valueOf(password_2_.getPassword());
				final String display = display_name_.getText();
				final String code = project_code_.getText();

				if (!password1.equals(password2) || code.equals("") || display.equals("")) {
					status_.setForeground(Color.RED);
					status_.setText("Registration not complete.");
					return;
				}

				Thread t = new Thread(new Runnable() {
					public void run() {
						try {
							boolean login = UserManager.instance().register(username, password1, display, code,
									new_office_.isSelected());
							if (login) {
								RegistrationDialog.this.dispose();
							} else {
								status_.setForeground(Color.red);
								status_.setText("Registration failed.  Username may be in "
										+ "use or project code may be invalid.");
								register_.setEnabled(true);
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				});
				t.start();
				register_.setEnabled(false);
				status_.setForeground(Color.GREEN);
				status_.setText("Validating registration...");
			}
		});

		JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		controls.add(register_);
		controls.add(cancel_);

		this.getRootPane().setDefaultButton(register_);
		this.setModal(true);
		this.setLayout(new BorderLayout());
		this.add(form, BorderLayout.CENTER);
		this.add(controls, BorderLayout.SOUTH);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setSize(450, 400);
		this.setLocationRelativeTo(this);
		this.setVisible(true);

	}
}

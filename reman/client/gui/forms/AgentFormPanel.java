package reman.client.gui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import reman.client.basetypes.Agent;
import reman.client.basetypes.Gender;
import reman.client.gui.DateField;
import reman.client.gui.TimeRangeField;
import reman.common.database.DatabaseObject;

public class AgentFormPanel extends DboFormPanel<Agent> {
	private JTextField title_;
	private JTextField first_;
	private JTextField middle_;
	private JTextField last_;
	private DateField dob_;
	private JComboBox gender_;
	private TimeRangeField active_;
	private LicenseFormPanel license_;
	private MLSInfoFormPanel mls_;
	private JButton manage_partners_;
	private JButton manage_liable_properties_;
	private JButton manage_fees_;

	public AgentFormPanel(String name, Agent a, DboFormPanel<? extends DatabaseObject> parent,
			boolean read_only) {
		super("Agent", a, parent, read_only);
		this.addFormItem("Title", title_ = new JTextField(20));
		this.addFormItem("First", first_ = new JTextField(20));
		this.addFormItem("Middle", middle_ = new JTextField(20));
		this.addFormItem("Last", last_ = new JTextField(20));

		this.addFormItem("DOB", dob_ = new DateField(this));

		this.addFormItem("Gender", gender_ = new JComboBox(Gender.values()));
		this
				.addFormItem(active_ = new TimeRangeField("Active Time", a.getActiveTime(), this, read_only));
		this.addFormItem(license_ = new LicenseFormPanel(a.getLicense(), this, read_only));
		this.addFormItem(mls_ = new MLSInfoFormPanel(a.getMLSInfo(), this, read_only));
		this.addFormItem("", manage_partners_ = new JButton("Partners..."));
		this.addFormItem("", manage_liable_properties_ = new JButton("Liable Properties..."));
		manage_liable_properties_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			}
		});
		manage_partners_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		this.updateForm();
	}

	public void retrieveForm() {
		Agent a = dbo_;
		a.setTitleName(title_.getText());
		a.setFirstName(first_.getText());
		a.setMiddleName(middle_.getText());
		a.setLastName(last_.getText());
		a.setGender((Gender) gender_.getSelectedItem());
		a.setDOB(dob_.getDate());
		active_.retrieveForm();
		license_.retrieveForm();
		mls_.retrieveForm();
	}

	public void updateForm() {
		Agent a = dbo_;
		if (a.getTitle() != null)
			title_.setText(a.getTitle());
		if (a.getFirstName() != null)
			first_.setText(a.getFirstName());
		if (a.getMiddleName() != null)
			middle_.setText(a.getMiddleName());
		if (a.getLastName() != null)
			last_.setText(a.getLastName());
		if (a.getGender() != null)
			gender_.setSelectedItem(a.getGender());
		if (a.getDOB() != null)
			dob_.setDate(a.getDOB());
		active_.updateForm();
		license_.updateForm();
		mls_.updateForm();
	}
}

package reman.client.gui.forms;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import reman.client.basetypes.License;
import reman.client.basetypes.State;
import reman.common.database.DatabaseObject;

public class LicenseFormPanel extends DboFormPanel<License> {
	private JTextField board_;
	private JTextField type_;
	private JTextField number_;
	private JComboBox state_;

	public LicenseFormPanel(License i, DboFormPanel<? extends DatabaseObject> parent,
			boolean read_only) {
		super("License", i, parent, read_only);
		this.addFormItem("Board", board_ = new JTextField(20));
		this.addFormItem("Type", type_ = new JTextField(20));
		this.addFormItem("Number", number_ = new JTextField(20));
		this.addFormItem("State", state_ = new JComboBox(State.values()));
	}

	public void retrieveForm() {
		dbo_.setBoard(board_.getText());
		dbo_.setType(type_.getText());
		dbo_.setNumber(number_.getText());
		dbo_.setState((State) state_.getSelectedItem());
	}

	public void updateForm() {
		board_.setText(dbo_.getBoard());
		type_.setText(dbo_.getType());
		number_.setText(dbo_.getNumber());
		state_.setSelectedItem(dbo_.getState());
	}

}
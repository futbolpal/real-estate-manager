package reman.client.gui.forms;

import javax.swing.JTextField;

import reman.client.basetypes.MLSInfo;
import reman.common.database.DatabaseObject;

public class MLSInfoFormPanel extends DboFormPanel<MLSInfo> {
	private JTextField username_;
	private JTextField id_;

	public MLSInfoFormPanel(MLSInfo i, DboFormPanel<? extends DatabaseObject> parent,
			boolean read_only) {
		super("MLS Info", i, parent, read_only);
		this.addFormItem("Username", username_ = new JTextField(20));
		this.addFormItem("ID", id_ = new JTextField(20));
	}

	public void retrieveForm() {
		dbo_.setUsername(username_.getText());
		dbo_.setMLSID(id_.getText());
	}

	public void updateForm() {
		username_.setText(dbo_.getUsername());
		id_.setText(dbo_.getMLSID());
	}

}
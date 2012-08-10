package reman.client.basetypes;

import reman.client.gui.forms.DboFormPanel;
import reman.client.gui.forms.MLSInfoFormPanel;
import reman.common.database.DatabaseObject;

public class MLSInfo extends DatabaseObject {
	private String username_;
	private String mls_id_;

	public MLSInfo() {

	}

	public String getUsername() {
		return username_;
	}

	public String getMLSID() {
		return mls_id_;
	}

	public void setMLSID(String id) {
		mls_id_ = id;
	}

	public void setUsername(String username) {
		username_ = username;
	}
}

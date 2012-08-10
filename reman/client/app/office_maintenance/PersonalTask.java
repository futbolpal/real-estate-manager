package reman.client.app.office_maintenance;

import reman.common.database.UserManager;

public class PersonalTask extends Task {
	private long user_id_;

	public PersonalTask() {
		user_id_ = UserManager.instance().getCurrentUserID();
		this.setChangeBroadCasted(false);
	}

}

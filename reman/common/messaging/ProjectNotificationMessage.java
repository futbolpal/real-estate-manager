package reman.common.messaging;

import reman.common.database.OfficeProjectManager;

public class ProjectNotificationMessage extends NotificationMessage {
	private long project_id_;

	public ProjectNotificationMessage() {
		project_id_ = OfficeProjectManager.instance().getCurrentProject().getID();
	}

	public long getProjectID() {
		return project_id_;
	}
}

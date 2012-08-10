package reman.common.messaging;

import java.io.Serializable;

import reman.common.database.UserManager;

public class NotificationMessage implements Serializable {
	public long source_;

	public NotificationMessage() {
		source_ = UserManager.instance().getCurrentUserID();
	}

	public long getSourceUserID() {
		return source_;
	}
}

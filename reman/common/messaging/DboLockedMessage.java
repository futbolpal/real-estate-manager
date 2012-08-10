package reman.common.messaging;

import reman.common.database.DatabaseObject;

public class DboLockedMessage extends DboChangedMessage {
	public boolean locked_;

	public DboLockedMessage(DatabaseObject o, boolean isLocked) {
		super(o);
		this.locked_ = isLocked;
	}

	public boolean isLocked() {
		return this.locked_;
	}

}

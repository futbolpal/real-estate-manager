package reman.common.messaging;

import reman.common.database.DatabaseObject;

public class DboChangedMessage extends ProjectNotificationMessage {
	private DatabaseObject object_;

	public DboChangedMessage(DatabaseObject o) {
		this.object_ = o;
	}

	public DatabaseObject getDatabaseObject() {
		return this.object_;
	}
}

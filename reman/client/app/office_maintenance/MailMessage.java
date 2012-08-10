package reman.client.app.office_maintenance;

import java.util.ArrayList;

import reman.common.database.DatabaseObject;
import reman.common.database.UserManager;
import reman.common.messaging.ChatMessage;

public class MailMessage extends ChatMessage {
	private ArrayList<DatabaseObject> attachments_;

	public MailMessage(String message) {
		super(UserManager.instance().getCurrentUserID(), message);
		attachments_ = new ArrayList<DatabaseObject>();
	}

	public ArrayList<DatabaseObject> getAttachments() {
		return attachments_;
	}

	public void addAttachment(DatabaseObject o) {
		attachments_.add(o);
	}
}

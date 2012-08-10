package reman.client.app.office_maintenance;

import java.util.ArrayList;

import reman.common.database.DatabaseObject;

public class Mailbox extends DatabaseObject {
	private transient ArrayList<MailboxListener> listeners_;
	private ArrayList<MailMessage> messages_;
	private ArrayList<String> folders_;

	public Mailbox() {
		listeners_ = new ArrayList<MailboxListener>();
		messages_ = new ArrayList<MailMessage>();
		folders_ = new ArrayList<String>();
	}

	public ArrayList<String> getFolderNames() {
		return folders_;
	}

	public synchronized ArrayList<MailMessage> getMessagesInFolder(Class<? extends MailMessage> c) {
		ArrayList<MailMessage> folder = new ArrayList<MailMessage>();
		for (MailMessage m : messages_) {
			if (m.getClass().equals(c))
				folder.add(m);
		}
		return folder;
	}

	public void addMailboxListener(MailboxListener l) {
		listeners_.add(l);
	}

	public void send(MailMessage m) {

	}

	public void receive(MailMessage m) {
		if (!folders_.contains(m.getClass()))
			folders_.add(m.getClass().getName());
		messages_.add(m);
		fireMessageReceived(m);
	}

	public synchronized void fireMessageReceived(MailMessage m) {
		MailEvent e = new MailEvent(m);
		for (MailboxListener l : listeners_) {
			l.messageReceived(e);
		}
	}
}

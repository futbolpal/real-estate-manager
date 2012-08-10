package reman.client.app.office_maintenance;

public class MailEvent {
	private MailMessage message_;

	public MailEvent(MailMessage message) {
		message_ = message;
	}

	public MailMessage getMessage() {
		return message_;
	}
}

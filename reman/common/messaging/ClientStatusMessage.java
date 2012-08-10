package reman.common.messaging;


public class ClientStatusMessage extends ProjectNotificationMessage {

	public static enum UserStatus {
		LOGGED_IN, LOGGED_OUT, IDLE;
	}

	private UserStatus user_status_;

	public ClientStatusMessage(UserStatus s) {
		this.user_status_ = s;
	}

	public UserStatus getUserStatus() {
		return this.user_status_;
	}
}

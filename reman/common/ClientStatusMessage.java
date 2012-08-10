package reman.common;

import reman.common.database.OfficeProjectManager;
import reman.common.database.UserManager;

public class ClientStatusMessage extends ProjectNotificationMessage {

  public static enum UserStatus {
    LOGGED_IN, LOGGED_OUT, IDLE;
  }

  private UserStatus user_status_;

  public ClientStatusMessage(UserStatus s) {
    this(UserManager.instance().getCurrentUserID(), OfficeProjectManager
	.instance().getCurrentProject().getID(), s);
  }

  public ClientStatusMessage(long user_id, long project_id, UserStatus s) {
    super(user_id, project_id);
    this.user_status_ = s;
  }

  public UserStatus getUserStatus() {
    return this.user_status_;
  }
}

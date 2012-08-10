package reman.common;

import reman.common.database.OfficeProjectManager;
import reman.common.database.UserManager;

public class DboChangedMessage extends ProjectNotificationMessage {
  private Class dbo_class_;
  private long dbo_id_;

  public DboChangedMessage(Class dbo_class, long dbo_id) {
    super(UserManager.instance().getCurrentUserID(), OfficeProjectManager
	.instance().getCurrentProject().getID());
    this.dbo_class_ = dbo_class;
    this.dbo_id_ = dbo_id;
  }

  public Class getDatabaseObjectClass() {
    return this.dbo_class_;
  }

  public long getDatabaseObjectID() {
    return this.dbo_id_;
  }
}

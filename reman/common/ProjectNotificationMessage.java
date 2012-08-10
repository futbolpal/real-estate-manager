package reman.common;

import reman.common.database.OfficeProjectManager;

public class ProjectNotificationMessage extends NotificationMessage {
  private long project_id_;

  public ProjectNotificationMessage(long source, long project_id) {
    super(source);
    project_id_ = project_id;
  }

  public long getProjectID() {
    return project_id_;
  }
}

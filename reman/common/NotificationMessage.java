package reman.common;

import java.io.Serializable;

public class NotificationMessage implements Serializable {
  public long source_;

  public NotificationMessage(long source) {
    source_ = source;
  }

  public long getSourceUserID() {
    return source_;
  }
}

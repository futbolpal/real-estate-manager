package reman.client.basetypes;

import java.sql.Timestamp;

public abstract class Worker extends Person {
  private TimeRange active_time_;

  public Worker() {
    active_time_ = new TimeRange();
  }

  public TimeRange getActiveTime() {
    return active_time_;
  }

  public Timestamp getStartDate() {
    return (Timestamp) this.active_time_.getBegin();
  }

  public Timestamp getEndDate() {
    return (Timestamp) this.active_time_.getEnd();
  }

  public void setStartDate(Timestamp s) {
    this.active_time_.setBegin(s);
  }

  public void setEndDate(Timestamp e) {
    this.active_time_.setEnd(e);
  }

}

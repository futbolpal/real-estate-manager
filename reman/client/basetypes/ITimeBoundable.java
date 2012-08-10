package reman.client.basetypes;

import java.sql.Timestamp;

public interface ITimeBoundable {
  public TimeRange getEffectiveTimeRange();

  public void setEffectiveTimeRange(TimeRange range);
}

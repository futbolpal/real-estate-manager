package reman.client.app.schedule;

import reman.client.basetypes.Location;
import reman.client.basetypes.TimeRange;
import reman.client.basetypes.UserDatabaseObject;

public class CalendarEvent extends UserDatabaseObject {
  private boolean all_day_;
  private TimeRange range_;
  private Location location_;

  public CalendarEvent() {
    this(null, null, null);
  }

  public CalendarEvent(String name, TimeRange range) {
    this(name, range, null);
  }

  public CalendarEvent(String name, TimeRange range, Location location) {
    this.setName(name);
    location_ = location;
    setTimeRange(range);
  }

  public boolean isAllDay() {
    return all_day_;
  }

  public void setTimeRange(TimeRange r) {
    all_day_ = (r == null);
    range_ = r;
  }

  public TimeRange getTimeRange() {
    return range_;
  }

  public void setLocation(Location l) {
    location_ = l;
  }

  public Location getLocation() {
    return location_;
  }
}

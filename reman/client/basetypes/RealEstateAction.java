package reman.client.basetypes;

import java.util.ArrayList;

import reman.client.app.finance.journals.JournalEntry;
import reman.common.database.DatabaseObject;

public abstract class RealEstateAction extends DatabaseObject implements
    ITimeBoundable {
  protected TimeRange time_range_;
  protected Property property_;
  protected ArrayList<Document> docs_;
  protected ArrayList<JournalEntry> costs_incured_;
  protected ArrayList<Commission> referrals_;

  public RealEstateAction(Property prop) {
  	this.property_=prop;
  }
  
  public TimeRange getEffectiveTimeRange() {
    return this.time_range_;
  }

  public void setEffectiveTimeRange(TimeRange range) {
    this.time_range_ = range;
  }
  
  public Property getProperty() {
  	return this.property_;
  }
}

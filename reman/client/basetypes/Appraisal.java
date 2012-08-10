package reman.client.basetypes;

import java.sql.Timestamp;

import reman.common.database.DatabaseObject;

public class Appraisal extends DatabaseObject {
  private Timestamp performed_time_;
  private double price_appraised_;
  private Company performing_comp_;
  private Person perfomring_worker_;

  public Appraisal() {
    this.performed_time_ = new Timestamp(0L);
    this.price_appraised_ = 0;
    this.performing_comp_ = new Company();
    /*TODO: what to initilize performing worker to?*/
    this.perfomring_worker_ = null;
  }
}

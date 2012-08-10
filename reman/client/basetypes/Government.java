package reman.client.basetypes;

import java.util.ArrayList;

public class Government extends OrganizationEntity {

  private ArrayList<GovernmentWorker> representatives_;

  public Government() {
    this.representatives_ = new ArrayList<GovernmentWorker>();
  }

  public ArrayList<GovernmentWorker> getWorkers() {
    return this.representatives_;
  }
}

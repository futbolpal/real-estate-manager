package reman.client.basetypes;

import java.util.ArrayList;

public abstract class OrganizationEntity {
  protected ArrayList<Office> offices_;

  /*TODO: figure out generic type so that sub classes can return
   * an array of type that extends Person*/
  public abstract ArrayList<? extends Person> getWorkers();
}

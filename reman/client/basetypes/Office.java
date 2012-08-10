package reman.client.basetypes;

import java.util.ArrayList;

public class Office extends Property {
  private ArrayList<Worker> workers_;

  public Office() {
    this.workers_ = new ArrayList<Worker>();
  }
}

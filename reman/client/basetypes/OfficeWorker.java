package reman.client.basetypes;

public abstract class OfficeWorker extends Worker {
  private Office owning_office_;

  public OfficeWorker() {
  }

  public Office getOwningOffice() {
    return owning_office_;
  }

  public void setOwningOffice(Office o) {
    owning_office_ = o;
  }
}

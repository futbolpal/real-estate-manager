package reman.client.basetypes;

public enum PropertyType {
  RES("Residential"), COM("Commercial"), GOV("Goverment");

  private String name_;

  private PropertyType(String name) {
    this.name_ = name;
  }

  public String getName() {
    return this.name_;
  }
}

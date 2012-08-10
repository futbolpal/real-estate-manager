package reman.client.basetypes;

public enum Gender {
  MALE, FEMALE;

  public String toString() {
    String name = this.name().toLowerCase();
    char f = Character.toUpperCase(name.charAt(0));
    return f + name.substring(1);
  }
}

package reman.client.basetypes;

import reman.common.database.DatabaseObject;

public class Document extends DatabaseObject {
  private double size_;
  private byte[] data_;

  public Document() {
    this.size_ = 0;
    /*TODO: how to represent (save, load..) file in db & java*/
    this.data_ = null;
  }
}

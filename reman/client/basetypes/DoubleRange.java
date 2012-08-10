package reman.client.basetypes;

import reman.common.database.DatabaseObject;

/**
 * No generics means that a range must be made for each type
 * @author Scott
 *
 */
public class DoubleRange extends DatabaseObject{
	private Double begin_ = null;
  private Double end_ = null;
  
  public DoubleRange()
  {
	  /*used for DBO construction*/
  }
  
  public DoubleRange(Double begin, Double end) {
    this.setBegin(begin);
    this.setEnd(end);
  }
  
  public void setBegin(Double begin) {
    if (begin == null)
    {
    	System.err.println("'begin' param passed as 'null' in 'DoubleRange' class 'setBegin()' method. Obj="+this.toString());
      return;
    }

    //if end_ has not been initilized or the parameter value is <= existing end_, valid begin
    if (end_ == null || begin.compareTo(end_) <= 0)
      begin_ = begin;
    else {
      //DoubleODO: log or throw exception
      //error, begin parameter is greater than end_
    }
  }

  public void setEnd(Double end) {
    if (end == null)
    {
    	System.err.println("'end' param passed as 'null' in 'DoubleRange' class 'setEnd()' method. Obj="+this.toString());
      return;
    }

    //if begin_ is not initilized or parameter end is >= existing begin_, valid end
    if (begin_ == null || end.compareTo(begin_) >= 0)
      end_ = end;
    else {
      //DoubleODO: log or throw exception
      //error, end parameter is less than begin_
    }
  }

  public Double getBegin() {
    return this.begin_;
  }

  public Double getEnd() {
    return this.end_;
  }

  public boolean isInRange(Double val) {
    return this.isInRange(val, val);
  }

  public boolean isInRange(Double begin, Double end) {
    return this.isInRange(new DoubleRange(begin, end));
  }

  public boolean isInRange(DoubleRange val) {
    if (this.begin_.compareTo(val.getEnd()) <= 0
	&& this.end_.compareTo(val.getBegin()) >= 0)
      return true;
    return false;
  }

  public boolean isBefore(Double val) {
    return this.isBefore(val, val);
  }

  public boolean isBefore(Double begin, Double end) {
    return this.isBefore(new DoubleRange(begin, end));
  }

  public boolean isBefore(DoubleRange val) {
    if (this.end_.compareTo(val.getBegin()) <= 0)
      return true;
    return false;
  }

  public boolean isAfter(Double val) {
    return this.isAfter(val, val);
  }

  public boolean isAfter(Double begin, Double end) {
    return this.isAfter(new DoubleRange(begin, end));
  }

  public boolean isAfter(DoubleRange val) {
    if (this.begin_.compareTo(val.getEnd()) >= 0)
      return true;
    return false;
  }

  public boolean startsBefore(Double val) {
    return this.startsBefore(val, val);
  }

  public boolean startsBefore(Double begin, Double end) {
    return this.startsBefore(new DoubleRange(begin, end));
  }

  public boolean startsBefore(DoubleRange val) {
    if (this.begin_.compareTo(val.getBegin()) <= 0)
      return true;
    return false;
  }

  public boolean endsAfter(Double val) {
    return this.endsAfter(val, val);
  }

  public boolean endsAfter(Double begin, Double end) {
    return this.endsAfter(new DoubleRange(begin, end));
  }

  public boolean endsAfter(DoubleRange val) {
    if (this.end_.compareTo(val.getEnd()) >= 0)
      return true;
    return false;
  }
}

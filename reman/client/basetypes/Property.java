package reman.client.basetypes;

import java.util.ArrayList;

import reman.common.database.DatabaseObject;

public class Property extends DatabaseObject {
	private PropertyType prop_type_;
	private Location location_;
	private ArrayList<OwnerEntry> owners_;
	private ArrayList<Appraisal> appraisals_;

	public Property() {
		this.prop_type_ = PropertyType.RES;
		this.location_ = new Location();
		this.owners_ = new ArrayList<OwnerEntry>();
		this.appraisals_ = new ArrayList<Appraisal>();
	}

	public Location getLocation() {
		return location_;
	}

	public ArrayList<OwnerEntry> getOwners() {
		return owners_;
	}

	public void setLocation(Location l) {
		location_ = l;
	}

	public void setPropertyType(PropertyType p) {
		this.prop_type_ = p;
	}

	public PropertyType getPropertyType() {
		return this.prop_type_;
	}
}

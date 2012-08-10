package reman.client.basetypes;

import java.util.ArrayList;

import reman.client.gui.forms.BusinessEntityFormPanel;
import reman.client.gui.forms.DboFormPanel;
import reman.common.database.DatabaseObject;

public abstract class BusinessEntity extends DatabaseObject {
	private ArrayList<ContactEntity> contacts_;
	private ArrayList<Property> liable_properties_;

	public BusinessEntity() {
		liable_properties_ = new ArrayList<Property>();
		contacts_ = new ArrayList<ContactEntity>();
	}

	public ArrayList<ContactEntity> getContacts() {
		return contacts_;
	}

	public ArrayList<Property> getLiableProperties() {
		return liable_properties_;
	}

	public void addContact(ContactEntity t) {
		contacts_.add(t);
	}

	public void removeContact(ContactEntity t) {
		contacts_.remove(t);
	}

	public void addLiableProperty(Property p) {
		liable_properties_.add(p);
	}

	public void removeLiableProperty(Property p) {
		liable_properties_.remove(p);
	}
}

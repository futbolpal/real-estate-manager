package reman.client.basetypes;

import java.sql.SQLException;
import java.util.ArrayList;

import reman.client.app.office_maintenance.Task;
import reman.common.database.exceptions.DatabaseException;

public class RealEstateOffice extends Office {
	private Agent broker_of_record_;
	private ArrayList<Person> person_;

	public RealEstateOffice() {
		this.broker_of_record_ = new Agent();
		this.person_ = new ArrayList<Person>();
	}

	/**
	 * 
	 * @param a
	 * @return True if this Agent <code>a</code> is a member of this RealEstateOffice
	 */
	public boolean isMember(Agent a) {
		for (Person agent : this.person_) {
			if (agent.equals(a))
				return true;
		}
		return false;
	}

	public Agent getBrokerOfRecord() {
		return this.broker_of_record_;
	}

	public void setBrokerOfRecord(Agent a) {
		this.broker_of_record_ = a;
	}

	public boolean addMember(Person p) {
		return this.person_.add(p);
	}

	public boolean removeMember(Person p) {
		return this.person_.remove(p);
	}

	public ArrayList<Person> getMembers() {
		return person_;
	}

	public void retrieve() throws DatabaseException, SQLException {
		super.retrieve();
	}
}

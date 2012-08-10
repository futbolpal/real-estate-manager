package reman.client.basetypes;

public class Email extends ContactEntity {
	private String address_;

	public Email() {

	}

	public Email(String a) {
		this.address_ = a;
	}

	public void setAddress(String address) {
		this.address_ = address;
	}

	public String getAddress() {
		return this.address_;
	}

	public String toString() {
		return this.getName() + ", " + this.getAddress();
	}
}

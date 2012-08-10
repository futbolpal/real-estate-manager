package reman.client.basetypes;

public class Phone extends ContactEntity {
	private String number_;

	public Phone() {

	}

	public Phone(String n) {
		number_ = n;
	}

	public String getNumber() {
		return number_;
	}

	public void setNumber(String number) {
		number_ = number;
	}

	public String toString() {
		return this.getName() + ", " + this.getNumber();
	}
}

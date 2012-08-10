package reman.client.basetypes;

public enum Country {
	USA("United States");

	private String name_;

	private Country(String s) {
		name_ = s;
	}

	public String getCountryName() {
		return name_;
	}
}

package reman.client.app.preferences;

public class BooleanUserPreference extends UserPreference {
	private boolean value_;

	public void setValue(boolean v) {
		value_ = v;
	}

	public boolean getValue() {
		return value_;
	}
}

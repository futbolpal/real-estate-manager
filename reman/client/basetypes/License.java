package reman.client.basetypes;

import reman.client.gui.forms.DboFormPanel;
import reman.client.gui.forms.LicenseFormPanel;
import reman.common.database.DatabaseObject;

public class License extends DatabaseObject {
	private String board_;
	private String type_;
	private String number_;
	private State state_;

	public License() {

	}

	public void setBoard(String board) {
		board_ = board;
	}

	public void setType(String type) {
		type_ = type;
	}

	public void setNumber(String number) {
		number_ = number;
	}

	public void setState(State s) {
		state_ = s;
	}

	public String getBoard() {
		return board_;
	}

	public String getType() {
		return type_;
	}

	public String getNumber() {
		return number_;
	}

	public State getState() {
		return state_;
	}
}

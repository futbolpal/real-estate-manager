package reman.common.database.exceptions;

public class LoggedInException extends DatabaseObjectException {
	public LoggedInException() {
		super(null, "You must be logged in to perform this operation");
	}
}

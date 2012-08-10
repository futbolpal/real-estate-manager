package reman.common.database.exceptions;

import java.sql.SQLException;

public class DatabaseException extends Exception {
	public DatabaseException(String message) {
		super(message);
	}
}

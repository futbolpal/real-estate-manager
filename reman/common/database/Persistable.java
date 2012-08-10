package reman.common.database;

import java.sql.SQLException;

import reman.common.database.exceptions.DatabaseException;

/**
 * This interface outlines what is required for a persistable object
 * in the database 
 * @author jonathan
 *
 */
public interface Persistable {
	public long commit() throws SQLException, DatabaseException;

	public void retrieve() throws SQLException, DatabaseException;

	public boolean isLocked() throws SQLException, DatabaseException;

	public boolean lock() throws SQLException, DatabaseException;

	public boolean unlock() throws SQLException, DatabaseException;
}

package reman.client.app.search;

/**
 * This exception is thrown when the SearchManager attempts to search a class that is not searchable.
 * @author Will
 *
 */
public class NotSearchableException extends Exception {
	private Class c_;
	public NotSearchableException(Class c) {
		c_ = c;
	}
	
	public String toString() {
		return String.format("Class %s is not searchable. It has not been regisered with the SearchManager.", c_);
	}
}

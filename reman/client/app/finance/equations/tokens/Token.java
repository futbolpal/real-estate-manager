package reman.client.app.finance.equations.tokens;

import reman.common.database.DatabaseObject;

/**
 * Provides the base class for a token used in the Equation parsing and evaluation.  Also provides the ability to persist a token
 * to the database by extending the ManagedDatabaseObject. 
 * @author Scott
 *
 */
public abstract class Token extends DatabaseObject{
	
	/**
	 * Enum to identify the run time implementation of this token.
	 */
	protected TokenMarkType mark_;
	
	/**
	 * Used for numbers of bases other than base-10
	 */
	private int position_;
	/**
	 * Used for numbers of bases other than base-10
	 */
	private int length_;
	
	/**
	 * DatabaseObject use
	 */
	protected Token(){
	}
	
	/**
	 * 
	 * @param position Position at which the token was encountered.
	 * @param length Length in characters of this token.
	 */
	public Token(int position, int length){
		this.position_ = position;
		this.length_ = length;
	}
	
	public int getPosition(){return this.position_;}
	public int getLength(){return this.length_;}
	public TokenMarkType getMark(){return this.mark_;}
}

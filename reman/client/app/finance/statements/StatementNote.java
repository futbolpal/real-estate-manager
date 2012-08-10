package reman.client.app.finance.statements;

import reman.common.database.DatabaseObject;

/**
 * When StatementLineItem objects appear on Statements that require clarification (judgment calls, or switching methods) A StatementNote is required.
 * @author Scott
 *
 */
public class StatementNote extends DatabaseObject{

	private StatementLineItem owner_;
	
	/**
	 * DatabaseObject use only
	 */
	private StatementNote(){
	}
	
	/**
	 * @param owner This is the line item which contains this note
	 * @param description
	 */
	public StatementNote(StatementLineItem owner, String description){
		super.description_ = description;
		this.owner_ = owner;
	}
	
	/**
	 * Obtain the StatementLineItem which this note pretains to.
	 * @return
	 */
	public StatementLineItem getOwner(){
		return this.owner_;
	}
}

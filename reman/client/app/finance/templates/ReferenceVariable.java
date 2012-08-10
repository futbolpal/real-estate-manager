package reman.client.app.finance.templates;

import reman.client.app.finance.equations.tokens.Variable;
import reman.common.database.DatabaseObject;

/**
 * This class will maintain a count of how many Equation objects reference the contained Variable.
 * This is to allow the Template and TemplateContribution to maintain a GlobalVariableSystem collection.
 * @author Scott
 *
 */
public class ReferenceVariable extends DatabaseObject {

	/**
	 * This is the number of equations which reference this variable
	 */
	private int num_references_;

	private Variable var_;

	public boolean equals(Object o) {
		if (o instanceof ReferenceVariable) {
			ReferenceVariable rv = (ReferenceVariable) o;
			if (rv.num_references_ == this.num_references_ && this.var_ == rv.getVariable())
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param var The Variable this ReferenceVariable will keep a reference count for.
	 */
	public ReferenceVariable(Variable var) {
		this.var_ = var;
		this.num_references_ = 1;
	}

	/**
	 * Increment the number of references made to the Variable object contained in this.
	 * @return
	 */
	public int incReferences() {
		return ++this.num_references_;
	}

	/**
	 * Decrement the number of references made to the Variable object contained in this.
	 * The number of references can not be negative.
	 * @return
	 */
	public int decReferences() {
		if (--this.num_references_ < 0)
			this.num_references_ = 0;
		return this.num_references_;
	}

	/**
	 * Obtain the number of references made to Variable object contained in this.
	 * @return
	 */
	public int getReferences() {
		return this.num_references_;
	}

	/**
	 * If this variable is no longer referenced
	 * @return
	 */
	public boolean isDeReferenced() {
		return this.num_references_ <= 0;
	}

	/**
	 * Obtain a reference to the Variable object contained in this.
	 * @return
	 */
	public Variable getVariable() {
		return this.var_;
	}

}

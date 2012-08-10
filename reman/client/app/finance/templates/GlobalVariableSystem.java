package reman.client.app.finance.templates;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

import reman.client.app.finance.equations.tokens.Variable;
import reman.common.database.DatabaseObject;

/**
 * A Template can contains many underlying TemplateContribution objects which may contain many TemplateLineItem which contains an Equation
 * and that Equation may contain many variables.  This class will ease the task of regulating the assignment of variables globally throughout
 * the entire template.  This class provides a global variable system at the level of TemplateContribution as well as the level of the Template
 * and these levels communicate throughout the tree structure of TemplateContributions in such a way to keep each global variable system consistent
 * with each child and parent TemplateContribution, and ultimately the Template.
 * @author Scott
 *
 */
public class GlobalVariableSystem extends DatabaseObject {
	/**
	 * Map of all variables contained in this template, each variable is only contained once in the map
	 */
	private Hashtable<String, ReferenceVariable> global_variables_;

	/**
	 * Construct an initially empty global variable system.
	 */
	public GlobalVariableSystem() {
		this.global_variables_ = new Hashtable<String, ReferenceVariable>();
	}

	/**
	 * Obtain a map (keyed by variable name) of all Variable objects contained within this GlobalVariableSystem.
	 * @return
	 */
	public Hashtable<String, Variable> getGlobalVars() {
		Hashtable<String, Variable> glob_vars = new Hashtable<String, Variable>(this.global_variables_
				.size());
		Enumeration<String> this_keys = this.global_variables_.keys();
		while (this_keys.hasMoreElements()) {
			String curr_key = this_keys.nextElement();
			glob_vars.put(curr_key, this.global_variables_.get(curr_key).getVariable());
		}
		return glob_vars;
	}

	/**
	 * Obtain the corresponding Variable to <code>variable_name</code>.
	 * @param variable_name The name of a Variable object know about by this GlobalVariableSystem.
	 * @return Corresponding Variable to <code>variable_name</code>, or null if not found.
	 */
	public Variable getVariable(String variable_name) {
		ReferenceVariable ref_var = this.global_variables_.get(variable_name);
		return (ref_var == null) ? null : ref_var.getVariable();
	}

	/**
	 * Keep the current global variable system up to date on account for potential deviations in each child GlobalVariableSystem. 
	 * @param children_gvs_collect Collection of all GlobalVariableSystem object which are contained in descendant objects in reference
	 * 								To the object which contains this GlobalVariableSystem.
	 */
	void updateParentGlobalVariables(Collection<GlobalVariableSystem> children_gvs_collect) {
		/*check if the parent has any artifact variables which need to be removed*/
		Enumeration<String> this_keys = this.global_variables_.keys();
		while (this_keys.hasMoreElements()) {
			String curr_key = this_keys.nextElement();
			boolean curr_value_seen = false;
			for (GlobalVariableSystem curr_child : children_gvs_collect) {
				if (curr_child.global_variables_.containsKey(curr_key)) {
					curr_value_seen = true;
					break;
				}
			}

			if (!curr_value_seen) {
				this.global_variables_.remove(curr_key);
			}
		}

		/*check if the parent is lacking a variable contained within a child*/
		for (GlobalVariableSystem curr_child : children_gvs_collect) {
			Enumeration<String> curr_child_keys = curr_child.global_variables_.keys();
			while (curr_child_keys.hasMoreElements()) {
				String curr_child_key = curr_child_keys.nextElement();
				if (!this.global_variables_.contains(curr_child_key)) {
					this.global_variables_.put(curr_child_key, curr_child.global_variables_
							.get(curr_child_key));
				}
			}
		}
	}

	/**
	 * To be used when directly adding or removing an Equation which contains Variables.
	 * @param subject_map Variable objects that are subject to be added/removed
	 * @param remove_variables Whether or not the Variable objects in <code>subject_vars</code> are being added or removed.
	 */
	void updateGlobalVariables(Hashtable<String, Variable> subject_map,
			boolean remove_variables) {
		if (subject_map == null)
			return;
		/*if variables are not being removed, set all matching variables to the instance 
		 * contained within the global list*/
		if (!remove_variables) {
			Enumeration<String> subject_keys = subject_map.keys();
			while (subject_keys.hasMoreElements()) {
				String curr_subject_key = subject_keys.nextElement();
				if (this.global_variables_.containsKey(curr_subject_key)) {
					/*increase reference count, point the variable in the subject map at the variable in the global map*/
					ReferenceVariable ref_var = this.global_variables_.get(curr_subject_key);
					ref_var.incReferences();
					subject_map.put(curr_subject_key, ref_var.getVariable());
				} else {
					/*this variable is unique (for now), add this variable to global map*/
					ReferenceVariable ref_var = new ReferenceVariable(subject_map.get(curr_subject_key));
					this.global_variables_.put(curr_subject_key, ref_var);
				}
			}
		}
		/*if variables are being removed, only remove unique variables from the global list*/
		else {
			Enumeration<String> subject_keys = subject_map.keys();
			while (subject_keys.hasMoreElements()) {
				String curr_subject_key = subject_keys.nextElement();
				ReferenceVariable ref_var = this.global_variables_.get(curr_subject_key);
				if (ref_var.decReferences() <= 0) {
					this.global_variables_.remove(curr_subject_key);
				}
			}
		}
	}
}

package reman.client.app.finance.equations.events;

import reman.client.app.finance.equations.Equation;
import reman.client.app.finance.events.ChangeState;

/**
 * This event is raised before, and after an equation is changed.
 * @author Scott
 *
 */
public class EquationChangeEvent extends EquationEvent{
	public EquationChangeEvent(Equation eq, ChangeState state){
		super(eq, state);
	}
}

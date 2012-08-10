package reman.client.app.finance.equations.events;

import reman.client.app.finance.equations.Equation;
import reman.client.app.finance.events.ChangeState;

/**
 * This event is fired before, and after an equation is evaluated.
 * @author Scott
 *
 */
public class EquationEvaluatedEvent extends EquationEvent{
	public EquationEvaluatedEvent(Equation eq, ChangeState state){
		super(eq,state);
	}
}

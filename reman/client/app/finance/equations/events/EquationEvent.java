package reman.client.app.finance.equations.events;

import reman.client.app.finance.equations.Equation;
import reman.client.app.finance.events.ChangeEvent;
import reman.client.app.finance.events.ChangeState;

/**
 * This event class provides a base class for all equation events.
 * @author Scott
 *
 */
public class EquationEvent extends ChangeEvent {

	public EquationEvent(Equation eq, ChangeState state) {
		super(eq, state);
	}

	public Equation getEquation() {
		return (Equation) this.getSource();
	}

}

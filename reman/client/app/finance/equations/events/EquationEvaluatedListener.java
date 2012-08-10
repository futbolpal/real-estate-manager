package reman.client.app.finance.equations.events;

import java.util.EventListener;

/**
 * This is the interface for classes who wish to subscribe to EquationEvaluatedEvent object notifications must adhere to.
 * @author Scott
 *
 */
public interface EquationEvaluatedListener extends EventListener{
	public void equationEvaluated(EquationEvaluatedEvent e);
}

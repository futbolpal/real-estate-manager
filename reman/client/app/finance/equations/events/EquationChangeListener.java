package reman.client.app.finance.equations.events;

import java.util.EventListener;

/**
 * This is an interface which classes who subscribe to EquationChangeEvent objects must adhere to.
 * @author Scott
 *
 */
public interface EquationChangeListener extends EventListener{
	public void equationChange(EquationChangeEvent e);
}

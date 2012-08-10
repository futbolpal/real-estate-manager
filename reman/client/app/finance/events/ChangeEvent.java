package reman.client.app.finance.events;

import java.util.EventObject;

/**
 * Provides a base event type which is intended to fire before and after the event occurs.
 * @author Scott
 *
 */
public class ChangeEvent extends EventObject {
	protected ChangeState state_;

	public ChangeEvent(Object obj, ChangeState state) {
		super(obj);
		this.state_ = state;
	}

	/**
	 * Whether this event occurred before the corresponding event or after.
	 * @return
	 */
	public ChangeState getState() {
		return this.state_;
	}
}

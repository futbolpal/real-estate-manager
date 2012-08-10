package reman.client.app.finance.templates.events;

import reman.client.app.finance.events.ChangeEvent;
import reman.client.app.finance.events.ChangeState;
import reman.client.app.finance.templates.TemplateLineItem;

/**
 * This will provide event notification of a change to a TemplateLineItem.
 * <br/>This may be used to notify that an Equation change occurred in a TemplateLineItem.
 * @author Scott
 *
 */
public class TemplateLineItemEvent extends ChangeEvent {
	public TemplateLineItemEvent(TemplateLineItem tli, ChangeState state) {
		super(tli, state);
	}

	public TemplateLineItem getTemplateLineItem() {
		return (TemplateLineItem) this.getSource();
	}
}

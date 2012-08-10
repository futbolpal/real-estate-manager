package reman.client.app.finance.templates.events;

import java.util.EventListener;

/**
 * This interface provides an interface for which TemplateLineItemEvent listeners must adhere to.
 * @author Scott
 *
 */
public interface TemplateLineItemListner extends EventListener{
	public void eventOccurred(TemplateLineItemEvent e);
}

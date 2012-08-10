package reman.client.app.finance.timer;

import java.util.EventListener;

public interface TimeListener extends EventListener{
	public void timeEventOccurred(TimeEvent e);
}

package reman.client.app.listeners;

import reman.common.messaging.DboChangedMessage;

public interface DboChangedListener extends FrameworkListener {
  public void dboChangedEvent(DboChangedMessage m);
}

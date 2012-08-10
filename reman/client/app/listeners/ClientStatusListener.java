package reman.client.app.listeners;

import reman.common.messaging.ClientStatusMessage;

public interface ClientStatusListener extends FrameworkListener {
  public void clientStatusChanged(ClientStatusMessage m);
}

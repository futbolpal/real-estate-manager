package reman.client.app.listeners;

import reman.common.messaging.ChatMessage;

public interface ChatMessageListener extends FrameworkListener {
	public void chatMessageReceived(ChatMessage m);
}

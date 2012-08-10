package reman.client.app.chatter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import reman.common.messaging.ChatMessage;

public class ChatListener extends Thread {
	private Socket client_;

	public ChatListener(Socket c) {
		client_ = c;
	}

	public void run() {
		try {
			ObjectInputStream in = new ObjectInputStream(client_.getInputStream());
			while (true) {
				Object o = in.readObject();
				if (o instanceof ChatMessage) {
					/* handle message */
				}
			}
		} catch (IOException e) {
			/* User is offline */
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}

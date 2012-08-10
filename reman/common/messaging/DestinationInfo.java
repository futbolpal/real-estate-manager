package reman.common.messaging;

import javax.jms.Destination;
import javax.jms.MessageProducer;

public class DestinationInfo {
	private MessageProducer sender_;
	private Destination dest_;

	public DestinationInfo(MessageProducer s, Destination d) {
		sender_ = s;
		dest_ = d;
	}

	public MessageProducer getSender() {
		return sender_;
	}

	public Destination getDestination() {
		return dest_;
	}
}

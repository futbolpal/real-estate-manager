package reman.common.messaging;

import java.io.Serializable;

public class ChatMessage implements Serializable {
  private long sender_;
  private String message_;

  public ChatMessage(long from, String message) {
    sender_ = from;
    message_ = message;
  }

  public Long getSender() {
    return sender_;
  }

  public String getMessage() {
    return message_;
  }
}

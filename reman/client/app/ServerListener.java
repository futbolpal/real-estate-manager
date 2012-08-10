package reman.client.app;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import reman.common.messaging.NotificationMessage;

/**
 * This listens for messages from the server and 
 * passes them on to the framework
 * @author jonathan
 */
public class ServerListener extends Thread {
  private Socket server_sock_;

  public ServerListener(Socket s) {
    this.server_sock_ = s;
  }

  public void run() {
    try {
      ObjectInputStream in = new ObjectInputStream(server_sock_
	  .getInputStream());
      while (true) {
	Object o = in.readObject();
	System.out.println("received message");
	if (o instanceof NotificationMessage) {
	  Framework.instance().distributeMessage((NotificationMessage) o);
	}
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}

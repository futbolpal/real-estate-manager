package reman.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import reman.common.ClientStatusMessage;
import reman.common.NotificationMessage;

/**
 * This listens for messages from the clients
 * and distributes them to the other clients
 * @author jonathan
 */
public class ClientListener extends Thread {
  private Socket client_sock_;
  private Long client_uid_;
  private Long project_id_;
  private ObjectOutputStream out_;

  public ClientListener(Socket c) throws IOException {
    this.client_sock_ = c;
    this.out_ = new ObjectOutputStream(c.getOutputStream());
  }

  public void run() {
    try {
      ObjectInputStream in = new ObjectInputStream(client_sock_
	  .getInputStream());
      while (true) {
	Object o = in.readObject();

	/* We need to save the uid so we know who disconnects later (see exception)*/
	if (o instanceof ClientStatusMessage) {
	  ClientStatusMessage m = (ClientStatusMessage) o;
	  this.client_uid_ = m.getSourceUserID();
	  this.project_id_ = m.getProjectID();
	}

	/* Handle messages */
	if (o instanceof NotificationMessage) {
	  Server.sendMessage(client_sock_, (NotificationMessage) o);
	}
      }
    } catch (IOException e) {
      /* Client disconnected */
      Server.removeClient(this);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public Socket getClientSocket() {
    return client_sock_;
  }

  public long getClientID() {
    return client_uid_;
  }

  public long getProjectID() {
    return project_id_;
  }

  public ObjectOutputStream getObjectOutputStream() {
    return out_;
  }
}

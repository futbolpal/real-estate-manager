package reman.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import reman.common.ClientStatusMessage;
import reman.common.ClientStatusMessage.UserStatus;
import reman.common.NotificationMessage;
import reman.common.ProjectNotificationMessage;
import reman.common.database.DatabaseManager;
import reman.common.database.UserManager;

public class Server {

  private static final int START_PORT = 1024;

  private static ArrayList<ClientListener> reman_clients_;

  private static Connection conn_ = reman.common.SQLServer.JONATHAN().connect();

  public static void removeClient(ClientListener l) {
    /* Update sql */
    try {
      String sql = "UPDATE " + DatabaseManager.getTableName(UserManager.class)
	  + " SET status_='" + UserStatus.LOGGED_OUT.name() + "' WHERE ID="
	  + l.getClientID();
      Statement stmt = conn_.createStatement();
      stmt.executeUpdate(sql);
    } catch (SQLException e1) {
      e1.printStackTrace();
    }

    /* Close socket */
    try {
      l.getClientSocket().close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    /* Remove client from list */
    reman_clients_.remove(l);

    /* Notify others */
    ClientStatusMessage csm = new ClientStatusMessage(l.getClientID(), l
	.getProjectID(), UserStatus.LOGGED_OUT);
    Server.sendMessage(l.getClientSocket(), csm);
  }

  /**
   * Distribute a message to the other clients
   * @param from - null if server generated
   * @param m
   */
  public static void sendMessage(final Socket from, final NotificationMessage m) {
    new Thread(new Runnable() {
      public void run() {
	synchronized (reman_clients_) {
	  for (ClientListener l : reman_clients_) {
	    /* Don't send message to event generator */
	    if (from == l.getClientSocket())
	      continue;

	    /* Only send message to same project */
	    if (m instanceof ProjectNotificationMessage) {
	      ProjectNotificationMessage pm = (ProjectNotificationMessage) m;
	      if (pm.getProjectID() != l.getProjectID()) {
		continue;
	      }
	    }

	    if (l.getClientSocket().isConnected()) {
	      try {
		l.getObjectOutputStream().writeObject(m);
	      } catch (IOException err) {
		err.printStackTrace();
	      }
	    }
	  }
	}
      }
    }).start();
  }

  private static ServerSocket bind() {
    ServerSocket serverSocket = null;
    boolean bound = false;
    for (int port = START_PORT; !bound; port++) {
      try {
	port++;
	serverSocket = new ServerSocket(port);
	System.out.println("Bound to port: " + port);
	bound = true;
      } catch (IOException e) {
	System.err.println("Could not listen on port: " + port);
	bound = false;
      }
    }
    return serverSocket;
  }

  public static void main(String[] args) throws IOException {
    System.out.println("Server started...");
    reman_clients_ = new ArrayList<ClientListener>();
    try {
      ServerSocket s_sock = bind();
      while (true) {
	System.out.println("Waiting for connections...");
	Socket c_sock = s_sock.accept();

	/* Spawn a thread to listen*/
	ClientListener cl = new ClientListener(c_sock);
	cl.start();
	reman_clients_.add(cl);

	System.out.println("Connection established.");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}

package reman.client.app.chatter;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import reman.common.database.UserManager;
import reman.common.messaging.ChatMessage;

public class ChatServer extends Thread {
	private static ChatServer this_;
	private static final int START_PORT = 1024;

	private Hashtable<Long, Socket> sockets_;
	private boolean started_;

	private ChatServer() {
		started_ = false;
		sockets_ = new Hashtable<Long, Socket>();//uid,socket
	}

	private ServerSocket bind() {
		ServerSocket serverSocket = null;
		boolean bound = false;
		for (int port = START_PORT; !bound; port++) {
			try {
				port++;
				serverSocket = new ServerSocket(port);
				System.out.println("chat bound to port: " + port);
				bound = true;
			} catch (IOException e) {
				System.err.println("Could not chat on port: " + port);
				bound = false;
			}
		}
		return serverSocket;
	}

	public boolean sendMessage(Long uid, ChatMessage m) {
		Socket s = sockets_.get(uid);
		try {
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			out.writeObject(m);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void run() {
		try {
			ServerSocket s_sock = bind();
			UserManager.instance().setChatServer(InetAddress.getLocalHost(), s_sock.getLocalPort());
			while (true) {
				System.out.println("Waiting for connections...");
				Socket c_sock = s_sock.accept();

				/* Spawn a thread to listen*/
				new ChatListener(c_sock).start();

				System.out.println("Connection established.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ChatServer instance() {
		if (this_ == null)
			this_ = new ChatServer();
		return this_;
	}

}

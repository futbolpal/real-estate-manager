package test_files;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class TestClient implements Runnable {
  public TestClient() {

  }

  public void run() {
    try {
      Socket c = new Socket("localhost", 1025);
      InputStream i = c.getInputStream();
      while (true) {
        System.out.println(i.read());
      }
    } catch (UnknownHostException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    new TestClient();
  }
}

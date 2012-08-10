package test_files;

import java.security.*;

public class TestMD5 {
  public static void main(String[] args) {

    byte[] defaultBytes = new String("test").getBytes();
    try {
      MessageDigest algorithm = MessageDigest.getInstance("MD5");
      algorithm.reset();
      algorithm.update(defaultBytes);
      byte messageDigest[] = algorithm.digest();

      StringBuffer hexString = new StringBuffer();
      for (int i = 0; i < messageDigest.length; i++) {
	hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
      }
      String foo = messageDigest.toString();
      System.out.println(hexString);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

  }
}

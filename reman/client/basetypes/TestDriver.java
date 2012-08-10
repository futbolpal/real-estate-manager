package reman.client.basetypes;

import java.sql.SQLException;
import java.util.Arrays;

public class TestDriver {
  public static void main(String[] args) throws IllegalArgumentException,
      SQLException, IllegalAccessException, ClassNotFoundException,
      InstantiationException, SecurityException, NoSuchFieldException {
    /*
     ContactBook b = new ContactBook();
     b.addCard(new ContactCard(6033451166L,"jonathan.lewis@uconn.edu"));
     b.addCard(new ContactCard(6033454058L,"rblewis10@aol.com"));
     b.addCard(new ContactCard(6033450058L,"smlewis@aol.com"));
     b.addCard(new ContactCard(6038808535L,"<none>"));
     
     b.addHashCard("scott", new ContactCard(8609664705L, "scott.mitchell@uconn.edu"));
     b.addHashCard("jon", new ContactCard(6033451166L,"jonathan.lewis@uconn.edu"));
     b.commit();
     */
    System.out.println(Arrays.toString(Phone.class.getDeclaredFields()));
  }
}

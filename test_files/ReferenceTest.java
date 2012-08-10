package test_files;

import java.sql.SQLException;

import reman.client.basetypes.Agent;
import reman.common.database.UserManager;
import reman.common.database.exceptions.DatabaseException;

public class ReferenceTest {
	public ReferenceTest() throws SQLException, DatabaseException {
		boolean setup = false;
		if (setup) {
			Agent a = new Agent();
			a.setFirstName("Jonathan");
			a.setLastName("Lewis");

			Agent b = new Agent();
			b.setFirstName("Scott");
			b.setLastName("Mitchell");

			a.addPartner(b);
			b.addPartner(a);

			a.commit();
			b.commit();
			System.out.println(a.getVersionChain() + ":" + b.getVersionChain());
		} else {
			Agent a = new Agent();
			a.setVersionChain("98425d2368f786e8b9d125777db36993");
			Agent b = new Agent();
			b.setVersionChain("30a875b1e9fb956c3bbed3123e3f767");
			a.retrieve();
			b.retrieve();
			System.out.println("Agent A: " + a.getVersionChain());
			System.out.println("Agent B: " + b.getVersionChain());
			System.out.println("Partner of A? " + a.getPartners().get(0).getVersionChain());
			System.out.println("Partner of B? " + b.getPartners().get(0).getVersionChain());
			System.out.println("Partner reference? " + (a.getPartners().get(0) == b));
			System.out.println("Partner reference? " + (b.getPartners().get(0) == a));
		}
	}

	public static void main(String... strings) throws SQLException, DatabaseException {
		UserManager.instance().login("Futbolpal", "Jonathan");
		new ReferenceTest();
		UserManager.instance().logout();
	}
}

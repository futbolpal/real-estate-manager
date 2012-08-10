package test_files;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import reman.client.basetypes.Agent;
import reman.common.database.DatabaseObject;
import reman.common.database.UserManager;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.DatabaseObjectException;
import reman.common.database.exceptions.LoggedInException;

public class TestDriveDBO {
	public TestDriveDBO() throws Exception{
		testCollection();
	}

	public void testCollection() throws DatabaseException, SQLException {
		/* Create the test scenario */
		Agent a = new Agent();
		Agent b = new Agent();

		a.setFirstName("Jonathan");
		b.setFirstName("Scott");

		a.addPartner(b);
		b.commit();
		a.commit();

		Agent c = new Agent();
		c.setID(a.getID());
		c.retrieve();

		Agent d = new Agent();
		d.setID(b.getID());
		d.retrieve();

		System.out.println("Agent " + c.getFirstName() + " has " + c.getPartners().size()
				+ " partners.");
		for (Agent z : c.getPartners()) {
			System.out.println("\t" + z.getFirstName());
		}

	}

	public static void main(String[] args) throws Exception {
		UserManager.instance().login("Futbolpal", "Jonathan");
		new TestDriveDBO();
		UserManager.instance().logout();
	}
}

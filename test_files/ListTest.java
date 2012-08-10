package test_files;

import java.sql.SQLException;

import reman.client.app.finance.FinanceManager;
import reman.client.basetypes.Agent;
import reman.common.database.DatabaseManager;
import reman.common.database.UserManager;
import reman.common.database.exceptions.DatabaseException;

public class ListTest {
	public ListTest() throws Exception {
		testCollection();
	}

	public void testCollection() throws DatabaseException, SQLException {
		/* Create the test scenario */
		Agent a = new Agent();
		a.addCredential("A");
		a.addCredential("B");
		a.addCredential("C");
		a.addCredential("D");
		a.commit();
	}

	public static void main(String[] args) throws Exception {
		UserManager.instance().login("Futbolpal", "Jonathan");
		new ListTest();
		UserManager.instance().logout();
	}
}

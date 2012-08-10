package test_files;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import reman.client.basetypes.Agent;
import reman.common.database.UserManager;
import reman.common.database.exceptions.DatabaseObjectException;
import reman.common.database.exceptions.LockedException;
import reman.common.database.exceptions.LoggedInException;

public class TestDBOLocks {
	public static void main(String[] args) throws Exception {
		/**
		 * The test plan: 1. Log in as scott 2. Lock an object 3. Logout 4.
		 * Login as jonathan 5. Test the lock 6. Since the lock lasts X minutes,
		 * the lock should stand
		 * 
		 */

		UserManager.instance().login("skm05001", "scott");
		System.out.println("UserID: " + UserManager.instance().getCurrentUserID());
		Agent a = new Agent();
		a.setFirstName("Scott");
		a.setLastName("Mitchell");
		a.setMiddleName("K");
		a.commit();
		a.lock();

		UserManager.instance().logout();
		UserManager.instance().login("Futbolpal", "Jonathan");
		System.out.println("UserID: " + UserManager.instance().getCurrentUserID());

		Agent b = new Agent();
		b.setID(a.getID());
		b.setVersionChain(a.getVersionChain());
		b.retrieve();
		System.out.println("Name: " + b.getName());
		System.out.println("Locked? " + b.isLocked());
		System.out.println("Unlock: " + b.unlock());
		System.out.println("Lock: " + b.lock());

		UserManager.instance().logout();
	}
}

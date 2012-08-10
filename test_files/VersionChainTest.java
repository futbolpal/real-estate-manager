package test_files;

import java.sql.SQLException;

import reman.client.basetypes.Agent;
import reman.client.basetypes.Gender;
import reman.common.database.UserManager;
import reman.common.database.exceptions.DatabaseException;

public class VersionChainTest {

	public static void main(String[] args) throws IllegalArgumentException, SQLException,
			IllegalAccessException, ClassNotFoundException, SecurityException, NoSuchFieldException,
			InstantiationException, DatabaseException {
		System.out.println(UserManager.instance().login("Futbolpal", "Jonathan"));
		boolean write = true;
		if (write) {
			Agent p = new Agent();
			p.setDescription("partner agent");
			p.setFirstName("scott");
			p.setLastName("k");
			p.setMiddleName("mitchell");
			p.setGender(Gender.MALE);
			p.addCredential("Credential P");
			p.commit();
			p.lock();
			p.setFirstName("mike");
			p.commit();
			p.unlock();
			System.out.println(p.getID()+":"+p.getVersionChain());
		} else {
			Agent a = new Agent();
			a.setID(21);
			a.retrieve();
			System.out.println("'" + a.getDescription() + "'");
			System.out.println("'" + a.getFirstName() + "'");
			System.out.println("'" + a.getLastName() + "'");
			System.out.println("'" + a.getMiddleName() + "'");
			System.out.println("'" + a.getGender() + "'");
			a.printCredentials();
			System.out.println("Number of partners: '" + a.getNumberOfPartners() + "'");
			//a.removeCredentials("Credential B");
			//System.out.println(a.commit(false));
		}
		UserManager.instance().logout();
	}
}

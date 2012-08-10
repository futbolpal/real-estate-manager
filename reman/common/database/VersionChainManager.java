package reman.common.database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;

import reman.common.database.exceptions.LoggedInException;

public class VersionChainManager extends DatabaseObject {
	private static VersionChainManager this_;

	/* Table Fields */
	private String chain_id_;

	public VersionChainManager() {
	}

	public String getUniqueVersionChain() throws SQLException, LoggedInException {
		String chain = null;
		int tries = 0;
		while (tries < 5) {
			try {
				MessageDigest algorithm = MessageDigest.getInstance("MD5");
				algorithm.reset();
				algorithm.update(this.getClass().getName().getBytes());
				algorithm.update(String.valueOf(System.currentTimeMillis()).getBytes());
				byte messageDigest[] = algorithm.digest();

				StringBuffer hexString = new StringBuffer();
				for (int i = 0; i < messageDigest.length; i++) {
					hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
				}
				chain = hexString.toString();

				String uniqueness_sql = "SELECT * FROM " + this.getTableName() + " WHERE chain_id_='"
						+ chain + "'";
				ResultSet rs = DatabaseManager.executeQuery(uniqueness_sql);
				if (DatabaseManager.getRowCount(rs) == 0) {
					String new_chain = "INSERT INTO " + this.getTableName() + "( chain_id_) values ('"
							+ chain + "')";
					DatabaseManager.executeUpdate(new_chain);
					return chain;
				}
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return null;
			}
		}
		return chain;
	}

	@Override
	public void retrieve() {
		System.err.println("Cannot retreive this from the database");
	}

	@Override
	public boolean lock() {
		System.err.println("Cannot lock this object");
		return false;
	}

	@Override
	public boolean isLocked() {
		System.err.println("Cannot lock this object");
		return false;
	}

	@Override
	public boolean unlock() {
		System.err.println("Cannot lock this object");
		return false;
	}

	public boolean isSingleton() {
		return true;
	}

	public static VersionChainManager instance() {
		if (this_ == null)
			this_ = new VersionChainManager();
		return this_;
	}

}

package reman.common;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.io.*;

public class SQLServer implements Serializable {
	private String host_, db_, username_, password_, nickname_;

	SQLServer(String host, String db, String username, String pass, String nickname) {
		host_ = host;
		db_ = db;
		username_ = username;
		password_ = pass;
		nickname_ = nickname;
	}

	public static SQLServer SCOTT() {
		return new SQLServer("scottmitchell.no-ip.org", "remax", "root", "xybnnz10", "SCOTT");
	}

	public static SQLServer JONATHAN() {
		return new SQLServer("localhost", "remax", "root", "Futbolpal@87", "JONATHAN");
	}
	
	public static SQLServer WILL() {
		return new SQLServer("137.99.178.115", "remax_will", "root", "metal^4967", "WILL");
	}
	
	public static SQLServer WILL_SCOTT() {
		return new SQLServer("137.99.178.115", "remax_scott", "root", "metal^4967", "WILL_SCOTT");
	}
	
	public static SQLServer WILL_JON() {
		return new SQLServer("137.99.178.115", "remax_jon", "root", "metal^4967", "WILL_JON");
	}

	public String getHostname() {
		return host_;
	}

	public String getDatabase() {
		return db_;
	}

	public String getUsername() {
		return username_;
	}

	public String getPassword() {
		return password_;
	}

	public String getConnectionURL() {
		return "jdbc:mysql://" + host_ + "/" + db_;
	}

	public String toString() {
		return nickname_;
	}

	public Connection connect() {
		try { // Load the JDBC driver
			Connection conn;
			String driverName = "com.mysql.jdbc.Driver";
			Class.forName(driverName);

			System.out.println(this);
			// Create a connection to the database
			conn = DriverManager.getConnection(this.getConnectionURL(), this.getUsername(), this
					.getPassword());

			return conn;
		} catch (ClassNotFoundException e) {
			System.err.println("Could not find driver class, the following are available:");
			List drivers = Collections.list(DriverManager.getDrivers());
			for (int i = 0; i < drivers.size(); i++) {
				Driver driver = (Driver) drivers.get(i);

				// Get name of driver
				String name = driver.getClass().getName();

				// Get version info
				int majorVersion = driver.getMajorVersion();
				int minorVersion = driver.getMinorVersion();
				boolean isJdbcCompliant = driver.jdbcCompliant();
				System.err.println(name + " version " + majorVersion + "." + minorVersion);
			}
			return null;
		} catch (SQLException e) {
			System.err.println("Connection failed.");
			e.printStackTrace();
			return null;
		}

	}
}

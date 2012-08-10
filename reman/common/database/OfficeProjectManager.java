package reman.common.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import reman.client.app.OfficeProject;
import reman.common.database.exceptions.DatabaseException;

/**
 * This class manages the project.  Each user is associated
 * with a project.  To register with a project, they must supply
 * a project access code.
 * @author jonathan
 *
 */
public class OfficeProjectManager {
	private static OfficeProjectManager this_;

	private OfficeProject current_project_;

	private transient boolean new_;

	private OfficeProjectManager() {
		current_project_ = new OfficeProject();
		this.new_ = false;
	}

	public OfficeProject getCurrentProject() {
		return current_project_;
	}

	public boolean loadProject(String pid) throws SQLException, DatabaseException {
		/*Fetch access code */
		current_project_.setVersionChain(pid);
		current_project_.retrieve();
		return true;
	}

	public boolean isNew() {
		return new_;
	}

	public String checkProjectCode(String pcode) throws SQLException {
		String dbo_table = DatabaseManager.getTableName(DatabaseObject.class);
		String office_table = DatabaseManager.getTableName(OfficeProject.class);

		String check_pcode = "SELECT " + dbo_table + ".version_chain_ FROM " + dbo_table + ","
				+ office_table + " WHERE " + office_table + ".access_code_='" + pcode + "' and "
				+ office_table + ".id = " + dbo_table + ".id";
		ResultSet ps = DatabaseManager.executeQuery(check_pcode);
		if (DatabaseManager.getRowCount(ps) != 1)
			return null;
		return ps.getString("version_chain_");
	}

	public void registerProject(String pcode) throws SQLException {
		this.current_project_ = new OfficeProject(pcode);
		this.new_ = true;
	}

	public static OfficeProjectManager instance() {
		if (this_ == null) {
			this_ = new OfficeProjectManager();
		}
		return this_;
	}
}

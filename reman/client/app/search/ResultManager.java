package reman.client.app.search;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import reman.client.app.finance.accounts.Account;
import reman.client.app.finance.invoice.Invoice;
import reman.client.app.finance.ledger.Ledger;
import reman.client.app.finance.statements.Statement;
import reman.client.app.office_maintenance.Meeting;
import reman.client.app.office_maintenance.Note;
import reman.client.app.office_maintenance.Task;
import reman.client.basetypes.Agent;

/**
 * The ResultManager contains static methods to format the database output that is returned
 * from the the search queries. Each class that the SearchManager will search must have a static
 * method in ResultManager with the name: format[class name].
 * The purpose of ResultManager is to format the output of various objects in the system, such as
 * a person, property, or a memo into a single GUI table that is presented to the end user.<br><br>
 * Each method in this class will return a list of SearchResult objects, which holds the data that
 * is displayed to the end user. More methods will be added to this class as development continues.
 * @author Will
 *
 */
public class ResultManager {

	public static ArrayList<SearchResult> formatMeeting(ResultSet rs) {
		return formatDbo(Meeting.class, "Meeting", rs);
	}
	
	public static ArrayList<SearchResult> formatNote(ResultSet rs) {
		ArrayList<SearchResult> r = new ArrayList<SearchResult>();
		try {
			//this method only gets called when rs actually has rows
			rs.first();
			do {
				Long id = rs.getLong("id");
				Long pid = rs.getLong("pid");
				String display_text = rs.getString("note_");
				String modified_by = rs.getString("display_name_");
				Timestamp modified_on = rs.getTimestamp("modified_last_");
				String version_chain = rs.getString("version_chain_");
				r.add(new SearchResult(Note.class, "Note", id, pid, display_text, modified_on, modified_by,
						version_chain));
			} while (rs.next());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return r;
	}
	
	public static ArrayList<SearchResult> formatTask(ResultSet rs) {
		return formatDbo(Task.class, "Task", rs);
	}
	
	public static ArrayList<SearchResult> formatAccount(ResultSet rs) {
		return formatDbo(Account.class, "Account", rs);
	}
	
	public static ArrayList<SearchResult> formatLedger(ResultSet rs) {
		return formatDbo(Ledger.class, "Ledger", rs);
	}
	
	public static ArrayList<SearchResult> formatStatement(ResultSet rs) {
		return formatDbo(Statement.class, "Statement", rs);
	}
	
	public static ArrayList<SearchResult> formatDbo(Class c, String friendly_name, ResultSet rs) {
		return formatDbo(c, friendly_name, rs, true);
	}
	
	public static ArrayList<SearchResult> formatDbo(Class c, String friendly_name, ResultSet rs, boolean useName) {
		ArrayList<SearchResult> r = new ArrayList<SearchResult>();
		try {
			//this method only gets called when rs actually has rows
			rs.first();
			do {
				Long id = rs.getLong("id");
				Long pid = rs.getLong("pid");
				String display_text;
				if (useName)
					display_text = rs.getString("name_");
				else
					display_text = rs.getString("description_");
				
				String modified_by = rs.getString("display_name_");
				Timestamp modified_on = rs.getTimestamp("modified_last_");
				String version_chain = rs.getString("version_chain_");
				r.add(new SearchResult(c, friendly_name, id, pid, display_text, modified_on, modified_by,
						version_chain));
			} while (rs.next());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return r;
	}
	
	public static ArrayList<SearchResult> formatAgent(ResultSet rs) {
		ArrayList<SearchResult> r = new ArrayList<SearchResult>();
		try {
			//this method only gets called when rs actually has rows
			rs.first();
			do {
				Long id = rs.getLong("id");
				Long pid = rs.getLong("pid");
				String display_text = rs.getString("first_") + " " + rs.getString("last_");
				String modified_by = rs.getString("display_name_");
				Timestamp modified_on = rs.getTimestamp("modified_last_");
				String version_chain = rs.getString("version_chain_");
				r.add(new SearchResult(Agent.class, "Agent", id, pid, display_text, modified_on, modified_by,
						version_chain));
			} while (rs.next());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return r;
	}
	
	public static ArrayList<SearchResult> formatInvoice(ResultSet rs) {
		return formatDbo(Invoice.class, "Invoice", rs, false);
	}
}

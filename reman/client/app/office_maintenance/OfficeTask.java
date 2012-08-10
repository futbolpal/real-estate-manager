package reman.client.app.office_maintenance;

public class OfficeTask extends Task {
	private long assigned_to_;

	public OfficeTask() {

	}

	public void assignTask(long user_id) {
		assigned_to_ = user_id;
	}

	public long getAssignee() {
		return assigned_to_;
	}
}

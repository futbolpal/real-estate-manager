package reman.client.gui.schedule;

import reman.client.app.office_maintenance.Task;
import reman.client.gui.forms.DboFormPanel;

public class TaskDescriptionPanel extends DboFormPanel<Task> {
  public TaskDescriptionPanel(Task t) throws Exception {
    super("Task Description", t, null, false);
  }

  @Override
  protected void retrieveForm() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void updateForm() {
    // TODO Auto-generated method stub

  }
}

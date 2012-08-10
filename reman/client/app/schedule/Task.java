package reman.client.app.schedule;

import java.sql.Timestamp;

import reman.client.basetypes.UserDatabaseObject;
import reman.client.gui.forms.DboFormPanel;
import reman.client.gui.forms.TaskFormPanel;
import reman.common.database.DatabaseObject;

public class Task extends UserDatabaseObject {
  public static enum TaskPriority {
    VERY_HIGH, HIGH, MEDIUM, LOW, VERY_LOW;
  }

  private TaskPriority priority_;
  private int percent_complete_;
  private Timestamp due_;

  public Task() {
    this("");
  }

  public Task(String name) {
    this.setName(name);
    percent_complete_ = 0;
    priority_ = TaskPriority.VERY_LOW;
  }

  public void setDueDate(Timestamp d) {
    due_ = d;
  }

  public Timestamp getDueDate() {
    return due_;
  }

  public void setPercentComplete(int p) {
    this.percent_complete_ = p;
  }

  public int getPercentComplete() {
    return this.percent_complete_;
  }

  public void setPriority(TaskPriority p) {
    priority_ = p;
  }

  public TaskPriority getPriority() {
    return priority_;
  }

  public DboFormPanel<? extends DatabaseObject> getFormPanel(String name,
      DboFormPanel<? extends DatabaseObject> parent, boolean read_only)
      throws Exception {
    return new TaskFormPanel(name, this, parent, read_only);
  }
}

package reman.client.gui.schedule;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import reman.client.app.office_maintenance.Task;

public class TaskTableModel extends AbstractTableModel {

  private final String[] COLUMN_NAMES = { "Task", "Progress", "Due Date" };

  private ArrayList<Task> tasks_;

  public TaskTableModel() {
    tasks_ = new ArrayList<Task>();
  }

  public TaskTableModel(ArrayList<Task> tasks) {
    tasks_ = tasks;
  }

  public String getColumnName(int c) {
    return COLUMN_NAMES[c];
  }

  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  public int getRowCount() {
    return tasks_.size();
  }

  public void addTask(Task t) {
    tasks_.add(t);
    this.fireTableDataChanged();
  }

  public Task getTask(int row) {
    return tasks_.get(row);
  }

  public void removeTask(int row) {
    tasks_.remove(row);
    this.fireTableDataChanged();
  }

  public Object getValueAt(int r, int c) {
    Task t = tasks_.get(r);
    switch (c) {
    case 0:
      return t.getName();
    case 1:
      return t.getPercentComplete();
    case 2:
      return t.getDueDate();
    }
    return null;
  }

}

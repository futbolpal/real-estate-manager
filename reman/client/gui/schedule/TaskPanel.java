package reman.client.gui.schedule;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import reman.client.app.office_maintenance.Task;
import reman.client.gui.DateField;
import reman.client.gui.forms.DboFormPanel;
import reman.common.database.OfficeProjectManager;

public class TaskPanel extends JSplitPane {
	private JTable task_table_;
	private DboFormPanel task_form_;
	private JPanel options_;

	public TaskPanel() {
		super(JSplitPane.VERTICAL_SPLIT);
		this.setDividerLocation(.75);

		this.setPreferredSize(new Dimension(300, -1));

		JButton add_task = new JButton("Add Task", new ImageIcon(this.getClass().getResource(
				"list-add.png")));
		add_task.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Task t = new Task("New Task");
				TaskTableModel m = (TaskTableModel) task_table_.getModel();
				m.addTask(t);

			}
		});
		JButton del_task = new JButton("Remove Task", new ImageIcon(this.getClass().getResource(
				"list-remove.png")));
		del_task.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = task_table_.getSelectedRow();
				TaskTableModel m = (TaskTableModel) task_table_.getModel();
				m.removeTask(row);
			}
		});
		options_ = new JPanel();
		options_.setLayout(new FlowLayout(FlowLayout.LEFT));
		options_.add(add_task);
		options_.add(del_task);

		try {
			ArrayList<Task> tasks = OfficeProjectManager.instance().getCurrentProject().getOffice()
					.getOfficeTasks();
			task_table_ = new TaskTable(new TaskTableModel(tasks));
		} catch (Exception e1) {
			e1.printStackTrace();
			task_table_ = new TaskTable(new TaskTableModel());
		}
		task_table_.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}

				int row = task_table_.getSelectedRow();
				System.err.println("SELECTED ROW: " + row);
				TaskTableModel ttm = (TaskTableModel) task_table_.getModel();
				updateDescriptionPanel(ttm.getTask(row));
			}
		});

		task_table_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		task_table_.setPreferredSize(new Dimension(-1, 400));
		task_table_.setAutoCreateRowSorter(true);
		task_table_.setRowHeight(25);

		JScrollPane task_view = new JScrollPane(task_table_, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JPanel wrap = new JPanel(new BorderLayout());
		wrap.add(options_, BorderLayout.NORTH);
		wrap.add(task_view, BorderLayout.CENTER);
		this.setTopComponent(wrap);

		this.setBottomComponent(new JLabel("<html><i>Select a task to view details</i></html>"));
	}

	private void updateDescriptionPanel(final Task t) {

		try {
			System.err.println("Looking at ID: " + t.getID());
			/* Unlock old object */
			if (task_form_ != null) {
				System.err.println("Last ID: " + task_form_.getDatabaseObject().getID());
				task_form_.cancel();
			}

			task_form_ = t.getFormPanel("Task Details", null, false);

			JButton commit = new JButton("Save");
			commit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						OfficeProjectManager.instance().getCurrentProject().getOffice().lock();
						task_form_.commit();
						OfficeProjectManager.instance().getCurrentProject().getOffice().commit();
						OfficeProjectManager.instance().getCurrentProject().getOffice().unlock();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});

			JPanel controls = new JPanel();
			controls.add(commit);
			task_form_.add(controls, BorderLayout.SOUTH);

			this.setBottomComponent(task_form_);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class TaskTable extends JTable {
		public TaskTable(TableModel m) {
			super(m);
		}

		public TableCellRenderer getCellRenderer(int row, int column) {
			switch (column) {
			case 1:
				return new TableCellRenderer() {
					public Component getTableCellRendererComponent(JTable table, Object value,
							boolean isSelected, boolean hasFocus, int row, int column) {
						JProgressBar b = new JProgressBar();
						if (value != null) {
							b.setStringPainted(true);
							b.setValue((Integer) value);
						}
						return b;
					}
				};
			case 2:
				return new TableCellRenderer() {
					public Component getTableCellRendererComponent(JTable table, Object value,
							boolean isSelected, boolean hasFocus, int row, int column) {
						DateField f = new DateField();
						if (value != null)
							f.setDate((Timestamp) value);
						return f;
					}
				};
			}
			return super.getCellRenderer(row, column);
		}

	}
}

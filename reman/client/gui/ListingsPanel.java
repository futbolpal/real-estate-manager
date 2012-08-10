package reman.client.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.noos.xing.mydoggy.AggregationPosition;
import org.noos.xing.mydoggy.MultiSplitConstraint;
import org.noos.xing.mydoggy.ToolWindow;

import reman.client.app.Framework;
import reman.client.app.IconManager;
import reman.client.basetypes.Commission;
import reman.client.basetypes.Listing;
import reman.client.basetypes.RealEstateAction;
import reman.client.basetypes.TimeRange;
import reman.client.basetypes.Transaction;
import reman.common.database.OfficeProjectManager;

public class ListingsPanel extends JPanel implements GUIBuilder {
	private ArrayList<RealEstateAction> properties_;
	private JTable properties_table_;
	private JTable commissions_table_;
	private JToolBar properties_tools_;

	public ListingsPanel() {
		properties_tools_ = new JToolBar(JToolBar.HORIZONTAL);
		properties_tools_.setFloatable(false);
		properties_tools_.add(new AbstractAction("Add", IconManager.instance().getIcon(16, "actions",
				"list-add.png")) {
			public void actionPerformed(ActionEvent e) {
				JPopupMenu menu = new JPopupMenu();
				menu.add(new AbstractAction("Listing...") {
					public void actionPerformed(ActionEvent e) {
					}
				});
				menu.add(new AbstractAction("Transaction...") {
					public void actionPerformed(ActionEvent e) {
					}
				});
				menu.show(properties_tools_.getComponent(0), 0, (int) properties_tools_.getPreferredSize()
						.getHeight());
			}
		});

		properties_ = new ArrayList<RealEstateAction>();
		properties_.addAll(OfficeProjectManager.instance().getCurrentProject().getListings());
		properties_.addAll(OfficeProjectManager.instance().getCurrentProject().getSoldListings());
		properties_table_ = new JTable(new PropertiesModel()) {
			public TableCellRenderer getCellRenderer(int row, int column) {
				return new PropertiesCellRenderer();
			}
		};

		properties_table_.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					//show popup tools
				}
			}
		});
		properties_table_.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				Listing l = OfficeProjectManager.instance().getCurrentProject().getListings().get(
						e.getFirstIndex());
				commissions_table_.setModel(new CommissionsModel(l));
				commissions_table_.repaint();
			}
		});
		properties_table_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		properties_table_.getColumnModel().getColumn(0).setMaxWidth(16);

		commissions_table_ = new JTable() {
			public TableCellRenderer getCellRenderer(int row, int column) {
				return new CommissionsCellRenderer();
			}
		};
		commissions_table_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		commissions_table_.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int column = commissions_table_.getColumnModel().getColumnIndexAtX(e.getX());
				int row = e.getY() / commissions_table_.getRowHeight();
				JComponent c = (JComponent) commissions_table_.getCellRenderer(row, column)
						.getTableCellRendererComponent(commissions_table_,
								commissions_table_.getValueAt(row, column), true, true, row, column);
				MouseEvent e2 = SwingUtilities.convertMouseEvent(commissions_table_, e, c);
				for (Component c1 : c.getComponents()) {
					if (c1 instanceof JButton)
						((JButton) c1).doClick();
				}
			}
		});

		JScrollPane properties_view_ = new JScrollPane(properties_table_);
		properties_table_.setFillsViewportHeight(true);
		properties_view_.setColumnHeaderView(properties_tools_);

		JScrollPane commissions_view_ = new JScrollPane(commissions_table_);
		commissions_table_.setFillsViewportHeight(true);

		JSplitPane view = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		view.setLeftComponent(properties_view_);
		view.setRightComponent(commissions_view_);
		view.setDividerLocation(.5);

		this.setLayout(new BorderLayout());
		this.add(new JLabel("TEST"), BorderLayout.NORTH);
		this.add(view, BorderLayout.CENTER);
	}

	public void build(FrameworkWindow owner) {
		owner.registerContent("Listings", "Listings", null, this, "", true, new MultiSplitConstraint(
				AggregationPosition.LEFT));
	}

	private class PropertiesModel extends AbstractTableModel {
		private String[] COLUMNS = { "", "Name", "List Price", "Date" };

		public PropertiesModel() {

		}

		public String getColumnName(int c) {
			return COLUMNS[c];
		}

		public int getColumnCount() {
			return COLUMNS.length;
		}

		public int getRowCount() {
			return properties_.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			return properties_.get(rowIndex);
		}
	}

	private class CommissionsModel extends AbstractTableModel {
		private String[] COLUMNS = { "Agent", "Amount" };
		private Listing listing_;

		public CommissionsModel(Listing listing) {
			listing_ = listing;
		}

		public int getColumnCount() {
			return COLUMNS.length;
		}

		public String getColumnName(int c) {
			return COLUMNS[c];
		}

		public int getRowCount() {
			return listing_.getCommissions().size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Commission c = listing_.getCommissions().get(rowIndex);
			return c;
		}
	}

	private class PropertiesCellRenderer implements TableCellRenderer {

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			RealEstateAction l = (RealEstateAction) value;
			switch (column) {
			case 0:
				JLabel label = new JLabel();
				if (l instanceof Transaction) {
					label.setIcon(IconManager.instance().getIcon(16, "actions", "go-home.png"));
					label.setPreferredSize(new Dimension(16, 16));
				} else {

					label.setIcon(IconManager.instance().getIcon(16, "actions", "media-record.png"));
					label.setPreferredSize(new Dimension(16, 16));
				}
				return label;
			case 1:
				return new JLabel(l.getName());
			case 2:
				return new JLabel("$");// + l.getPrice());
			case 3:
				TimeRange t = l.getEffectiveTimeRange();
				if (t != null) {
					String start = DateFormat.getDateInstance(DateFormat.SHORT).format(
							new Date(t.getBegin().getTime()));
					String end = DateFormat.getDateInstance(DateFormat.SHORT).format(
							new Date(t.getEnd().getTime()));
					return new JLabel(start + " - " + end);
				} else
					return new JLabel("");
			}
			return null;
		}
	}

	private class CommissionsCellRenderer implements TableCellRenderer {
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			final Commission c = (Commission) value;
			switch (column) {
			case 0:
				JPanel p = new JPanel();
				JButton details = new JButton(IconManager.instance().getIcon(16, "actions", "go-next.png"));
				details.setBorderPainted(false);
				details.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ToolWindow w = Framework.instance().getWindow().getToolManager().getToolWindow(
								AgentViewPanel.ID);
						w.setActive(true);
						AgentViewPanel p = (AgentViewPanel) w.getComponent();
						p.setAgent(c.getAgent());
					}
				});
				p.setLayout(new BorderLayout());
				p.add(new JLabel(c.getAgent().getName()), BorderLayout.WEST);
				p.add(details, BorderLayout.EAST);
				return p;
			case 1:
				if (c.isFlatRated())
					return new JLabel("$" + c.getAmount());
				else
					return new JLabel(c.getAmount() + "%");
			}
			return null;
		}
	}
}

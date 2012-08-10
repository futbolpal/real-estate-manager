package reman.client.gui.forms;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import reman.client.app.Framework;
import reman.client.app.IconManager;
import reman.client.app.listeners.DboChangedListener;
import reman.client.gui.AutoComplete;
import reman.client.gui.FrameworkWindow;
import reman.client.gui.GUIBuilder;
import reman.common.database.DatabaseObject;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.LoggedInException;
import reman.common.messaging.DboChangedMessage;
import reman.common.messaging.DboLockedMessage;

/**
 * This provides a general way of creating a form that describes a DBO.
 * It allows forms to be chained and enforces the lock/commit/unlock sequence 
 * to ensure data synchronization amongst users.  
 * <BR><BR>
 * The panel uses focus to determine which object it should lock.  This works for
 * all of the nested panels, only locking pieces of the overall object at a time.  
 * <BR><BR>
 * The panel can be set read only in which case the form components will be completely disabled
 * and locks will not be created on focus.  
 * <BR><BR>
 * This class chains with other DBOFormPanels to provide a recursive nature.  
 * 
 * @author jonathan
 *
 * @param <T>
 */
public abstract class DboFormPanel<T extends DatabaseObject> extends JPanel implements
		FocusListener, GUIBuilder, DboChangedListener {

	protected T dbo_;

	protected FormPanel form_;

	protected ArrayList<DboFormPanel<?>> children_;

	protected DboFormPanel<?> parent_;

	protected boolean read_only_;

	protected boolean has_focus_;

	protected JToolBar tools_;

	private boolean dynamic_;

	protected ArrayList<JComponent> form_items_;

	protected boolean auto_commit_;

	public DboFormPanel(String object_name) throws Exception {
		this(object_name, null, null, false);
	}

	public DboFormPanel(String object_name, T o, DboFormPanel<?> parent, boolean read_only) {
		auto_commit_ = true;
		read_only_ = read_only;
		dbo_ = o;
		children_ = new ArrayList<DboFormPanel<?>>();
		parent_ = parent;
		dynamic_ = true;
		form_items_ = new ArrayList<JComponent>();

		tools_ = new JToolBar();
		tools_.setFloatable(false);
		tools_.setFocusable(false);

		JButton rollback = new JButton(IconManager.instance().getIcon(16, "actions", "edit-undo.png"));
		rollback.setToolTipText("Rollback");
		rollback.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rollback();
			}
		});
		tools_.add(rollback);
		JButton retrieve = new JButton(IconManager.instance()
				.getIcon(16, "actions", "view-refresh.png"));
		retrieve.setToolTipText("Retrieve Object");
		retrieve.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					dbo_.retrieve();
				} catch (DatabaseException e1) {
					e1.printStackTrace();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		});
		tools_.add(retrieve);
		final JToggleButton live = new JToggleButton(IconManager.instance().getIcon(16, "actions",
				"appointment-new.png"), dynamic_);
		live.setToolTipText("Dynamic Mode");
		live.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				dynamic_ = live.isSelected();
			}
		});
		tools_.add(live);
		for (Component c : tools_.getComponents())
			c.setFocusable(false);

		this.setFocusable(false);
		this.setName(object_name);
		this.setLayout(new BorderLayout());
		this.add(form_ = new FormPanel(), BorderLayout.CENTER);
		this.addHeader();
		if (parent == null)
			form_.setFocusCycleRoot(true);
		Framework.instance().addDboChangedListener(this);
	}

	public boolean isAutoCommit() {
		return auto_commit_;
	}

	public void setAutoCommit(boolean flag) {
		auto_commit_ = flag;
	}

	public void rollback() {
		try {
			dbo_.rollback();
			System.out.println("viewing: " + dbo_.getVersion());
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.updateForm();
	}

	public boolean isRoot() {
		return parent_ == null;
	}

	public DboFormPanel<?> getRoot() {
		if (this.isRoot())
			return this;
		else
			return parent_.getRoot();
	}

	/**
	 * 
	 * @return true if the form is in read only mode
	 */
	public boolean isReadOnly() {
		return read_only_;
	}

	/**
	 * Sets the mode of the panel to be read only if f is true
	 * @param f true for read only mode
	 */
	public void setReadOnly(boolean f) {
		read_only_ = f;
	}

	public T getDatabaseObject() {
		return dbo_;
	}

	public abstract void retrieveForm();

	public abstract void updateForm();

	public void fillFromSuggestion(DatabaseObject o) {
		this.dbo_ = (T) o;
		this.updateForm();
	}

	protected void addHeader() {
		String title = "<html><b><i>" + this.getName() + "</i></b></html>";
		if (this.isRoot()) {
			this.addFormItem(title, tools_);
		} else {
			this.parent_.addFormItem(title, tools_);
		}
	}

	public void addFormItem(DboFormPanel<?> c) {
		children_.add(c);
		this.addFormItem("", c);
	}

	public void addFormItem(String text, JComponent c) {
		if (read_only_)
			c.setEnabled(false);
		else
			c.addFocusListener(this);
		if (c instanceof JTextField && !read_only_) {
			AutoComplete ac = new AutoComplete(this, (JTextField) c);
			form_.addFormItem(text, ac);
			this.register(ac);
		} else {
			form_.addFormItem(text, c);
			if (!(c instanceof DboFormPanel))
				this.register(c);
		}
		this.getRoot().validate();
	}

	protected void register(JComponent c) {
		form_items_.add(c);
		for (Component cc : c.getComponents()) {
			if (cc instanceof JComponent)
				register((JComponent) cc);
		}
	}

	public void removeAll() {
		form_.removeAll();
		form_items_.clear();
	}

	public void focusGained(FocusEvent e) {
		if (dbo_ == null)
			return;
		has_focus_ = true;
		try {
			if (!dbo_.isLocked())
				dbo_.lock();
			else {
				//show message
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (LoggedInException e1) {
			e1.printStackTrace();
		}
	}

	public void focusLost(FocusEvent e) {
		//return;

		Component c = e.getOppositeComponent();
		if (c instanceof JPopupMenu)
			return;
		if (auto_commit_ && c != null && !form_items_.contains(c)) {
			try {
				this.retrieveForm();
				dbo_.commit(this.getRoot().getDatabaseObject());
				dbo_.unlock();
				has_focus_ = false;
			} catch (SQLException e1) {
				e1.printStackTrace();
			} catch (DatabaseException e1) {
				e1.printStackTrace();
			}
		}

	}

	/**
	 * Dynamic mode will refetch changed objects from the database on the 
	 * fly
	 * @param flag
	 */
	public void setDynamicMode(boolean flag) {
		dynamic_ = flag;
	}

	/**
	 * Returns whether this form is in dynamic mode or not.
	 * @return
	 */
	public boolean isDynamicMode() {
		return dynamic_;
	}

	public void dboChangedEvent(DboChangedMessage m) {
		if (m instanceof DboLockedMessage) {
			DboLockedMessage lm = ((DboLockedMessage) m);
			boolean same = m.getDatabaseObject().getVersionChain().equals(dbo_.getVersionChain());
			if (same) {
				this.setReadOnly(lm.isLocked());
			}

		} else {
			if (dynamic_) {
				try {
					dbo_.retrieve();
				} catch (DatabaseException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void build(FrameworkWindow w) {
		w.registerContent(this.getClass().getName(), this.getName(), null, this, null, false, null);
	}
}

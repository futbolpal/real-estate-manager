package reman.client.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;


/**
 * This is a GUI component for inputting dates.  It provides a popup menu date
 * picker to eliminate the need for typing.  
 * @author jonathan
 */
public class DateField extends JPanel {
	private ArrayList<DateFieldListener> listeners_;
	private CalendarPanel calendar_;
	private JPopupMenu menu_;
	private JButton drop_;
	protected JFormattedTextField date_field_;

	public DateField(FocusListener owner) {
		listeners_ = new ArrayList<DateFieldListener>();

		calendar_ = new CalendarPanel(false);
		calendar_.addListener(new CalendarPanelListener() {
			public void dateChangedEvent(Calendar date) {
				DateField.this.setDate(new Timestamp(date.getTimeInMillis()));
				menu_.setVisible(false);
			}
		});

		menu_ = new JPopupMenu();
		menu_.add(calendar_);

		date_field_ = new JFormattedTextField(new SimpleDateFormat("MM-dd-yyyy"));
		date_field_.setColumns(8);
		if (owner != null)
			date_field_.addFocusListener(owner);

		drop_ = new JButton("v");
		if (owner != null)
			drop_.addFocusListener(owner);
		drop_.setPreferredSize(new Dimension(25, 20));
		drop_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (DateField.this.isEnabled()) {
					menu_.show(DateField.this, 0, DateField.this.getHeight());
				}
			}
		});
		this.setFocusable(false);
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.add(date_field_);
		this.add(drop_);
	}

	public void setEditable(boolean flag) {
		date_field_.setEditable(flag);
	}

	/**
	 * 
	 * @return true if the date picker is visible
	 */
	public boolean isPopupShowing() {
		return menu_.isShowing();
	}

	/**
	 * Sets the value in the text field to represent the selected timestamp
	 * @param t
	 */
	public void setDate(Timestamp t) {
		if (t == null)
			return;
		date_field_.setValue(new Date(t.getTime()));
		fireDateChanged();
	}

	/**
	 * Returns the date in the text field 
	 * @return Timestamp - the selected date
	 */
	public Timestamp getDate() {
		if (date_field_.getValue() == null)
			return null;
		return new Timestamp(((Date) date_field_.getValue()).getTime());
	}

	/**
	 * Notify listeners that a change to the date has been made
	 */
	public void fireDateChanged() {
		Timestamp t = getDate();
		for (DateFieldListener l : listeners_) {
			l.dateFieldChanged(t);
		}
	}

	/**
	 * Registers a listener to listen for changes in the date.
	 * @param l
	 */
	public void addDateChangedListener(DateFieldListener l) {
		listeners_.add(l);
	}

	public void setEnabled(boolean flag) {
		super.setEnabled(flag);
		for (Component c : this.getComponents())
			c.setEnabled(flag);
	}
}

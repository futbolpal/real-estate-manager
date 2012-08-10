package reman.client.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPopupMenu;

/**
 * This is a GUI component for inputting dates.  It provides a popup menu date
 * picker to eliminate the need for typing.  
 * @author jonathan
 */
public class DateTimeField extends DateField {
	private ArrayList<DateFieldListener> listeners_;
	private TimePickerPanel time_picker_;
	private JPopupMenu menu_;
	private JButton drop_;
	protected JFormattedTextField time_field_;

	public DateTimeField(FocusListener owner) {
		super(owner);
		listeners_ = new ArrayList<DateFieldListener>();

		time_picker_ = new TimePickerPanel();
		/*
		time_picker_.addListener(new TimePickerPanelListener() {
			public void timeChangedEvent(Calendar date) {
				DateTimeField.this.setDate(new Timestamp(date.getTimeInMillis()));
				menu_.setVisible(false);
			}
		});
		*/

		menu_ = new JPopupMenu();
		menu_.add(time_picker_);

		time_field_ = new JFormattedTextField(new SimpleDateFormat("h:mm a"));
		time_field_.setColumns(8);
		if (owner != null)
			time_field_.addFocusListener(owner);

		drop_ = new JButton("v");
		if (owner != null)
			drop_.addFocusListener(owner);
		drop_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (DateTimeField.this.isEnabled()) {
					menu_.show(DateTimeField.this, 0, DateTimeField.this.getHeight());
				}
			}
		});

		this.add(time_field_);
		this.add(drop_);
	}

	public void setEditable(boolean flag) {
		super.setEditable(flag);
		time_field_.setEditable(flag);
	}

	/**
	 * 
	 * @return true if the date picker is visible
	 */
	public boolean isPopupShowing() {
		return menu_.isShowing();
	}

	public void setDate(Timestamp t) {
		if (t == null)
			return;
		super.setDate(t);
		time_field_.setValue(new Date(t.getTime()));
	}

	public Timestamp getDate() {
		Timestamp date_stamp = super.getDate();
		Date time_stamp = (Date) time_field_.getValue();
		if (date_stamp == null || time_stamp == null)
			return null;
		Calendar date = Calendar.getInstance();
		Calendar time = Calendar.getInstance();
		date.setTimeInMillis(super.getDate().getTime());
		time.setTime(((Date) time_field_.getValue()));

		Calendar ret = Calendar.getInstance();
		ret.set(Calendar.MONTH, date.get(Calendar.MONTH));
		ret.set(Calendar.DAY_OF_YEAR, date.get(Calendar.DAY_OF_YEAR));
		ret.set(Calendar.YEAR, date.get(Calendar.YEAR));
		ret.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
		ret.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
		System.out.println(DateFormat.getDateTimeInstance().format(ret.getTime()));
		return new Timestamp(ret.getTimeInMillis());
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
}

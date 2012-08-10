package reman.client.gui.expense;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import reman.client.basetypes.TimeRange;
import reman.client.gui.DateField;
import reman.client.gui.forms.DboFormPanel;

public class TimeRangePanel extends DboFormPanel<TimeRange> {
	private DateField start_time_;
	private DateField end_time_;

	private JCheckBox start_applicable_;
	private JCheckBox end_applicable_;

	public TimeRangePanel(String title, TimeRange r, DboFormPanel<?> parent, boolean read_only) {
		super(title, r, parent, read_only);

		this.addFormItem("Start Time", this.start_time_ = new DateField(this));
		this.addFormItem("Not Applicable", this.start_applicable_ = new JCheckBox());

		this.addFormItem("End Time", this.end_time_ = new DateField(this));
		this.addFormItem("Not Applicable", this.end_applicable_ = new JCheckBox());

		this.start_applicable_.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				start_time_.setEnabled(!start_applicable_.isSelected());
				if (start_applicable_.isSelected()) {
					start_time_.setDate(new Timestamp(getMinimumTime().getTimeInMillis()));
				} else {
					resetStart();
				}
			}
		});

		this.end_applicable_.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				end_time_.setEnabled(!end_applicable_.isSelected());
				if (end_applicable_.isSelected()) {
					end_time_.setDate(new Timestamp(getMaximumTime().getTimeInMillis()));
				} else {
					resetEnd();
				}
			}
		});

		this.updateForm();
	}

	private Calendar getMinimumTime() {
		Calendar cal = Calendar.getInstance();
		cal.set(cal.getMinimum(Calendar.YEAR), cal.getMinimum(Calendar.MONTH), cal
				.getMinimum(Calendar.DATE), cal.getMinimum(Calendar.HOUR), cal.getMinimum(Calendar.MINUTE),
				cal.getMinimum(Calendar.SECOND));
		return cal;
	}

	public Calendar getMaximumTime() {
		Calendar cal = Calendar.getInstance();
		cal.set(cal.getMaximum(Calendar.YEAR), cal.getMaximum(Calendar.MONTH), cal
				.getMaximum(Calendar.DATE), cal.getMaximum(Calendar.HOUR), cal.getMaximum(Calendar.MINUTE),
				cal.getMaximum(Calendar.SECOND));
		return cal;
	}

	public void setStartEnabled(boolean e) {
		this.start_applicable_.setSelected(e);
	}

	public void setEndEnabled(boolean e) {
		this.end_applicable_.setSelected(e);
	}

	@Override
	public void retrieveForm() {
		if (!dbo_.setBegin(start_time_.getDate())) {
			JOptionPane.showMessageDialog(this, "Invalid start date. Must be before end date.");
			resetStart();
		}
		if (!dbo_.setEnd(end_time_.getDate())) {
			JOptionPane.showMessageDialog(this, "Invalid end date. Must be after start date.");
			resetEnd();
		}
	}

	private void resetStart() {
		start_time_.setDate(new Timestamp(end_time_.getDate().getTime() - 1000L));
	}

	private void resetEnd() {
		end_time_.setDate(new Timestamp(start_time_.getDate().getTime() + 1000L));
	}

	@Override
	public void updateForm() {
		if (dbo_.getBegin() != null)
			this.start_time_.setDate(dbo_.getBegin());
		else
			this.start_time_.setDate(new Timestamp((new Date()).getTime()));
		if (dbo_.getEnd() != null)
			this.end_time_.setDate(dbo_.getEnd());
		else
			this.end_time_.setDate(new Timestamp(this.start_time_.getDate().getTime() + 1L));
	}
}

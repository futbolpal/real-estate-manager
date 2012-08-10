package reman.client.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import reman.client.basetypes.TimeRange;
import reman.client.basetypes.TimeRange.TimeRangeType;
import reman.client.gui.forms.DboFormPanel;
import reman.common.database.DatabaseObject;

/**
 * This is a GUI component that provides a mechanism for creating a time range.
 * It shows two DateFields next to eachother, the left is the start, the right is
 * the end.  
 * @author jonathan
 *
 */
public class TimeRangeField extends DboFormPanel<TimeRange> {
	private DateField start_date_;
	private DateField end_date_;

	public TimeRangeField(String name, TimeRange r, DboFormPanel<? extends DatabaseObject> parent,
			boolean read_only) {
		super(name, r, parent, read_only);
		this.setBorder(new EmptyBorder(0, 0, 0, 0));
		JPanel form = buildForm(r.getType());
		this.addFormItem("", form);

		DateFieldListener l = new DateFieldListener() {
			public void dateFieldChanged(Timestamp t) {
				boolean valid = isValid();
				if (!valid) {
					start_date_.setOpaque(true);
					end_date_.setOpaque(true);
					start_date_.setBackground(Color.PINK);
					end_date_.setBackground(Color.PINK);
				} else {
					start_date_.setOpaque(false);
					end_date_.setOpaque(false);
				}
				start_date_.validate();
				end_date_.repaint();
			}
		};

		start_date_.addDateChangedListener(l);
		end_date_.addDateChangedListener(l);
	}

	protected JPanel buildForm(TimeRangeType t) {
		if (t == TimeRangeType.DATE) {
			end_date_ = new DateField(this);
		} else {
			start_date_ = new DateTimeField(this);
			end_date_ = new DateTimeField(this);
		}
		JPanel p = new JPanel();
		p.setFocusable(false);
		p.setBorder(new EmptyBorder(0, 0, 0, 0));
		p.setLayout(new GridLayout(0, 1));
		p.add(start_date_);

		JLabel toLabel = new JLabel("to");
		toLabel.setFocusable(false);
		p.add(toLabel);
		p.add(end_date_);
		return p;
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		start_date_.setEnabled(enabled);
		end_date_.setEnabled(enabled);
	}

	public TimeRange getTimeRange() {
		return new TimeRange(start_date_.getDate(), end_date_.getDate());
	}

	public boolean isValid() {
		if (start_date_.getDate() == null || end_date_.getDate() == null)
			return false;
		Date s = new Date(start_date_.getDate().getTime());
		Date e = new Date(end_date_.getDate().getTime());
		return s.before(e);
	}

	public void retrieveForm() {
		if (dbo_ == null)
			return;
		dbo_.setBegin(start_date_.getDate());
		dbo_.setEnd(end_date_.getDate());
	}

	public void updateForm() {
		if (dbo_ == null)
			return;
		start_date_.setDate(dbo_.getBegin());
		end_date_.setDate(dbo_.getEnd());
	}

	public void focusLost(FocusEvent e) {
		if (e.getComponent() instanceof DateField) {
			DateField f = (DateField) e.getComponent();
			if (f.isPopupShowing())
				return;
		}
		super.focusLost(e);
	}
}

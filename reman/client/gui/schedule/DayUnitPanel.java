package reman.client.gui.schedule;

import java.util.Calendar;

import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

public class DayUnitPanel extends CalendarPanelUnitPanel {

	public DayUnitPanel(CalendarPanel owner, Calendar date) {
		super(owner, date);
		build();
	}

	public void build() {
		final int day = this.getCalendar().get(Calendar.DAY_OF_MONTH);
		this.setBorder(new TitledBorder(String.valueOf(day)));
		if (this.getOwner().isShowingDetails()) {
			String details_str = new String();
			if (meetings_.size() > 0)
				details_str = "<html>" + meetings_.size() + " meetings<BR>";
			if (meetings_.size() > 0)
				details_str += tasks_.size() + " tasks due";
			JLabel details_lbl = new JLabel(details_str);
			details_lbl.setFont(this.getFont().deriveFont(8f));
			this.add(details_lbl);
		}
	}

	public int getUnitResolution() {
		return Calendar.DAY_OF_YEAR;
	}
}
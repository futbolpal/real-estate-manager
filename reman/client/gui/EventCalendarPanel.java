package reman.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import reman.client.app.UserDatabaseObject;
import reman.client.app.office_maintenance.CalendarEvent;

public class EventCalendarPanel extends CalendarPanel {
  private JPanel details_;

  public EventCalendarPanel() {
    this.setBorder(new TitledBorder("Calendar"));
    this.add(details_ = new JPanel(), BorderLayout.SOUTH);
    details_.setPreferredSize(new Dimension(-1, 300));
    details_.setBorder(new TitledBorder("Day Details"));
    try {

      Collection<CalendarEvent> events = (Collection<CalendarEvent>) UserDatabaseObject
	  .load(CalendarEvent.class);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}

package reman.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import reman.client.app.office_maintenance.Note;
import reman.common.database.exceptions.DatabaseException;

public class NotepadPanel extends JPanel {
  /* Browse Old Notes */
  /*Write a new note */

  private JTextField note_name_;
  private JTextPane note_text_;

  private JComboBox browse_menu_;

  public NotepadPanel() {
    this.setLayout(new BorderLayout());

    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
    header.add(new JLabel("Title"));
    header.add(note_name_ = new JTextField(15));
    this.add(header, BorderLayout.NORTH);
    JButton new_button = new JButton("New");
    new_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				note_name_.setText("");
				note_text_.setText("");
			}});
    JButton save_button = new JButton("Save");
    save_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveNote();
			}});
    header.add(new_button);
    header.add(save_button);

    note_text_ = new JTextPane();
    JScrollPane notepane = new JScrollPane(note_text_);
    notepane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    notepane.setPreferredSize(new Dimension(250, 200));
    note_text_.setEditable(true);
    this.add(note_text_, BorderLayout.CENTER);

    browse_menu_ = new JComboBox();
    browse_menu_.addItem("Your past notes...");
    this.add(browse_menu_, BorderLayout.SOUTH);
  }

  private void saveNote() {
  	if (!note_name_.getText().isEmpty()) {
  		Note n = new Note(note_name_.getText());
  		n.setDescription(note_text_.getText());
  		try {
				n.commit();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
  		
  	}
  }
}

package reman.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import reman.client.gui.statusbar.ChatPanel;
import reman.common.database.UserManager;
import reman.common.messaging.ChatMessage;
import reman.common.messaging.MessageManager;

public class ChatBoxPanel extends JPanel {
	private String user_, other_user_;
	private Long other_id_, user_id_;
	private JTextPane text_area_;
	private JTextField text_box_;
	private static SimpleAttributeSet outgoing_ = new SimpleAttributeSet();
	private static SimpleAttributeSet incoming_ = new SimpleAttributeSet();
	private static SimpleAttributeSet outgoing_time_ = new SimpleAttributeSet();
	private static SimpleAttributeSet incoming_time_ = new SimpleAttributeSet();
	private static SimpleAttributeSet normal_text_ = new SimpleAttributeSet();
	private JButton send_;
	private ChatPanel parent_;
	private ChatOptionPanel options_;

	public ChatBoxPanel(ChatPanel parent, String user, String other_user, Long other_id) {
		parent_ = parent;
		user_id_ = UserManager.instance().getCurrentUserID();
		other_id_ = other_id;
		user_ = user;
		other_user_ = other_user;
		StyleConstants.setForeground(outgoing_time_, Color.red);
		StyleConstants.setForeground(outgoing_, Color.red);
		StyleConstants.setBold(outgoing_, true);
		StyleConstants.setForeground(incoming_time_, Color.blue);
		StyleConstants.setForeground(incoming_, Color.blue);
		StyleConstants.setBold(incoming_, true);
		StyleConstants.setForeground(normal_text_, Color.black);
		this.setLayout(new BorderLayout());
		options_ = new ChatOptionPanel(parent_, other_id_);
		this.add(options_, BorderLayout.NORTH);
		text_box_ = new JTextField(28);
		text_area_ = new JTextPane();
		JScrollPane scrollPane = new JScrollPane(text_area_);
		text_area_.setEditable(false);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(250, 200));
		this.add(scrollPane, BorderLayout.CENTER);
		JPanel button_panel = new JPanel();
		send_ = new JButton("Send");
		button_panel.setLayout(new FlowLayout());
		button_panel.add(text_box_);
		button_panel.add(send_);

		this.add(button_panel, BorderLayout.SOUTH);
		//Add listeners...
		send_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!text_box_.getText().isEmpty()) {
					//send a message
					ChatMessage m = new ChatMessage(user_id_, text_box_.getText());
					MessageManager.instance().send(other_id_, m);
					displayMessage(m);
					clearTextBox();
				}
			}
		});

		text_box_.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					send_.doClick();
				}
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}
		});

		this.setFocusable(true);
		this.setRequestFocusEnabled(true);
		this.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent arg0) {
				text_box_.requestFocus();
			}

			public void focusLost(FocusEvent arg0) {
			}
		});
	}

	public void displayMessage(ChatMessage m) {
		if (m.getSender() == user_id_) {
			//outgoing message
			if (options_.showTimestamp()) {
				insertText(getDate(), outgoing_time_);
				insertText(user_ + " :", outgoing_);
			} else
				insertText(user_ + ":", outgoing_);
			setEndSelection();
		} else {
			//incoming message
			if (options_.showTimestamp()) {
				insertText(getDate(), incoming_time_);
				insertText(other_user_ + " :", incoming_);
			} else
				insertText(other_user_ + ":", incoming_);

			setEndSelection();
		}
		insertText(" " + m.getMessage() + "\n", normal_text_);
		setEndSelection();
	}

	private void clearTextBox() {
		text_box_.setText("");
	}

	private void insertText(String text, AttributeSet set) {
		try {
			text_area_.getDocument().insertString(text_area_.getDocument().getLength(), text, set);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	// Needed for inserting icons in the right places
	private void setEndSelection() {
		text_area_.setSelectionStart(text_area_.getDocument().getLength());
		text_area_.setSelectionEnd(text_area_.getDocument().getLength());
	}

	private String getDate() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
		return "(" + sdf.format(cal.getTime()) + ") ";
	}
}

package reman.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.Timer;

import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.CustomDockableDescriptor;

/**
 * Provides a panel to handle notifications.  When the setText function is called, the text is set
 * and fades out over a period of time.  
 * @author jonathan
 *
 */
public class TextNotificationPanel extends CustomDockableDescriptor {
	private JLabel text_;

	public TextNotificationPanel(MyDoggyToolWindowManager owner) {
		super(owner, ToolWindowAnchor.BOTTOM);
		//this.setAvailable(true);

		text_ = new JLabel();
		text_.setPreferredSize(new Dimension(200, 25));
		text_.setOpaque(false);
		Timer t = new Timer(100, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Color c = text_.getForeground();
				int alpha = Math.max(c.getAlpha() - 25, 0);
				Color t = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
				text_.setForeground(t);
			}
		});
		t.start();
	}

	/**
	 * Apply notification text
	 */
	public void setText(String text) {
		text_.setText(text);
		text_.setForeground(Color.BLACK);
	}

	public JComponent getRepresentativeAnchor(Component arg0) {
		return text_;
	}

	public void updateRepresentativeAnchor() {
	}
}

package reman.client.gui.quick_icon;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import reman.client.app.Framework;

public class QuickIcon extends JLabel {
	public QuickIcon(ImageIcon icon, String text, JComponent panel) {
		this(icon, text, panel, 300, 200);
	}

	public QuickIcon(ImageIcon icon, final String text, final JComponent panel, int w, int h) {
		panel.setPreferredSize(new Dimension(w, h));
		this.setIcon(icon);
		this.setPreferredSize(new Dimension(25, 25));
		this.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				Framework.instance().setStatus(text);
			}

			public void mouseClicked(MouseEvent e) {
				JPopupMenu p = new JPopupMenu();
				p.add(panel);
				p.show(QuickIcon.this, 0, -(int) panel.getPreferredSize().getHeight());
			}
		});
	}
}

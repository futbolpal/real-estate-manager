package reman.client.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.noos.xing.mydoggy.Content;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;

import reman.client.app.Framework;

public class FrameworkMenuBar extends JMenuBar {
	private JMenu file_;
	private JMenu edit_;
	private JMenu reports_;
	private JMenu window_;
	private JMenu views_;
	private JMenu help_;

	public FrameworkMenuBar(final MyDoggyToolWindowManager owner) {
		file_ = new JMenu("File");
		file_.add(new AbstractAction("Switch user...") {
			public void actionPerformed(ActionEvent e) {

			}
		});
		file_.add(new AbstractAction("Exit") {
			public void actionPerformed(ActionEvent arg0) {
				Framework.instance().close();
			}
		});

		edit_ = new JMenu("Edit");

		reports_ = new JMenu("Reports");

		views_ = new JMenu("Show View");
		views_.addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent e) {
				// TODO Auto-generated method stub

			}

			public void menuDeselected(MenuEvent e) {
				// TODO Auto-generated method stub

			}

			public void menuSelected(MenuEvent e) {
				views_.removeAll();
				Content[] contents = owner.getContentManager().getContents();
				for (final Content c : contents) {
					views_.add(new AbstractAction(c.getTitle()) {
						public void actionPerformed(ActionEvent e) {
							owner.getContentManager().addContent(c.getId(), c.getTitle(), c.getIcon(),
									c.getComponent(), c.getToolTipText());
						}
					});
				}

			}
		});
		window_ = new JMenu("Window");
		window_.add(new AbstractAction("Open Perspective...") {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.showOpenDialog(Framework.instance().getWindow());
				try {
					FileInputStream fos = new FileInputStream(fc.getSelectedFile());
					owner.getPersistenceDelegate().apply(fos);
				} catch (FileNotFoundException e1) {
					JOptionPane.showMessageDialog(null, "Perspective could not be opened.", "Open Failed",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		window_.addSeparator();
		window_.add(views_);
		window_.addSeparator();
		window_.add(new AbstractAction("Save Perspective As...") {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.showSaveDialog(Framework.instance().getWindow());
				try {
					FileOutputStream fos = new FileOutputStream(fc.getSelectedFile());
					owner.getPersistenceDelegate().save(fos);
				} catch (FileNotFoundException e1) {
					JOptionPane.showMessageDialog(null, "Perspective could not be saved.", "Save Failed",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		help_ = new JMenu("Help");
		help_.add(new AbstractAction("About") {
			public void actionPerformed(ActionEvent arg0) {

			}
		});

		this.addMenu(file_);
		this.addMenu(edit_);
		this.addMenu(reports_);
		this.addMenu(window_);
		this.addMenu(help_);
	}

	public void addMenu(final JMenu menu) {
		menu.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				Framework.instance().getWindow().setStatus(menu.getText());
			}
		});
		this.add(menu);
	}

}

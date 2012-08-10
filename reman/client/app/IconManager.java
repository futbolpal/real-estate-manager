package reman.client.app;

import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

public class IconManager {
	private static IconManager this_;

	private static String PATH = "/Icons/tango-icon-theme-0.8.1/";

	private Hashtable<String, Hashtable<String, Icon>> icons16_;
	private Hashtable<String, Hashtable<String, Icon>> icons22_;
	private Hashtable<String, Hashtable<String, Icon>> icons32_;
	private Hashtable<Integer, Hashtable<String, Hashtable<String, Icon>>> all_;

	public IconManager() {
		icons16_ = new Hashtable<String, Hashtable<String, Icon>>();
		icons22_ = new Hashtable<String, Hashtable<String, Icon>>();
		icons32_ = new Hashtable<String, Hashtable<String, Icon>>();
		all_ = new Hashtable<Integer, Hashtable<String, Hashtable<String, Icon>>>();
		all_.put(16, icons16_);
		all_.put(22, icons22_);
		all_.put(32, icons32_);
	}

	public Icon getIcon(int size, String category, String name) {
		String path = System.getProperty("user.dir") + PATH + size + "x" + size + "/" + category + "/"
				+ name;
		Hashtable<String, Icon> cat_icons = all_.get(size).get(category);
		if (cat_icons == null) {
			cat_icons = new Hashtable<String, Icon>();
			ImageIcon icon = new ImageIcon(path);
			if (icon == null)
				return null;
			cat_icons.put(name, icon);
			all_.get(size).put(category, cat_icons);
			return icon;
		} else {
			Icon icon = cat_icons.get(name);
			if (icon == null) {
				icon = new ImageIcon(path);
				cat_icons.put(name, icon);
			}
			return icon;
		}
	}

	public static void main(String... args) {
		System.out.println(System.getProperty("user.dir"));
		JFrame f = new JFrame();
		Icon icon = IconManager.instance().getIcon(16, "actions", "contact-new.png");
		System.out.println(icon.getIconHeight());
		System.out.println(icon);
		JButton l = new JButton(icon);
		f.getContentPane().add(l);
		f.pack();
		f.setVisible(true);
	}

	public static IconManager instance() {
		if (this_ == null)
			this_ = new IconManager();
		return this_;
	}
}

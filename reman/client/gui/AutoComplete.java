package reman.client.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import reman.client.basetypes.Agent;
import reman.client.gui.forms.AgentFormPanel;
import reman.client.gui.forms.DboFormPanel;
import reman.common.database.DatabaseObject;
import reman.common.database.MemoryManager;

import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;

public class AutoComplete extends JComboBox {
	private JTextField field_;
	private DboFormPanel<? extends DatabaseObject> owner_;
	private TreeSet<DBOSuggestion> suggestions_;
	private DBOSuggestion suggestion_;

	public AutoComplete(DboFormPanel<?> owner, JTextField f) {
		suggestions_ = new TreeSet<DBOSuggestion>();
		owner_ = owner;

		field_ = f;
		field_.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER || e.getKeyChar() == KeyEvent.VK_ESCAPE) {
					AutoComplete.this.hidePopup();
				}
				updatePopup(field_.getText());
			}
		});
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().endsWith("Changed") && AutoComplete.this.isPopupVisible()
						&& AutoComplete.this.getSelectedItem() instanceof DBOSuggestion) {
					suggestion_ = (DBOSuggestion) AutoComplete.this.getSelectedItem();
				}
			}
		});
		this.addPopupMenuListener(new PopupMenuListener() {
			private boolean canceled_;

			public void popupMenuCanceled(PopupMenuEvent e) {
				canceled_ = true;
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				if (!canceled_) {
					fireUpdate(suggestion_);
				}
				canceled_ = false;
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}

		});

		DefaultComboBoxModel model = (DefaultComboBoxModel) this.getModel();
		this.setUI(new WindowsComboBoxUI());
		model.removeListDataListener(model.getListDataListeners()[0]);
		this.remove(0);//remove arrow
		this.setRenderer(new CustomRenderer());
		this.setEditor(new CustomEditor(f));
		this.setBounds(50, 50, 75, 21);
		this.setEditable(true);
		this.setEnabled(f.isEnabled());
	}

	public String getText() {
		return field_.getText();
	}

	public void fireUpdate(DBOSuggestion s) {
		if (s != null)
			owner_.fillFromSuggestion(s.getDatabaseObject());
	}

	public void updatePopup(String text) {
		System.out.println("updated");
		DefaultComboBoxModel model = (DefaultComboBoxModel) this.getModel();
		model.removeAllElements();
		if (text == null || text.isEmpty())
			return;
		suggestions_ = this.searchForSuggestions(text);
		int max_results = 5;
		for (DBOSuggestion suggestion : suggestions_) {
			model.addElement(suggestion);
			max_results--;
			if (max_results == 0)
				break;
		}

		for (int i = 0; i < model.getSize(); i++) {
			String element = model.getElementAt(i).toString().toLowerCase();
			if (element.startsWith(text)) {
				model.setSelectedItem(model.getElementAt(i));
				break;
			}
		}
		if (AutoComplete.this.isShowing() && model.getSize() > 0)
			AutoComplete.this.setPopupVisible(true);
	}

	public void fireActionEvent() {
		super.fireActionEvent();
	}

	protected TreeSet<DBOSuggestion> searchForSuggestions(String text) {
		TreeSet<DBOSuggestion> matches = new TreeSet<DBOSuggestion>();

		ArrayList<DatabaseObject> objects_of_type = MemoryManager.instance().getDatabaseObjectsByClass(
				owner_.getDatabaseObject().getClass());
		if (objects_of_type == null)
			return matches;

		try {
			for (DatabaseObject of_type : objects_of_type) {
				boolean add = false;
				double strength = 0;

				/* Collect fields */
				ArrayList<Field> fields = new ArrayList<Field>();
				Class c = of_type.getClass();
				while (c != null) {
					for (Field f : c.getDeclaredFields()) {
						if (c.equals(DatabaseObject.class) && Modifier.isPrivate(f.getModifiers()))
							continue;
						fields.add(f);
						/* Examine Fields*/
						f.setAccessible(true);
						Object value = f.get(of_type);
						if (value == null || value.toString() == null) {
							strength -= 1;
							continue;
						}
						if (value.toString().toLowerCase().contains(text.toLowerCase())) {
							strength += text.toString().length() / value.toString().length();
							add = true;
						}
					}
					c = c.getSuperclass();
				}
				if (add) {
					matches.add(new DBOSuggestion(of_type, strength));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return matches;
	}

	private class DBOSuggestion implements Comparable<DBOSuggestion> {
		private DatabaseObject obj_;
		private double strength_;

		public DBOSuggestion(DatabaseObject obj, double strength) {
			obj_ = obj;
			strength_ = strength;
		}

		public double getStrength() {
			return strength_;
		}

		public DatabaseObject getDatabaseObject() {
			return obj_;
		}

		public int compareTo(DBOSuggestion s) {
			if (s.getStrength() > this.getStrength())
				return 1;
			else
				return -1;
		}
	}

	private class CustomEditor extends BasicComboBoxEditor {
		public CustomEditor(JTextField field) {
			super.editor = field;
		}
	}

	private class CustomRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList l, Object v, int i, boolean s, boolean f) {
			JLabel label = (JLabel) super.getListCellRendererComponent(l, v, i, s, f);
			if (v == null)
				return label;
			DBOSuggestion sugg = (DBOSuggestion) v;
			DatabaseObject o = sugg.getDatabaseObject();

			label.setFont(label.getFont().deriveFont(8));
			String text = o.toString();
			label.setText(text);
			label.setPreferredSize(new Dimension(100, 21));
			return label;
		}
	}

	public static void main(String arg[]) {
		JFrame f = new JFrame("AutoCompleteComboBox");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(200, 300);
		Container cp = f.getContentPane();
		cp.setLayout(null);
		//String[] names= {"Beate", "Claudia", "Fjodor", "Fred", "Friedrich",	"Fritz", "Frodo", "Hermann", "Willi"};
		//JComboBox cBox= new AutoComplete(names);
		Locale[] locales = Locale.getAvailableLocales();//
		AgentFormPanel p = new AgentFormPanel("Agent", new Agent(), null, false);
		JTextField field = new JTextField(20);
		JComboBox cBox = new AutoComplete(p, field);
		for (Locale l : locales)
			((DefaultComboBoxModel) cBox.getModel()).addElement(l);
		cBox.setBounds(50, 50, 100, 21);
		cBox.setEditable(true);
		cp.add(cBox);
		f.setVisible(true);
	}
}

package reman.client.gui.search;

/**
 * ComboBoxItem class is used to hold a class and a String for use with a JComboBox GUI component.
 * @author Will
 *
 */
public class ComboBoxItem {
	private Class class_;
	
	public ComboBoxItem(Class c) {
		class_ = c;
	}
	
	public Class getItemClass() {
		return class_;
	}
	
	public String toString() {
		return class_.getSimpleName();
	}
}

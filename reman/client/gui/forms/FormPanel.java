package reman.client.gui.forms;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is a custom panel that makes it easy to add items in a form like fashion.  
 * @author jonathan
 *
 */
public class FormPanel extends JPanel implements FocusListener {
	private static final String COL_SPEC = "right:pref:none, 3dlu, left:default:grow, 3dlu";
	private ArrayList<Component> components_;
	private DefaultFormBuilder builder_;
	private FormLayout layout_;

	public FormPanel() {
		layout_ = new FormLayout(COL_SPEC, "");
		builder_ = new DefaultFormBuilder(layout_, this);
		components_ = new ArrayList<Component>();
		this.addFocusListener(this);
	}

	public void removeAll() {
		super.removeAll();
		components_.clear();
		layout_ = new FormLayout(COL_SPEC, "");
		builder_ = new DefaultFormBuilder(layout_, this);
		this.validate();
	}

	public void addFormItem(String label, JComponent c) {
		JLabel l = new JLabel(label);
		l.setFocusable(false);
		builder_.append(l, c);
		components_.add(c);
	}

	public void addFormSeparator(String label) {
		JSeparator js = new JSeparator(JSeparator.HORIZONTAL);
		js.setPreferredSize(new Dimension((int) this.getPreferredSize().getWidth(), 5));
		js.setFocusable(false);
		this.addFormItem("<html><b>" + label + "</b></html>", js);
	}

	public void focusGained(FocusEvent e) {
		this.transferFocus();
	}

	public void focusLost(FocusEvent e) {
	}
}

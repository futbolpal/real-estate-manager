package reman.client.gui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import reman.client.app.IconManager;
import reman.client.basetypes.Location;
import reman.client.basetypes.OwnerEntry;
import reman.client.basetypes.Property;
import reman.client.basetypes.PropertyType;

public class PropertyFormPanel extends DboFormPanel<Property> {
	private JComboBox type_;
	private LocationFormPanel location_;
	private ArrayList<OwnersEntryFormPanel> owners_;
	private JButton add_owners_;

	public PropertyFormPanel(Property p, DboFormPanel<?> parent, final boolean read_only) {
		super("Property", p, parent, read_only);
		owners_ = new ArrayList<OwnersEntryFormPanel>();
		add_owners_ = new JButton(IconManager.instance().getIcon(16, "actions", "list-add.png"));
		add_owners_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addOwner();
				buildPanel();
			}
		});
		this.buildPanel();
	}

	public void addOwner() {
		dbo_.getOwners().add(new OwnerEntry());
	}

	public void buildPanel() {
		this.removeAll();

		this.addFormItem("Property Type", type_ = new JComboBox(PropertyType.values()));
		this.addFormItem(location_ = new LocationFormPanel(null, this, this.isReadOnly()));
		for (OwnerEntry e : dbo_.getOwners()) {
			OwnersEntryFormPanel p = new OwnersEntryFormPanel(e, this, this.isReadOnly());
			owners_.add(p);
			this.addFormItem(p);
		}
		this.addFormItem("Add Owner", add_owners_);

		this.validate();
		this.updateForm();
	}

	public void retrieveForm() {
		dbo_.setPropertyType((PropertyType) type_.getSelectedItem());
		location_.retrieveForm();
		for (OwnersEntryFormPanel f : owners_)
			f.retrieveForm();

	}

	public void updateForm() {
		type_.setSelectedItem(dbo_.getPropertyType());
		location_.updateForm();
		for (OwnersEntryFormPanel f : owners_)
			f.updateForm();
	}

	public static void main(String... args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		JFrame f = new JFrame();
		Property l = new Property();

		PropertyFormPanel lp = new PropertyFormPanel(l, null, false);

		f.add(lp);
		f.setSize(500, 500);
		f.setVisible(true);
	}

}

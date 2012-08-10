package reman.client.gui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;

import reman.client.app.IconManager;
import reman.client.basetypes.BusinessEntity;
import reman.client.basetypes.ContactEntity;
import reman.client.basetypes.Property;

public class BusinessEntityFormPanel extends DboFormPanel<BusinessEntity> {

	private ArrayList<ContactEntityFormPanel> contacts_;
	private ArrayList<PropertyFormPanel> properties_;
	private JButton add_contact_;
	private JButton add_property_;

	public BusinessEntityFormPanel(BusinessEntity o, DboFormPanel<?> parent, boolean read_only) {
		super("Business Entity", o, parent, read_only);
		contacts_ = new ArrayList<ContactEntityFormPanel>();
		properties_ = new ArrayList<PropertyFormPanel>();
		add_contact_ = new JButton(IconManager.instance().getIcon(16, "actions", "list-add.png"));
		add_contact_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addContact();
				buildPanel();
			}
		});
		add_property_ = new JButton(IconManager.instance().getIcon(16, "actions", "list-add.png"));
		add_property_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addProperty();
				buildPanel();
			}
		});
		buildPanel();
	}

	private void addContact() {
		dbo_.addContact(new ContactEntity());
	}

	private void addProperty() {
		dbo_.addLiableProperty(new Property());
	}

	public void buildPanel() {
		this.removeAll();
		for (ContactEntity c : dbo_.getContacts()) {
			ContactEntityFormPanel p = new ContactEntityFormPanel(c, this, this.isReadOnly());
			contacts_.add(p);
			this.addFormItem("Contact", p);
		}
		for (Property p : dbo_.getLiableProperties()) {
			PropertyFormPanel c = new PropertyFormPanel(p, this, this.isReadOnly());
			properties_.add(c);
			this.addFormItem("Property", c);
		}

	}

	public void retrieveForm() {
		for (PropertyFormPanel p : properties_)
			p.retrieveForm();
		for (ContactEntityFormPanel p : contacts_)
			p.retrieveForm();
	}

	public void updateForm() {
		for (PropertyFormPanel p : properties_)
			p.updateForm();
		for (ContactEntityFormPanel p : contacts_)
			p.updateForm();
	}
}

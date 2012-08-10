package reman.client.gui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JTextField;

import reman.client.app.IconManager;
import reman.client.basetypes.Country;
import reman.client.basetypes.Location;
import reman.client.basetypes.State;

public class LocationFormPanel extends DboFormPanel<Location> {
	private JTextField name_;
	private ArrayList<JTextField> address_fields_;
	private JTextField city_;
	private JComboBox state_;
	private JTextField zip_;
	private JComboBox country_;
	private JButton add_address_;

	public LocationFormPanel(Location l, DboFormPanel<?> owner, boolean read_only) {
		super("Location", l, owner, read_only);
		address_fields_ = new ArrayList<JTextField>();
		add_address_ = new JButton(IconManager.instance().getIcon(16, "actions", "list-add.png"));
		add_address_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addAddressLine();
				buildPanel();
			}
		});
		buildPanel();
	}

	private void addAddressLine() {
		dbo_.getAdddress().add(new String());
	}

	protected void buildPanel() {
		this.removeAll();

		this.addFormItem("Name", name_ = new JTextField(20));
		if (dbo_.getAdddress().isEmpty())
			dbo_.addAddressLine("");
		for (int i = 0; i < dbo_.getAdddress().size(); i++) {
			JTextField f = new JTextField(20);
			address_fields_.add(f);
			this.addFormItem("Address " + (i + 1), f);
		}
		this.addFormItem("Add address line", add_address_);

		this.addFormItem("City", city_ = new JTextField(20));
		this.addFormItem("State", state_ = new JComboBox(State.values()));
		this.addFormItem("Zip", zip_ = new JTextField(6));
		this.addFormItem("Country", country_ = new JComboBox(Country.values()));

		this.validate();
		this.updateForm();
	}

	public void retrieveForm() {
		dbo_.setName(name_.getText());
		dbo_.getAdddress().clear();
		for (JTextField f : address_fields_)
			dbo_.getAdddress().add(f.getText());
		dbo_.setCity(city_.getText());
		dbo_.setState((State) state_.getSelectedItem());
		dbo_.setCountry((Country) country_.getSelectedItem());
		dbo_.setZip(Integer.valueOf(zip_.getText()));
	}

	public void updateForm() {
		name_.setText(dbo_.getName());
		for (int i = 0; i < dbo_.getAdddress().size(); i++)
			address_fields_.get(i).setText(dbo_.getAdddress().get(i));
		city_.setText(dbo_.getCity());
		state_.setSelectedItem(dbo_.getState());
		country_.setSelectedItem(dbo_.getCountry());
		zip_.setText("" + dbo_.getZip());
	}

	public static void main(String... args) {
		JFrame f = new JFrame();
		Location l = new Location();

		LocationFormPanel lp = new LocationFormPanel(l, null, false);

		f.add(lp);
		f.pack();
		f.setVisible(true);
	}
}

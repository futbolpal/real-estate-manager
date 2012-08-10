package reman.client.gui.forms;

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JTextField;

import reman.client.basetypes.Listing;

public class ListingFormPanel extends DboFormPanel<Listing> {
	private JFormattedTextField list_price_;

	public ListingFormPanel(Listing l) throws Exception {
		super(l.getName(), l, null, false);
		this.addFormItem("List Price", list_price_ = new JFormattedTextField(NumberFormat
				.getCurrencyInstance()));
	}

	public void retrieveForm() {

	}

	public void updateForm() {
		list_price_.setValue(dbo_.getPrice());
	}

}

package reman.client.gui.statusbar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JToolBar;

import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.CustomDockableDescriptor;

import reman.client.gui.quick_icon.QuickIcon;

public class QuickIconPanel extends CustomDockableDescriptor {
	private JToolBar panel_;

	public QuickIconPanel(MyDoggyToolWindowManager owner) {
		super(owner, ToolWindowAnchor.BOTTOM);
		panel_ = new JToolBar();
		panel_.setFloatable(false);
		panel_.setBorderPainted(false);
		//panel_.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel_.setPreferredSize(new Dimension(300, 25));

		this.setAnchor(ToolWindowAnchor.BOTTOM, 1000);
	}

	public void addQuickIcon(QuickIcon q) {
		panel_.add(q);
		panel_.validate();
	}

	public JComponent getRepresentativeAnchor(Component c) {
		return panel_;
	}

	public void updateRepresentativeAnchor() {
		panel_.validate();
	}

}

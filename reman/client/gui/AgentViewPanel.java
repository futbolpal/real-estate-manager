package reman.client.gui;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.noos.xing.mydoggy.AggregationPosition;
import org.noos.xing.mydoggy.DockedTypeDescriptor;
import org.noos.xing.mydoggy.MultiSplitConstraint;
import org.noos.xing.mydoggy.SlidingTypeDescriptor;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.ToolWindowType;

import reman.client.basetypes.Agent;
import reman.client.gui.forms.AgentFormPanel;

public class AgentViewPanel extends JPanel implements GUIBuilder {
	public static final String ID = "Agents";

	private JComboBox list_;
	private JPanel view_;

	public AgentViewPanel() {
		this.setLayout(new BorderLayout());
	}

	public void setAgent(Agent a) {
		if (view_ != null)
			this.remove(view_);
		try {
			view_ = new AgentFormPanel("Agent", a, null, false);
		} catch (Exception e) {
			view_ = new JPanel();
			view_.add(new JLabel("<html><i>Error</i></html>"));
		}
		this.add(view_, BorderLayout.CENTER);
		this.validate();
	}

	public void build(FrameworkWindow w) {
		ToolWindow tw = w.getToolManager().registerToolWindow("Agents", "Agents", null, this,
				ToolWindowAnchor.RIGHT);
		tw.setType(ToolWindowType.SLIDING);
		tw.getTypeDescriptor(DockedTypeDescriptor.class).setDockLength(500);
		tw.getTypeDescriptor(DockedTypeDescriptor.class).setAnimating(false);
	}
}

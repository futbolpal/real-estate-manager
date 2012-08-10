package reman.client.gui.office_config;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.noos.xing.mydoggy.AggregationPosition;
import org.noos.xing.mydoggy.Content;
import org.noos.xing.mydoggy.MultiSplitConstraint;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.content.MyDoggyMultiSplitContentManagerUI;

import reman.client.app.IconManager;
import reman.client.basetypes.Agent;
import reman.client.gui.FrameworkWindow;
import reman.client.gui.GUIBuilder;
import reman.common.database.OfficeProjectManager;
import reman.common.database.exceptions.DatabaseException;

public class OfficeConfigPanel extends MyDoggyToolWindowManager implements GUIBuilder {
	private PersonTable people_;

	public OfficeConfigPanel() {
		MyDoggyMultiSplitContentManagerUI splitUI = new MyDoggyMultiSplitContentManagerUI();
		this.getContentManager().setContentManagerUI(splitUI);
		splitUI.setShowAlwaysTab(true);

		JScrollPane people_view = new JScrollPane(people_ = new PersonTable());
		JToolBar person_tools = new JToolBar(JToolBar.VERTICAL);
		person_tools.setFloatable(false);
		person_tools.add(new AbstractAction("Add Member", IconManager.instance().getIcon(16, "actions",
				"list-add.png")) {
			public void actionPerformed(ActionEvent e) {
				JPopupMenu type = new JPopupMenu();
				type.add(new AbstractAction("Agent") {
					public void actionPerformed(ActionEvent e) {
						Agent p = new Agent();
						OfficeProjectManager.instance().getCurrentProject().getOffice().addMember(p);
						people_.update();
						try {
							people_.commitListOwner();
						} catch (SQLException e1) {
							e1.printStackTrace();
						} catch (DatabaseException e1) {
							e1.printStackTrace();
						}
					}
				});
				Component c = (Component) e.getSource();
				type.show(c, c.getX() + c.getWidth(), c.getY());
			}
		});

		people_view.setRowHeaderView(person_tools);

		Content people = this.getContentManager().addContent("People", "People", null, people_view, "",
				new MultiSplitConstraint(AggregationPosition.DEFAULT));
	}

	public void build(FrameworkWindow w) {
		w.registerContent("Office Configuration", "Office Configuration", null, this, null, true,
				new MultiSplitConstraint(AggregationPosition.RIGHT));
	}

}

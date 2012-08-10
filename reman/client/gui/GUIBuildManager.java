package reman.client.gui;

import java.util.ArrayList;

import reman.client.app.finance.gui.FinanceManagerPanel;
import reman.client.gui.office_config.OfficeConfigPanel;
import reman.client.gui.schedule.SchedulePanel;

public class GUIBuildManager {
	private ArrayList<GUIBuilder> builders_;
	private FrameworkWindow owner_;

	public GUIBuildManager(FrameworkWindow owner) {
		builders_ = new ArrayList<GUIBuilder>();
		owner_ = owner;
		/* Hardcode builders */
		//builders_.add(new AccountManagerPanel("All Accounts"));
		try {
			//builders_.add(new AgentViewPanel());
			builders_.add(new SchedulePanel());
			//builders_.add(new FinanceManagerPanel());
			builders_.add(new OfficeConfigPanel());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void build() {
		for (GUIBuilder b : builders_) {
			b.build(owner_);
		}
	}
}

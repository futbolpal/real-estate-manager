package reman.client.app.finance.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.noos.xing.mydoggy.AggregationPosition;
import org.noos.xing.mydoggy.Content;
import org.noos.xing.mydoggy.MultiSplitConstraint;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.content.MyDoggyMultiSplitContentManagerUI;
import org.xml.sax.SAXException;

import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.exceptions.FinanceException;
import reman.client.app.listeners.DboChangedListener;
import reman.client.gui.FrameworkWindow;
import reman.client.gui.GUIBuilder;
import reman.common.database.exceptions.DatabaseException;
import reman.common.messaging.DboChangedMessage;

public class FinanceManagerPanel extends MyDoggyToolWindowManager implements GUIBuilder,
		DboChangedListener {
	final JButton init_phase;

	public FinanceManagerPanel() {

		MyDoggyMultiSplitContentManagerUI splitUI = new MyDoggyMultiSplitContentManagerUI();
		this.getContentManager().setContentManagerUI(splitUI);

		init_phase = new JButton("Turn Off Initialization Phase");
		init_phase.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String msg = "";
					if (FinanceManager.instance().turnOffInitializationPhase()) {
						msg = "Success";
						init_phase.setEnabled(false);
					} else {
						msg = "Failure";
					}
					JOptionPane.showMessageDialog(null, msg);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (DatabaseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		JButton gen_statements = new JButton("Generate Statements");
		gen_statements.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String msg = "";
				try {
					FinanceManager.instance().getStatementManager().generateDefaultStatements(null);
					msg = "Success";
				} catch (FinanceException e1) {
					// TODO Auto-generated catch block
					msg = e1.getMessage();
					e1.printStackTrace();
				} catch (DatabaseException e1) {
					msg = e1.getMessage();
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SQLException e1) {
					msg = e1.getMessage();
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				JOptionPane.showMessageDialog(null, msg);
			}
		});

		JButton import_accts = new JButton("Import Accounts");
		import_accts.addActionListener(new ActionListener() {
			JFileChooser file_dialog;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (file_dialog == null) {
					file_dialog = new JFileChooser();
					if (file_dialog.showOpenDialog(FinanceManagerPanel.this) == JFileChooser.APPROVE_OPTION) {
						int accts_imported = 0;
						if (file_dialog.getSelectedFile() != null) {
							try {
								accts_imported = FinanceManager.instance().getAccountManager().importAccts(
										file_dialog.getSelectedFile().getAbsolutePath());
							} catch (SAXException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (DatabaseException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (SQLException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							JOptionPane.showMessageDialog(FinanceManagerPanel.this, "Imported a total of "
									+ accts_imported + " accounts.");
						}
					}
				} else {
				}
			}
		});

		JPanel content = new JPanel();
		content.add(init_phase);
		content.add(gen_statements);
		content.add(import_accts);

		Content finance_m = this.getContentManager().addContent("Finance Manager", "Finance Manager",
				null, content, "Finance engine controls.",
				new MultiSplitConstraint(AggregationPosition.BOTTOM));
		Content acct_m = this.getContentManager().addContent("Account Manager", "Account Manager",
				null, new AccountManagerPanel("All Accounts"),
				"View all Accounts in the financial system.",
				new MultiSplitConstraint(finance_m, AggregationPosition.LEFT));
		Content journal_m = this.getContentManager().addContent("Journal Manager", "Journal Manager",
				null, new JournalManagerPanel(),
				"View all Journals and Journal Entries in the financial system.",
				new MultiSplitConstraint(AggregationPosition.TOP));
		Content ledger_m = this.getContentManager().addContent("General Ledger", "General Ledger",
				null, new LedgerPanel(), "Select an account to view corresponding ledger.",
				new MultiSplitConstraint(journal_m, AggregationPosition.RIGHT));

		this.updateButtons();
	}

	private void updateButtons() {
		try {
			init_phase.setEnabled(FinanceManager.instance().isInitializationPhase());
		} catch (DatabaseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	@Override
	public void build(FrameworkWindow owner) {
		owner.registerContent("Finance Manager", "Finance Manager", null, this,
				"Entry point to the financial system.", true, new MultiSplitConstraint(
						AggregationPosition.BOTTOM));
	}

	@Override
	public void dboChangedEvent(DboChangedMessage m) {
		if (m.getDatabaseObject() instanceof FinanceManager) {
			this.updateButtons();
		}
	}
}

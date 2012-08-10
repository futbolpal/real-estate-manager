package reman.client.gui.statusbar;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.CustomDockableDescriptor;
import org.noos.xing.mydoggy.plaf.ui.util.StringUtil;
import org.noos.xing.mydoggy.plaf.ui.util.SwingUtil;

import reman.client.app.IconManager;

public class MemoryMonitorDockableDescriptor extends CustomDockableDescriptor {

	public MemoryMonitorDockableDescriptor(MyDoggyToolWindowManager manager, ToolWindowAnchor anchor) {
		super(manager, anchor);
	}

	public void updateRepresentativeAnchor() {
	}

	public JComponent getRepresentativeAnchor(Component parent) {
		if (representativeAnchor == null)
			representativeAnchor = new MemoryMonitorPanel(anchor);
		return representativeAnchor;
	}

	public class MemoryMonitorPanel extends JPanel {
		int sleepTime;

		public MemoryMonitorPanel(ToolWindowAnchor anchor) {
			sleepTime = 1000;

			final JProgressBar memoryUsage = new JProgressBar();
			memoryUsage.setStringPainted(true);

			JButton gc = new JButton(IconManager.instance().getIcon(16, "places", "user-trash.png"));
			gc.setBorderPainted(true);
			gc.setFocusable(false);
			gc.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			gc.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.gc();
				}
			});

			Thread memoryThread = new Thread(new Runnable() {
				public void run() {
					while (true) {
						String grabbed = StringUtil.bytes2MBytes(Runtime.getRuntime().totalMemory()
								- Runtime.getRuntime().freeMemory());
						String total = StringUtil.bytes2MBytes(Runtime.getRuntime().totalMemory());

						memoryUsage.setMaximum(Integer.parseInt(total));
						memoryUsage.setValue(Integer.parseInt(grabbed));

						memoryUsage.setString(grabbed + " MB of " + total + " MB");
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
						}
					}
				}
			});
			memoryThread.setDaemon(true);
			memoryThread.setPriority(Thread.MIN_PRIORITY);
			memoryThread.start();

			switch (anchor) {
			case BOTTOM:
			case TOP:
				memoryUsage.setOrientation(SwingConstants.HORIZONTAL);
				setLayout(new TableLayout(new double[][] { { 120, 1, 17 }, { -1 } }));
				add(memoryUsage, "0,0,FULL,FULL");
				add(gc, "2,0,FULL,FULL");
				break;
			case LEFT:
				memoryUsage.setOrientation(SwingConstants.VERTICAL);
				setLayout(new TableLayout(new double[][] { { -1 }, { 120, 1, 17 } }));
				add(memoryUsage, "0,0,FULL,FULL");
				add(gc, "0,2,FULL,FULL");
				break;
			case RIGHT:
				memoryUsage.setOrientation(SwingConstants.VERTICAL);
				setLayout(new TableLayout(new double[][] { { -1 }, { 17, 1, 120 } }));
				add(gc, "0,0,FULL,FULL");
				add(memoryUsage, "0,2,FULL,FULL");
				break;
			}

			registerDragGesture(memoryUsage);
			registerDragGesture(gc);
			registerDragGesture(this);
		}

		public void setSleepTime(int sleepTime) {
			this.sleepTime = sleepTime;
		}

	}

}

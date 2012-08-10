package reman.client.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * This class provides a generic animation to fade a border from one color to fade out.  
 * @author jonathan
 *
 */
public class BorderFadeAnimation {
	private static final int STEP_COUNT = 10;
	private Color start_;
	private JComponent c_;
	private int step_count_;
	private Timer timer_;
	private AnimationListener listener_;

	public BorderFadeAnimation(JComponent comp, AnimationListener a, Color c) throws Exception {
		listener_ = a;
		start_ = c;
		c_ = comp;
		step_count_ = 0;
		timer_ = new Timer(100, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (step_count_ > STEP_COUNT) {
					BorderFadeAnimation.this.stop();
				}

				/* Transistion Color */
				Color bg = ((LineBorder) c_.getBorder()).getLineColor();
				int alpha = Math.max(bg.getAlpha() - 25, 0);
				Color trans = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), alpha);
				c_.setBorder(new LineBorder(trans, 2));

				step_count_++;
			}
		});
	}

	/**
	 * Set the start color of the border.
	 * @param c - Color to start with.
	 */
	public void setStartColor(Color c) {
		start_ = c;
	}

	/**
	 * Runs the fade animation
	 */
	public void start() {
		if (timer_.isRunning()) {
			stop();
		}
		c_.setBorder(new LineBorder(start_, 2));
		timer_.start();
	}

	public void stop() {
		c_.setBorder(new EmptyBorder(1, 1, 1, 1));
		step_count_ = 0;
		timer_.stop();
		listener_.animationDone();
	}
}
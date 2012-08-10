package reman.client.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.JPanel;

public class GradientPanel extends JPanel {
	public static enum Direction {
		HORIZONTAL, VERTICAL;
	}

	private Color color1_, color2_;
	private Direction direction_;

	public GradientPanel() {
		this(Color.black, Color.white, Direction.VERTICAL);
	}

	public GradientPanel(Color a, Color b, Direction d) {
		color1_ = a;
		color2_ = b;
		direction_ = d;
	}

	public void setDirection(Direction d) {
		direction_ = d;
	}

	public void setColors(Color a, Color b) {
		color1_ = a;
		color2_ = b;
		this.repaint();
	}

	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		super.paint(g2d);

		int w = getWidth();
		int h = getHeight();
		Insets i = this.getInsets();

		//	 Paint a gradient from top to bottom
		GradientPaint gp;
		if (direction_ == Direction.HORIZONTAL) {
			gp = new GradientPaint(0, 0, color1_, 0, w, color2_);
		} else {
			gp = new GradientPaint(0, 0, color1_, 0, h, color2_);
		}

		g2d.setPaint(gp);
		g2d.fillRect(i.left, i.top, w - i.left - i.right, h - i.top - i.bottom);
		super.paintChildren(g2d);
	}
}

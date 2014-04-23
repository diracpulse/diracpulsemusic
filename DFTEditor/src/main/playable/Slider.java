
package main.playable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Slider {
	
	private double minValue;
	private double maxValue;
	private Rectangle bounds;
	private boolean vertical;
	private double currentValue;
	
	public Slider(double minValue, double maxValue, Rectangle bounds) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.bounds = bounds;
		if(bounds.height > bounds.width) {
			vertical = true;
		} else {
			vertical = false;
		}
		this.currentValue = minValue;
	}
	
	public double getCurrentValue() {
		return currentValue;
	}
	
	public double getCurrentValuePow2() {
		return Math.pow(2.0, currentValue);
	}
	
	public void pointSelected(int x, int y) {
		if(bounds.contains(x, y)) {
			if(vertical) {
				double ratio = (y - bounds.y) / (double) bounds.height;
				currentValue = (1.0 - ratio) * (maxValue - minValue) + minValue;
			} else {
				double ratio = (x - bounds.x) / (double) bounds.width;
				currentValue = ratio * (maxValue - minValue) + minValue;
			}
		}
	}
	
	public void draw(Graphics2D g2) {
		g2.setColor(Color.BLACK);
		g2.fill(bounds);
		g2.setColor(Color.WHITE);
		g2.draw(bounds);
		double ratio = (currentValue - minValue) / (maxValue - minValue);
		if(vertical) {
			int y = (int) Math.round((1.0 - ratio) * bounds.height + bounds.y);
			g2.drawLine(bounds.x, y, bounds.x + bounds.width, y);
		} else {
			int x = (int) Math.round(ratio * bounds.height + bounds.y);
			g2.drawLine(x, bounds.y, x, bounds.y + bounds.height);
		}
	}
	
}
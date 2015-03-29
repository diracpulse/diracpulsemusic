
package main.playable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Slider {
	
	public enum Type {
		LINEAR,
		LOGARITHMIC;
	}
	
	private Type type;
	private double minValue;
	private double maxValue;
	private int screenX;
	private int screenY;
	private int range;
	private Rectangle sliderBounds;
	private int sliderPosition = 0;
	private int sliderWidth = 10;
	private int xPadding = 16;
	private int yPadding = 24;
	private String descriptor;
	
	public Slider(Type type, int screenX, int screenY, int range, double minValue, double maxValue, double initialValue, String descriptor) {
		this.type = type;
		this.screenX = screenX;
		this.screenY = screenY;
		this.range = range;
		this.minValue = minValue;
		this.maxValue = maxValue;
		int x = screenX + xPadding;
		int y = screenY + yPadding;
		int w = sliderWidth;
		int l = range;
		this.sliderBounds = new Rectangle(x, y, w, l);
		setCurrentValue(initialValue);
		this.descriptor = descriptor;
	}
	
	public Rectangle getBounds() {
		return new Rectangle(screenX, screenY, sliderWidth + xPadding * 2, range + yPadding * 2);
	}
	
	public int getMaxX() {
		return screenX + sliderWidth + xPadding * 2;
	}
	
	public double getCurrentValue() {
		double ratio = (1.0 - (double) sliderPosition / (double) range);
		switch(type) {
			case LINEAR:
				return ratio * (maxValue - minValue) + minValue;
			case LOGARITHMIC:
				double range = Math.log(maxValue) - Math.log(minValue);
				double logValue = ratio * range;
				return Math.exp(logValue + Math.log(minValue));
			}
		return 0.0;
	}
	
	public void setCurrentValue(double value) {
		switch(type) {
			case LINEAR:
				double ratio = (value - minValue) / (maxValue - minValue);
				sliderPosition = (int) (Math.round(1.0 - ratio) * range);
				return;
			case LOGARITHMIC:
				double logValue = Math.log(value) - Math.log(minValue);
				double logRange =  Math.log(maxValue) - Math.log(minValue);
				sliderPosition = (int) Math.round((1.0 - (logValue / logRange)) * range);
				return;
		}
	}
	
	public void pointSelected(int x, int y) {
		if(sliderBounds.contains(x, y)) {
			sliderPosition = y - sliderBounds.y;
			return;
		}
	}
	
	public void draw(Graphics2D g2) {
		g2.setColor(Color.BLACK);
		g2.fill(sliderBounds);
		g2.setColor(Color.WHITE);
		g2.draw(sliderBounds);
		g2.setColor(Color.RED);
		int currentPosition = sliderPosition + sliderBounds.y;
		g2.drawLine(sliderBounds.x, currentPosition, sliderBounds.x + sliderBounds.width, currentPosition);
		g2.setColor(Color.GRAY);
		//g2.drawRect(sliderBounds.x - 2, currentPosition - 2, range + xPad, 4);
		// get metrics from the graphics
		Font font = g2.getFont();
		FontMetrics metrics = g2.getFontMetrics(font);
		// get the height of a line of text in this
		// font and render context
		int hgt = metrics.getHeight();
		// get the advance of my text in this font
		// and render context
		int adv = metrics.stringWidth(descriptor);
		// calculate the size of a box to hold the
		// text with some padding.
		g2.setColor(Color.WHITE);
		int textYOffset = (yPadding - hgt) / 2;
		int textXOffset = ((xPadding * 2 + sliderWidth) - adv) / 2;
		g2.drawString(descriptor, screenX + textXOffset, screenY + textYOffset);
	}
	
}
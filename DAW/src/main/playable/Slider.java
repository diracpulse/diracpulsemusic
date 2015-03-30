
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
	private int xPadding = 24;
	private int yPadding = 16;
	private String descriptor;
	private String upperLable;
	private String lowerLable;
	
	public Slider(Type type, int screenX, int screenY, int range, double minValue, double maxValue, double initialValue, String[] text) {
		this.type = type;
		this.screenX = screenX;
		this.screenY = screenY;
		this.range = range;
		this.minValue = minValue;
		this.maxValue = maxValue;
		int x = screenX + xPadding;
		int y = screenY + yPadding * 2;
		int w = sliderWidth;
		int l = range;
		this.sliderBounds = new Rectangle(x, y, w, l);
		setCurrentValue(initialValue);
		this.descriptor = text[0];
		this.upperLable = text[1];
		this.lowerLable = text[2];
	}
	
	public Rectangle getBounds() {
		return new Rectangle(screenX, screenY, sliderWidth + xPadding * 2, range + yPadding * 2);
	}
	
	public int getMaxX() {
		return screenX + sliderWidth + xPadding * 2;
	}
	
	public int getMaxY() {
		return screenY + range + yPadding * 3;
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
		g2.fillRect(sliderBounds.x + sliderBounds.width / 2 - 1, sliderBounds.y, 2, range);
		g2.setColor(Color.GRAY);
		int numMarkings = 10;
		for(int index = 0; index <= numMarkings; index++) {
			int markingY = (int) Math.round(sliderBounds.y + ((float) range / numMarkings) * index);
			g2.drawLine(sliderBounds.x, markingY, sliderBounds.x + sliderBounds.width, markingY); 
		}
		g2.setColor(Color.RED);
		int currentPosition = sliderPosition + sliderBounds.y;
		g2.fillRect(sliderBounds.x - 2, currentPosition - 3, sliderBounds.width + 4, 6);
		g2.setColor(Color.WHITE);
		g2.drawRect(sliderBounds.x - 2, currentPosition - 3, sliderBounds.width + 4, 6);
		// Draw Parameter String
		g2.setFont(new Font("TRUETYPE_FONT", Font.BOLD, 12));
		Font font = g2.getFont();
		FontMetrics metrics = g2.getFontMetrics(font);
		int hgt = metrics.getHeight();
		int adv = metrics.stringWidth(descriptor);
		g2.setColor(Color.WHITE);
		int textYOffset = (yPadding - hgt) / 2 + yPadding / 2;
		int textXOffset = ((xPadding * 2 + sliderWidth) - adv) / 2;
		g2.drawString(descriptor, screenX + textXOffset, screenY + textYOffset);
		// Draw Parameter Values
		g2.setFont(new Font("TRUETYPE_FONT", Font.PLAIN, 12));
		font = g2.getFont();
		metrics = g2.getFontMetrics(font);
		hgt = metrics.getHeight();
		adv = metrics.stringWidth(upperLable);
		textYOffset += yPadding;
		textXOffset = ((xPadding * 2 + sliderWidth) - adv) / 2;
		g2.drawString(upperLable, screenX + textXOffset, screenY + textYOffset);
		adv = metrics.stringWidth(lowerLable);
		textYOffset += range + yPadding * 1.5;
		textXOffset = ((xPadding * 2 + sliderWidth) - adv) / 2;
		g2.drawString(lowerLable, screenX + textXOffset, screenY + textYOffset);
	}
	
}

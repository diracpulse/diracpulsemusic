
package main.playable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

public class Slider {
	
	public static enum Serial {
		FM_TUNE,
		FM_FINE,
		FM_ENV,
		FM_LFO,
		FM_SH,
		AMP_ENV,
		AMP_LFO,
		AMP_SH,
		PWM_ENV,
		PWM_LFO,
		PWM_SH,
		PWM_AMT,
		SH_RATE,
		SH_NOISE,
		SH_LFO,
		LFO_FREQ,
		LFO_SHAPE,
		ENV_A,
		ENV_D,
		ENV_S,
		ENV_R;
	}
	
	public static final Double[] ratios = {1.0, 3.0 / 2.0, 4.0 / 3.0, 5.0 / 3.0, 5.0 / 4.0, 7.0 / 4.0, 6.0 / 5.0, 7.0 / 5.0, 8.0 / 5.0, 9.0 / 5.0, Math.sqrt(2.0), Math.sqrt(3.0), Math.sqrt(5.0) / 2.0, Math.sqrt(7.0) / 2.0};
	TreeSet<Double> sorted = new TreeSet<Double>();
	ArrayList<Double> sortedArray = new ArrayList<Double>();
	
	public enum Type {
		LINEAR,
		LOGARITHMIC,
		LOGARITHMIC_ZERO,
		LOGDIV2;
	}
	
	private Type type;
	private double minValue;
	private double maxValue;
	private int screenX;
	private int screenY;
	private int range = 53 * 8;
	private Rectangle sliderBounds;
	private int sliderPosition = 0;
	private int sliderWidth = 10;
	private int xPadding = 10;
	private int yPadding = 16;
	private String descriptor;
	private String upperLable;
	private String lowerLable;
	private double[] positionToValue;
	private Random random;
	
	public Slider(Type type, int screenX, int screenY, double minValue, double maxValue, double initialValue, String[] text) {
		for(Double ratio: ratios) {
			sorted.add(ratio);
		}
		sortedArray.addAll(sorted);
		this.type = type;
		this.screenX = screenX;
		this.screenY = screenY;
		this.minValue = minValue;
		this.maxValue = maxValue;
		int x = screenX + xPadding;
		int y = screenY + yPadding * 3;
		int w = sliderWidth;
		int l = range;
		this.sliderBounds = new Rectangle(x, y, w, l);
		setCurrentValue(initialValue);
		this.descriptor = text[0];
		this.upperLable = text[1];
		this.lowerLable = text[2];
		positionToValue = new double[range + 1];
		for(int position = 0; position <= range; position++) {
			positionToValue[position] = getCurrentValue(position);
		}
		random = new Random();
	}
	
	public Rectangle getBounds() {
		return new Rectangle(screenX, screenY, sliderWidth + xPadding * 2, range + yPadding * 2);
	}
	
	public int getMaxX() {
		return screenX + sliderWidth + xPadding * 2;
	}
	
	public int getMaxY() {
		return screenY + range + yPadding * 4;
	}
	
	public double getCurrentValue() {
		return positionToValue[sliderPosition];
	}
	
	private double getCurrentValue(int sliderPositionVal) {
		double dRange = (double) range;
		double ratio = (1.0 - (double) sliderPositionVal / (double) dRange);
		double logValue = 0.0;
		switch(type) {
			case LINEAR:
				return ratio * (maxValue - minValue) + minValue;
			case LOGARITHMIC:
				dRange = Math.log(maxValue) - Math.log(minValue);
				logValue = ratio * dRange;
				return Math.exp(logValue + Math.log(minValue));
			case LOGARITHMIC_ZERO:
				if(ratio == 0.0) return 0.0;
				dRange = Math.log(maxValue) - Math.log(minValue);
				logValue = ratio * dRange;
				return Math.exp(logValue + Math.log(minValue));
			case LOGDIV2:
				double sliderVal = Math.floor(sliderPositionVal / 2.0) * 2.0;
				ratio = (1.0 - (double) sliderVal / dRange);
				dRange = Math.log(maxValue) - Math.log(minValue);
				logValue = ratio * dRange;
				return Math.exp(logValue + Math.log(minValue));
			}
		return 0.0;
	}
	
	public void setCurrentValue(double value) {
		double logValue = 0.0;
		double logRange = 0.0;
		switch(type) {
			case LINEAR:
				double ratio = (value - minValue) / (maxValue - minValue);
				sliderPosition = (int) (Math.round((1.0 - ratio) * range));
				return;
			case LOGARITHMIC:
				logValue = Math.log(value) - Math.log(minValue);
				logRange =  Math.log(maxValue) - Math.log(minValue);
				sliderPosition = (int) Math.round((1.0 - (logValue / logRange)) * range);
				return;
			case LOGARITHMIC_ZERO:
				if(value == 0.0) {
					sliderPosition = range;
					return;
				}
				logValue = Math.log(value) - Math.log(minValue);
				logRange =  Math.log(maxValue) - Math.log(minValue);
				sliderPosition = (int) Math.round((1.0 - (logValue / logRange)) * range);
				return;
			case LOGDIV2:
				logValue = Math.log(value) - Math.log(minValue);
				logRange =  Math.log(maxValue) - Math.log(minValue);
				sliderPosition = (int) Math.round((1.0 - (logValue / logRange)) * range);
				return;
		}
	}
	
	public void setRandomValue() {
		sliderPosition = random.nextInt(range);
	}
	
	public void pointSelected(int x, int y, PlayableController.ClickInfo info) {
		if(sliderBounds.contains(x, y)) {
			sliderPosition = y - sliderBounds.y;
			return;
		}
		if(sliderBounds.contains(x, y - 8)) {
			sliderPosition = range;
			return;
		}
		if(sliderBounds.contains(x, y + 8)) {
			sliderPosition = 0;
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
		g2.setFont(new Font("TRUETYPE_FONT", Font.BOLD, 10));
		Font font = g2.getFont();
		FontMetrics metrics = g2.getFontMetrics(font);
		int hgt = metrics.getHeight();
		int adv = metrics.stringWidth(descriptor);
		g2.setColor(Color.WHITE);
		int textYOffset = (yPadding - hgt) / 2 + yPadding / 2;
		int textXOffset = ((xPadding * 2 + sliderWidth) - adv) / 2;
		g2.drawString(descriptor, screenX + textXOffset, screenY + textYOffset);
		// Draw Parameter Values
		g2.setFont(new Font("TRUETYPE_FONT", Font.PLAIN, 10));
		font = g2.getFont();
		metrics = g2.getFontMetrics(font);
		hgt = metrics.getHeight();
		String currentValueString = displayCurrentValue();
		adv = metrics.stringWidth(currentValueString);
		textYOffset += yPadding;
		textXOffset = ((xPadding * 2 + sliderWidth) - adv) / 2;
		g2.drawString(currentValueString, screenX + textXOffset, screenY + textYOffset);
		adv = metrics.stringWidth(upperLable);
		textYOffset += yPadding;
		textXOffset = ((xPadding * 2 + sliderWidth) - adv) / 2;
		g2.drawString(upperLable, screenX + textXOffset, screenY + textYOffset);
		adv = metrics.stringWidth(lowerLable);
		textYOffset += range + yPadding * 1.5;
		textXOffset = ((xPadding * 2 + sliderWidth) - adv) / 2;
		g2.drawString(lowerLable, screenX + textXOffset, screenY + textYOffset);
	}
	
	private String displayCurrentValue() {
		float currentValue = (float) getCurrentValue();
		if(currentValue >= 1000.0) return new Integer((int) Math.round(currentValue)).toString();
		if(currentValue >= 100.0) {
			int intVal = (int) Math.floor(currentValue);
			float floatVal = Math.round((currentValue - intVal) * 10.0f) / 10.0f;
			return new Float(intVal + floatVal).toString();
		}
		if(currentValue >= 10.0) {
			int intVal = (int) Math.floor(currentValue);
			float floatVal = Math.round((currentValue - intVal) * 100.0f) / 100.0f;
			return new Float(intVal + floatVal).toString();
		}
		return new Float(Math.round(currentValue * 1000.0) / 1000.0).toString();
	}
	
}

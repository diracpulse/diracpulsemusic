package main.playable;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JOptionPane;

import main.Module;
import main.Module.ModuleType;
import main.Filter;
import main.ModuleEditor;
import main.MultiWindow;
import main.SynthTools;
import main.playable.Slider.Type;

public class PlayableFilter implements PlayableModule {
	
	PlayableEditor parent;
	double prevCutoff = 0.0;
	double maxCutoff = 20000;
	double minCutoff = 64;
	double cutoffMod = 8.0;
	double maxCutoffHP = 1000;
	double minCutoffHP = 20;
	double cutoffModHP = 8.0;
	private FilterType type;
	private Slider cutoffControl;
	private Slider resControl;
	private int maxScreenX;
	String moduleName;
	private int screenX;
	private int screenY;
	private int yPadding = PlayableEditor.moduleYPadding;
	
	// FIR FilterState
	double[] samples;
	double[] filter;
	
	// Filter State
	double[] y = {0.0, 0.0, 0.0}; 
	double[] input = {0.0, 0.0, 0.0};
	double[] y2 = {0.0, 0.0, 0.0}; 
	double[] input2 = {0.0, 0.0, 0.0};

	public enum FilterType {
		LOWPASS,
		HIGHPASS;
	}
	
	public PlayableFilter(PlayableEditor parent, FilterType type, int screenX, int screenY, String moduleName) {
		this.parent = parent;
		int x = screenX;
		int y = screenY + PlayableEditor.moduleYPadding;
		this.screenX = x;
		this.screenY = screenY;
		this.moduleName = moduleName;
		this.type = type;
		switch(type) {
		case LOWPASS:
			cutoffControl = new Slider(Slider.Type.LOGARITHMIC, x, y, minCutoff, maxCutoff, 256.0, new String[] {"FREQ", "Hz", " "});
			x = cutoffControl.getMaxX();
			resControl = new Slider(Slider.Type.LOGARITHMIC, x, y, 0.25, 4.0, Math.sqrt(2.0) / 2.0, new String[] {"RES", " ", " "});
			maxScreenX = resControl.getMaxX();
			initLowpassFIR();
			return;
		case HIGHPASS:
			cutoffControl = new Slider(Slider.Type.LOGARITHMIC, x, y, minCutoffHP, maxCutoffHP, minCutoffHP, new String[] {"FREQ", " ", " "});
			maxScreenX = cutoffControl.getMaxX();
		}
		initLowpassFIR();
	}
	
	public void reset() {
		for(int index = 0; index < 3; index++) {
			y[index] = 0.0;
			input[index] = 0.0;
			y2[index] = 0.0; 
			input2[index] = 0.0;
		}
	}

	public int getMaxScreenX() {
		return maxScreenX;
	}

	public double getSample(double sample, double freqRatio, double input) {
		switch(type) {
		case LOWPASS:
			if(sample > 1.0) sample = 1.0;
			if(sample < -1.0) sample = -1.0;
			double returnVal = variableQLowpass2(sample, freqRatio, input, 0.0);
			return variableQLowpass2(returnVal, freqRatio, input, 0.0);
			//return lowpassFIR(sample);
		case HIGHPASS:
			return butterworthHighpass2(sample, freqRatio, input, 0.0);
		}
		return 0.0;
	}
	
	public synchronized double variableQLowpass2(double sample, double freqRatio, double fControl, double qControl) {
		if(sample > 1.0) sample = 1.0;
		if(sample < -1.0) sample = -1.0;
		input2[0] = input2[1];
		input2[1] = input2[2];
		input2[2] = sample;
		y2[0] = y2[1];
		y2[1] = y2[2];
		double f = cutoffControl.getCurrentValue() * (1.0 + fControl * cutoffMod);
		if(f > maxCutoff) f = maxCutoff;
		if(f < minCutoff) f = minCutoff;
		double q = resControl.getCurrentValue() * (1.0 + qControl);
		double g = Math.tan((Math.PI * f) / AudioFetcher.sampleRate);
		double D = q * g * g + g + q;
		double b0 = q * (g * g) / D;
		double b1 = 2.0 * b0;
		double b2 = b0;
		double a0 = 2.0 * q * (g * g - 1.0) / D;
		double a1 = (q * g * g - g + q) / D;
		y2[2] = b0 * input2[2] + b1 * input2[1] + b2 * input2[0] - a0 * y2[1] - a1 * y2[0];
		//if(y2[1] > 1.0) y2[1] = 1.0;
		//if(y2[1] < -1.0) y2[1] = -1.0;
		return y2[2];
	}
	
	private double butterworthHighpass2(double sample, double freqRatio, double fControl, double qControl) {
		if(sample > 1.0) sample = 1.0;
		if(sample < -1.0) sample = -1.0;
		input[0] = input[1];
		input[1] = input[2];
		input[2] = sample;
		y[0] = y[1];
		y[1] = y[2];
		double f = cutoffControl.getCurrentValue() * ((1.0 + fControl * cutoffMod));
		if(f > maxCutoff) f = maxCutoff;
		if(f < minCutoff) f = minCutoff;
		double gamma = Math.tan((Math.PI * f) / SynthTools.sampleRate);
		double D = gamma * gamma + Math.sqrt(2.0) * gamma + 1.0;
		double b0 = 1.0 / D;
		double b1 = -2.0 / D;
		double b2 = 1.0 / D;
		double a0 = 2.0 * (gamma * gamma - 1.0) / D;
		double a1 = (gamma * gamma - Math.sqrt(2.0) * gamma + 1.0) / D;
		y[2] = b0 * input[2] + b1 * input[1] + b2 * input[0] - a0 * y[1] - a1 * y[0];
		if(y2[1] > 1.0) y2[1] = 1.0;
		if(y2[1] < -1.0) y2[1] = -1.0;
		return y[2];
	}

	public double lowpassFIR(double sample) {
	   	double bins = 1.0;
		double alpha = 5.0;
		double samplesPerCycle = SynthTools.sampleRate / cutoffControl.getCurrentValue();
		int filterLength = (int) Math.round(bins * samplesPerCycle);
		filterLength += filterLength % 2;
		filter = Filter.getLPFilter(cutoffControl.getCurrentValue(), filterLength, alpha);
		double returnVal = 0.0;
		for(int index = 1; index < samples.length; index++) {
			samples[index - 1] = samples[index];
		}
		samples[samples.length - 1] = sample;
		int startIndex = (samples.length - filter.length) / 2;
		for(int filterIndex = 0; filterIndex < filter.length - 1; filterIndex++) {
			returnVal += samples[startIndex + filterIndex] * filter[filterIndex];
		}
		return returnVal;
	}
	
	private void initLowpassFIR() {
		double freqInHz = cutoffControl.getCurrentValue();
	   	double bins = 1.0;
		double alpha = 5.0;
		double samplesPerCycle = SynthTools.sampleRate / freqInHz;
		int filterLength = (int) Math.round(bins * samplesPerCycle);
		filterLength += filterLength % 2;
		samples = new double[filterLength];
	}

	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g; 
		g2.setFont(new Font("TRUETYPE_FONT", Font.BOLD, 10));
		Font font = g2.getFont();
		FontMetrics metrics = g2.getFontMetrics(font);
		int hgt = metrics.getHeight();
		int adv = metrics.stringWidth(moduleName);
		g2.setColor(Color.WHITE);
		int textYOffset = (yPadding - hgt) / 2 + yPadding / 2;
		int textXOffset = (maxScreenX - screenX - adv) / 2;
		g2.setColor(new Color(0.5f, 0.0f, 0.0f));
		g2.fillRect(screenX + textXOffset - 4, screenY, maxScreenX - screenX - textXOffset * 2 + 8, yPadding - 4);
		g2.setColor(Color.WHITE);
		g2.drawString(moduleName, screenX + textXOffset, screenY + textYOffset);
		g2.setColor(Color.BLUE);
		g2.drawRect(screenX, screenY, maxScreenX - screenX, cutoffControl.getMaxY());
		cutoffControl.draw(g2);
		if(type == FilterType.LOWPASS) resControl.draw(g2);
	}

	public void pointSelected(int x, int y, PlayableController.ClickInfo info) {
		cutoffControl.pointSelected(x, y, info);
		if(type == FilterType.LOWPASS) resControl.pointSelected(x, y, info);
		parent.view.repaint();
	}
	
	public void loadModuleInfo(BufferedReader in) {
		try {
			cutoffControl.setCurrentValue(new Double(in.readLine()));
			if(type == FilterType.LOWPASS) resControl.setCurrentValue(new Double(in.readLine()));
		} catch (Exception e) {
			System.out.println("PlayableFilter.loadModuleInfo: Error reading from file");
		}
	}

	@Override
	public void saveModuleInfo(BufferedWriter out) {
		try {
			out.write(new Double(cutoffControl.getCurrentValue()).toString());
			out.newLine();
			if(type == FilterType.LOWPASS) {
				out.write(new Double(resControl.getCurrentValue()).toString());
				out.newLine();
			}
		} catch (Exception e) {
			System.out.println("PlayableFilter.saveModuleInfo: Error reading from file");
		}
	}
	
	
	
}

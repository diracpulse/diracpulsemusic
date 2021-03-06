package main.playable;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
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
	double minLogFreq = 5.0;
	double maxLogFreq = 14.0;
	double minLogAmp = -16.0;
	double maxLogAmp = 0.0;
	double prevCutoff = 0.0;
	double prevRes = 7;
	private Slider cutoffControl;
	private Slider resControl;
	private int maxScreenX;
	
	// Filter State
	double[] y2 = {0.0, 0.0, 0.0}; 
	double[] input2 = {0.0, 0.0, 0.0};
	
	public enum WaveformType {
		SINE,
		TRIANGLE,
		SQUAREWAVE,
		SAWTOOTH,
	}

	private WaveformType type = WaveformType.SINE;
	private double currentPhase = 0.0;
	
	public PlayableFilter(PlayableEditor parent, int screenX, int screenY) {
		this.parent = parent;
		int x = screenX;
		int y = screenY;
		cutoffControl = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, 256.0, 8096.0, 1024.0, "Cutoff");
		x = cutoffControl.getMaxX();
		resControl = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, 0.25, 2.0, Math.sqrt(2.0) / 2.0, "Resonance");
		maxScreenX = resControl.getMaxX();
	}
	
	public int getMaxScreenX() {
		return maxScreenX;
	}
	
	public synchronized void reset() {
		currentPhase = 0.0;
	}
	

	public double masterGetSample(double sample) {
		return variableQLowpass2(sample, 0.0, 0.0);
	}
	
	public double variableQLowpass2(double sample, double fControl, double qControl) {
		input2[0] = input2[1];
		input2[1] = input2[2];
		input2[2] = sample;
		y2[0] = y2[1];
		y2[1] = y2[2];
		double f = cutoffControl.getCurrentValue() * (1.0 + fControl);
		double q = resControl.getCurrentValue() * (1.0 + qControl);
		double g = Math.tan((Math.PI * f) / AudioFetcher.sampleRate);
		double D = q * g * g + g + q;
		double b0 = q * (g * g) / D;
		double b1 = 2.0 * b0;
		double b2 = b0;
		double a0 = 2.0 * q * (g * g - 1.0) / D;
		double a1 = (q * g * g - g + q) / D;
		y2[2] = b0 * input2[2] + b1 * input2[1] + b2 * input2[0] - a0 * y2[1] - a1 * y2[0];
		return y2[2];
	}

	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g; 
		cutoffControl.draw(g2);
		resControl.draw(g2);
	}

	public void pointSelected(int x, int y) {
		cutoffControl.pointSelected(x, y);
		resControl.pointSelected(x, y);
		parent.view.repaint();
	}
}

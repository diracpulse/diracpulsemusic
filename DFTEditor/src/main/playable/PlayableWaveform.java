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
import main.ModuleEditor;
import main.MultiWindow;
import main.SynthTools;

public class PlayableWaveform {
	
	PlayableWaveformEditor parent;
	double minLogFreq = 5.0;
	double maxLogFreq = 14.0;
	double minLogAmp = -16.0;
	double maxLogAmp = 0.0;
	double prevAmp = 0.0;
	double prevFreq = 7;
	private Slider freqControl;
	private Slider ampControl;
	
	public enum WaveformType {
		SINE,
		TRIANGLE,
		SQUAREWAVE,
		SAWTOOTH,
	}

	private WaveformType type = WaveformType.SINE;
	private double currentPhase = 0.0;
	
	public PlayableWaveform(PlayableWaveformEditor parent) {
		this.parent = parent;
		freqControl = new Slider(minLogFreq, maxLogFreq, new Rectangle(4, 4, 16, 800));
		ampControl = new Slider(minLogAmp, maxLogAmp, new Rectangle(24, 4, 16, 800));
	}
	
	public synchronized void reset() {
		currentPhase = 0.0;
	}
	

	public double[] masterGetSamples(int numSamples) {
		double currentAmp = prevAmp;
		double currentFreq = prevFreq;
		double endAmp;
		double endFreq;
		synchronized(this) {
			endAmp =  ampControl.getCurrentValuePow2();
			endFreq = freqControl.getCurrentValuePow2();
		}
		double deltaAmp = (endAmp - prevAmp) / numSamples;
		double deltaFreq = (endFreq - prevFreq) / numSamples;
		double[] returnVal = new double[numSamples];
		switch(type) {
			case SINE:
				for(int index = 0; index < returnVal.length; index++) {
					returnVal[index] += Math.sin(currentPhase) * currentAmp;
					currentPhase += currentFreq / SynthTools.sampleRate * Math.PI * 2.0;
					currentAmp += deltaAmp;
					currentFreq += deltaFreq;
				}
				break;
			case SQUAREWAVE:
				for(int index = 0; index < returnVal.length; index++) {
					returnVal[index] += squarewave(currentPhase) * ampControl.getCurrentValuePow2();
					currentPhase += freqControl.getCurrentValuePow2() / SynthTools.sampleRate * Math.PI * 2.0;
				}
				break;
			case TRIANGLE:
				for(int index = 0; index < returnVal.length; index++) {
					returnVal[index] += triangle(currentPhase) * ampControl.getCurrentValuePow2();
					currentPhase += freqControl.getCurrentValuePow2() / SynthTools.sampleRate * Math.PI * 2.0;
				}
				break;
			case SAWTOOTH:
				for(int index = 0; index < returnVal.length; index++) {
					returnVal[index] += sawtooth(currentPhase) * ampControl.getCurrentValuePow2();
					currentPhase += freqControl.getCurrentValuePow2() / SynthTools.sampleRate * Math.PI * 2.0;
				}
				break;
			}
		synchronized(this) {
			prevAmp = endAmp;
			prevFreq = endFreq;
		}
		return returnVal;
	}

	public double sawtooth(double phase) {
		phase -= Math.floor(phase / (Math.PI * 2.0)) * Math.PI * 2.0;
		if(phase < Math.PI) return phase / Math.PI;
		if(phase > Math.PI) return -1.0 + (phase - Math.PI) / Math.PI;
		return Math.random() * 2.0 - 1.0; // phase == Math.PI
	}

	public double squarewave(double phase) {
		phase -= Math.floor(phase / (Math.PI * 2.0)) * Math.PI * 2.0;
		if(phase < Math.PI) return 1.0;
		if(phase > Math.PI) return -1.0;
		return Math.random() * 2.0 - 1.0; // phase == Math.PI
	}
	
	public double triangle(double phase) {
		phase -= Math.floor(phase / (Math.PI * 2.0)) * Math.PI * 2.0;
		if(phase < Math.PI / 2.0) return phase / (Math.PI / 2.0);
		if(phase < Math.PI * 1.5) return 1.0 - (phase - Math.PI / 2.0) / (Math.PI / 2.0);
		return -1.0 + (phase - Math.PI * 1.5) / (Math.PI / 2.0);
	}

	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g; 
		freqControl.draw(g2);
		ampControl.draw(g2);
	}

	public void pointSelected(int x, int y) {
		freqControl.pointSelected(x, y);
		ampControl.pointSelected(x, y);
		parent.view.repaint();
	}
}

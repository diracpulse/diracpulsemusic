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
	double minLogAmp = 0.0;
	double maxLogAmp = Math.log(Short.MAX_VALUE) / Math.log(2.0);
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
	
	public void reset() {
		currentPhase = 0.0;
	}

	public double[] masterGetSamples(double[] control) {
		double[] returnVal = new double[control.length];
		switch(type) {
			case SINE:
				for(int index = 0; index < returnVal.length; index++) {
					if(control[index] == -1) {
						currentPhase = 0.0;
						continue;
					}
					returnVal[index] += Math.sin(currentPhase) * ampControl.getCurrentValuePow2();
					currentPhase += freqControl.getCurrentValuePow2() / SynthTools.sampleRate * Math.PI * 2.0;
				}
				break;
			case SQUAREWAVE:
				for(int index = 0; index < returnVal.length; index++) {
					if(control[index] == -1) {
						currentPhase = 0.0;
						continue;
					}
					returnVal[index] += squarewave(currentPhase) * ampControl.getCurrentValuePow2();
					currentPhase += freqControl.getCurrentValuePow2() / SynthTools.sampleRate * Math.PI * 2.0;
				}
				break;
			case TRIANGLE:
				for(int index = 0; index < returnVal.length; index++) {
					if(control[index] == -1) {
						currentPhase = 0.0;
						continue;
					}
					returnVal[index] += triangle(currentPhase) * ampControl.getCurrentValuePow2();
					currentPhase += freqControl.getCurrentValuePow2() / SynthTools.sampleRate * Math.PI * 2.0;
				}
				break;
			case SAWTOOTH:
				for(int index = 0; index < returnVal.length; index++) {
					if(control[index] == -1) {
						currentPhase = 0.0;
						continue;
					}
					returnVal[index] += sawtooth(currentPhase) * ampControl.getCurrentValuePow2();
					currentPhase += freqControl.getCurrentValuePow2() / SynthTools.sampleRate * Math.PI * 2.0;
				}
				break;
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

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
import main.ModuleEditor;
import main.MultiWindow;
import main.SynthTools;
import main.playable.Slider.Type;

public class PlayableLFO implements PlayableModule {
	
	PlayableEditor parent;
	Waveforms waveforms = new Waveforms();
	double minLogFreq = 5.0;
	double maxLogFreq = 14.0;
	double minLogAmp = -16.0;
	double maxLogAmp = 0.0;
	double prevAmp = 0.0;
	double prevFreq = 7;
	private Slider freqControl;
	private Slider fineFreqControl;
	private Slider ampControl;
	private Slider attackControl;
	private Slider releaseControl;
	private double currentAmplitude;
	private int maxScreenX;
	String moduleName;
	private int screenX;
	private int screenY;
	private int yPadding = PlayableEditor.moduleYPadding;
	private WaveType type = WaveType.STANDARD;
	private boolean fineFreq = false;

	private double currentPhase = 0.0;
	
	public enum WaveType {
		STANDARD,
		VARIABLE;
	}	
	
	public PlayableLFO(PlayableEditor parent, int screenX, int screenY, String moduleName, WaveType type) {
		this.parent = parent;
		this.moduleName = moduleName;
		this.type = type;
		int x = screenX;
		int y = screenY + PlayableEditor.moduleYPadding;
		this.screenX = x;
		this.screenY = screenY;
		if(type == WaveType.STANDARD) {
			freqControl = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, 0.5, 1024.0, 8.0, new String[] {"RATE", " ", " "});
			x = freqControl.getMaxX();
			fineFreqControl = new Slider(Slider.Type.LINEAR, x, y, 400, 1.0, 2.0, 1.0, new String[] {"FINE", " ", " "});
			x = fineFreqControl.getMaxX();
			ampControl = new Slider(Slider.Type.LINEAR, x, y, 400, 1.0 / 256.0, 1.0, 1.0 / 256.0, new String[] {"DEPTH", " ", " "});
			maxScreenX = ampControl.getMaxX();
		} else {
			freqControl = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, 0.5, 1024.0, 32.0, new String[] {"RATE", " ", " "});
			x = freqControl.getMaxX();
			fineFreqControl = new Slider(Slider.Type.LINEAR, x, y, 400, 1.0, 1.25, 1.0, new String[] {"FINE", " ", " "});
			x = fineFreqControl.getMaxX();
			attackControl = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, 0.001, 0.5, 0.5, new String[] {"A", " ", " "});
			x = attackControl.getMaxX();
			releaseControl = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, 0.001, 0.5, 0.5, new String[] {"R", " ", " "});
			x = releaseControl.getMaxX();
			ampControl = new Slider(Slider.Type.LINEAR, x, y, 400, 1.0 / 256.0, 1.0, 1.0 / 256.0, new String[] {"DEPTH", " ", " "});
			maxScreenX = ampControl.getMaxX();
		}
	}
	
	public PlayableLFO(PlayableEditor parent, int screenX, int screenY, double minFreq, double maxFreq, String moduleName, boolean fineFreq) {
		this.fineFreq = fineFreq;
		this.type = WaveType.STANDARD;
		this.parent = parent;
		this.moduleName = moduleName;
		int x = screenX;
		int y = screenY + PlayableEditor.moduleYPadding;
		this.screenX = x;
		this.screenY = screenY;
		freqControl = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, minFreq, maxFreq, minFreq, new String[] {"RATE", " ", " "});
		x = freqControl.getMaxX();
		if(fineFreq) {
			fineFreqControl = new Slider(Slider.Type.LINEAR, x, y, 400, 1.0, 2.0, 1.0, new String[] {"FINE", " ", " "});
			x = fineFreqControl.getMaxX();
		}
		ampControl = new Slider(Slider.Type.LINEAR, x, y, 400, 1.0 / 256.0, 1.0, 1.0 / 256.0, new String[] {"DEPTH", " ", " "});
		maxScreenX = ampControl.getMaxX();
	}
	
	public int getMaxScreenX() {
		return maxScreenX;
	}
	
	public synchronized void reset() {
		currentPhase = 0.0;
	}
	
	public synchronized void newSample() {
		double fineFreqVal = 1.0;
		if(fineFreq) fineFreqVal = fineFreqControl.getCurrentValue();
		currentPhase += (freqControl.getCurrentValue() * fineFreqVal / AudioFetcher.sampleRate) * 2.0 * Math.PI;
	}
	
	public synchronized void newSample(double freqRatio) {
		double fineFreqVal = 1.0;
		if(fineFreq) fineFreqVal = fineFreqControl.getCurrentValue();
		currentPhase += (freqControl.getCurrentValue() * fineFreqVal * freqRatio / AudioFetcher.sampleRate) * 2.0 * Math.PI;
	}
	
	public synchronized void newSample(double freqRatio, double vibrato) {
		double fineFreqVal = 1.0;
		if(fineFreq) fineFreqVal = fineFreqControl.getCurrentValue();
		currentPhase += (freqControl.getCurrentValue() * fineFreqVal * freqRatio / AudioFetcher.sampleRate * vibrato) * 2.0 * Math.PI;
	}
	
	public synchronized double triangle() {
		double ampVal = ampControl.getCurrentValue();
		double returnVal = (waveforms.triangle(currentPhase) / 2.0 + 0.5) * ampVal + 1.0 - ampVal;
		return returnVal;
	}
	
	public synchronized double squarewave() {
		double ampVal = ampControl.getCurrentValue();
		double returnVal = (waveforms.squarewave(currentPhase) / 2.0 + 0.5) * ampVal + 1.0 - ampVal;
		return returnVal;
	}
	
	public synchronized double sawtooth() {
		double ampVal = ampControl.getCurrentValue();
		double returnVal = (waveforms.sawtooth(currentPhase) / 2.0 + 0.5) * ampVal + 1.0 - ampVal;
		return returnVal;
	}
	
	public synchronized double sine() {
		double ampVal = ampControl.getCurrentValue();
		double returnVal = (Math.sin(currentPhase) / 2.0 + 0.5) * ampVal + 1.0 - ampVal;
		return returnVal;
	}
	
	public synchronized double ar() {
		double phase = currentPhase / (Math.PI * 2.0);
		phase -= Math.floor(phase);
		double ampVal = ampControl.getCurrentValue();
		if(phase <= 0.5) {
			return (1.0 - Math.exp(-1.0 * phase / attackControl.getCurrentValue())) * ampVal + (1.0 - ampVal);
		}
		return Math.exp(-1.0 * (phase - 0.5) / releaseControl.getCurrentValue())* ampVal + (1.0 - ampVal);
	}
	
	public synchronized double arFilter() {
		double phase = currentPhase / (Math.PI * 2.0);
		phase -= Math.floor(phase);
		if(phase <= 0.5) {
			return (1.0 - Math.exp(-1.0 * phase / attackControl.getCurrentValue()));
		}
		return Math.exp(-1.0 * (phase - 0.5) / releaseControl.getCurrentValue());
	}
	
	public synchronized double triangleFilter() {
		double ampVal = ampControl.getCurrentValue();
		double returnVal = (waveforms.triangle(currentPhase) / 2.0 + 0.5) * ampVal;
		return returnVal;
	}
	
	public synchronized double squarewaveFilter() {
		double ampVal = ampControl.getCurrentValue();
		double returnVal = (waveforms.squarewave(currentPhase) / 2.0 + 0.5) * ampVal;
		return returnVal;
	}
	
	public synchronized double sawtoothFilter() {
		double ampVal = ampControl.getCurrentValue();
		double returnVal = (waveforms.sawtooth(currentPhase) / 2.0 + 0.5) * ampVal;
		return returnVal;
	}
	
	public synchronized double sineFilter() {
		double ampVal = ampControl.getCurrentValue();
		double returnVal = (Math.sin(currentPhase) / 2.0 + 0.5) * ampVal;
		return returnVal;
	}
	
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g; 
		g2.setFont(new Font("TRUETYPE_FONT", Font.BOLD, 12));
		Font font = g2.getFont();
		FontMetrics metrics = g2.getFontMetrics(font);
		int hgt = metrics.getHeight();
		int adv = metrics.stringWidth(moduleName);
		g2.setColor(Color.WHITE);
		int textYOffset = (yPadding - hgt) / 2 + yPadding / 2;
		int textXOffset = (maxScreenX - screenX - adv) / 2;
		g2.setColor(new Color(0.5f, 0.0f, 0.0f));
		g2.fillRect(screenX + 4, screenY, maxScreenX - screenX - 8, yPadding - 4);
		g2.setColor(Color.WHITE);
		g2.drawString(moduleName, screenX + textXOffset, screenY + textYOffset);
		g2.setColor(Color.BLUE);
		g2.drawRect(screenX, screenY, maxScreenX - screenX, freqControl.getMaxY());
		freqControl.draw(g2);
		if(fineFreq) fineFreqControl.draw(g2);
		if(type == WaveType.VARIABLE) {
			attackControl.draw(g2);
			releaseControl.draw(g2);
		}
		ampControl.draw(g2);
	}

	public void pointSelected(int x, int y) {
		freqControl.pointSelected(x, y);
		if(fineFreq) fineFreqControl.pointSelected(x, y);
		if(type == WaveType.VARIABLE) {
			attackControl.pointSelected(x, y);
			releaseControl.pointSelected(x, y);
		}
		ampControl.pointSelected(x, y);
		parent.view.repaint();
	}
}

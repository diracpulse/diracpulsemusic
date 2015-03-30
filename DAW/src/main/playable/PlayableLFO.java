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
	private Slider ampControl;
	private int maxScreenX;
	String moduleName;
	private int screenX;
	private int screenY;
	private int yPadding = PlayableEditor.moduleYPadding;
	
	public enum WaveformType {
		SINE,
		TRIANGLE,
		SQUAREWAVE,
		SAWTOOTH,
	}
	
	private double currentPhase = 0.0;
	
	public PlayableLFO(PlayableEditor parent, int screenX, int screenY, String moduleName) {
		
		this.parent = parent;
		this.moduleName = moduleName;
		int x = screenX;
		int y = screenY + PlayableEditor.moduleYPadding;
		this.screenX = x;
		this.screenY = screenY;
		freqControl = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, 1.0, 1024.0, 4.0, new String[] {"RATE", " ", " "});
		x = freqControl.getMaxX();
		ampControl = new Slider(Slider.Type.LOGARITHMIC, x, y, 400, 1.0 / Short.MAX_VALUE, 1.0, 0.5, new String[] {"DEPTH", " ", " "});
		maxScreenX = ampControl.getMaxX();
	}
	
	public int getMaxScreenX() {
		return maxScreenX;
	}
	
	public synchronized void reset() {
		currentPhase = 0.0;
	}
	
	public void newSample(double freqRatio) {
		currentPhase += (freqControl.getCurrentValue() * freqRatio / AudioFetcher.sampleRate) * 2.0 * Math.PI;
	}
	
	public double triangle() {
		double ampVal = ampControl.getCurrentValue();
		double returnVal = (waveforms.triangle(currentPhase) / 2.0 + 0.5) * ampVal + 1.0 - ampVal;
		return returnVal;
	}
	
	public double squarewave() {
		double ampVal = ampControl.getCurrentValue();
		double returnVal = (waveforms.squarewave(currentPhase) / 2.0 + 0.5) * ampVal + 1.0 - ampVal;
		return returnVal;
	}
	
	public double sawtooth() {
		double ampVal = ampControl.getCurrentValue();
		double returnVal = (waveforms.sawtooth(currentPhase) / 2.0 + 0.5) * ampVal + 1.0 - ampVal;
		return returnVal;
	}
	
	public double sine() {
		double ampVal = ampControl.getCurrentValue();
		double returnVal = (Math.sin(currentPhase) / 2.0 + 0.5) * ampVal + 1.0 - ampVal;
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
		ampControl.draw(g2);
	}

	public void pointSelected(int x, int y) {
		freqControl.pointSelected(x, y);
		ampControl.pointSelected(x, y);
		parent.view.repaint();
	}
}


package main.modules;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import main.ModuleEditor;
import main.modules.BasicWaveformController;
import main.modules.BasicWaveformView;
import main.modules.BasicWaveformEditor.ControlRect;
import main.modules.SpectrumEQ.EQBand;

public class SpectrumEQView extends JPanel {

	private static final long serialVersionUID = 9057228507254113149L;
	
	SpectrumEQEditor parent;
	
	public SpectrumEQView(SpectrumEQEditor parent) {
		this.parent = parent;
	}
	
	protected Color timeToColor(double time) {
		float currentVal = (float) time / (float) (parent.maxTime - parent.minTime);
		if (currentVal < 0.0f)
			currentVal = 0.0f;
		if (currentVal > 1.0f)
			currentVal = 1.0f;
		float red = currentVal;
		float green = 0.0f;
		float blue = 1.0f - currentVal;
		if (red >= 0.5f) {
			green = (1.0f - red) * 2.0f;
		} else {
			green = red * 2.0f;
		}
		return new Color(red, green, blue, 0.5f);
	}
	
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		if(parent.minTime == parent.maxTime || parent.minFreq == parent.maxFreq) return;
		for(double freq: parent.freqToTimeToAmplitude.keySet()) {
			for(double time: parent.freqToTimeToAmplitude.get(freq).keySet()) {
				g2.setColor(timeToColor(time));
				int x = parent.freqToX(freq);
				int y = parent.amplitudeToY(parent.freqToTimeToAmplitude.get(freq).get(time));
				g2.drawLine(x, y, x, getHeight());
			}
		}
		drawGrid(g2);
		drawEQBands(g2);
	}
	
	private void drawGrid(Graphics2D g2) {
		g2.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
		for(double freq = Math.ceil(parent.minFreq); freq < parent.maxFreq; freq += 1.0) {
			g2.drawLine(parent.freqToX(freq), getHeight() - parent.yPadding, parent.freqToX(freq), 0);
			g2.drawString(Math.round(Math.pow(2.0, freq)) + "", parent.freqToX(freq), getHeight() - 2);
		}
		for(double amplitude = Math.ceil(parent.minAmplitude); amplitude < parent.maxAmplitude; amplitude += 1.0) {
			g2.drawLine(parent.xPadding, parent.amplitudeToY(amplitude), getWidth(),  parent.amplitudeToY(amplitude));
			g2.drawString(Math.round(Math.pow(2.0, amplitude)) + "", 0, parent.amplitudeToY(amplitude));
		}
		g2.setColor(new Color(1.0f, 0.0f, 1.0f, 0.5f));
		for(double gain = ModuleEditor.minAmplitudeLog2; gain <= ModuleEditor.maxAmplitudeLog2; gain += 1.0) {
			g2.drawLine(parent.xPadding, parent.gainToY(gain), getWidth(),  parent.gainToY(gain));
			g2.drawString(Math.round(Math.pow(2.0, gain)) + "", 0, parent.gainToY(gain));
		}
	}
	
	private void drawEQBands(Graphics2D g2) {
		g2.setColor(new Color(0.0f, 1.0f, 0.0f, 0.5f));
		g2.setStroke(new BasicStroke(2));
		for(EQBand eqBand: parent.parent.eqBands) {
			int x = parent.freqToX(Math.log(eqBand.criticalBand.getCenterFreq()) / Math.log(2.0));
			int x0 = parent.freqToX(Math.log(eqBand.criticalBand.getLowerBound()) / Math.log(2.0));
			int x1 = parent.freqToX(Math.log(eqBand.criticalBand.getUpperBound()) / Math.log(2.0));
			int y = parent.gainToY(Math.log(eqBand.gain)/Math.log(2.0));
			int y0 = parent.gainToY(Math.log(eqBand.gain)/Math.log(2.0) - 1.0);
			g2.drawLine(x0, y0, x, y);
			g2.drawLine(x1, y0, x, y);
			g2.fillRect(x - 6, y - 6, 12, 12);
		}
	}

}

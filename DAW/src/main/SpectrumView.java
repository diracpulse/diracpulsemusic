
package main;

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

import main.Spectrum.Channel;
import main.Spectrum.DataView;
import main.modules.BasicWaveformController;
import main.modules.BasicWaveformView;
import main.modules.BasicWaveformEditor.ControlRect;

public class SpectrumView extends JPanel {

	private static final long serialVersionUID = 9057228507254113149L;
	
	Spectrum parent;
	
	public SpectrumView(Spectrum parent) {
		this.parent = parent;
	}
	
	protected Color timeToColor(Channel channel, double time) {
		float currentVal = (float) time / (float) (parent.channelToData.get(channel).maxTime - parent.channelToData.get(channel).minTime);
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
		return new Color(red, green, blue);
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		if(parent.dataView == DataView.LEFT || parent.dataView == DataView.RIGHT) {
			Channel channel = Channel.LEFT;
			if(parent.dataView == DataView.RIGHT) channel = Channel.RIGHT;
			double minTime = parent.channelToData.get(channel).minTime;
			double maxTime = parent.channelToData.get(channel).maxTime;
			double minFreq = parent.channelToData.get(channel).minFreq;
			double maxFreq = parent.channelToData.get(channel).maxFreq;			
			if(minTime == maxTime || minFreq == maxFreq) return;
			TreeMap<Double, TreeMap<Double, Double>> freqToTimeToAmplitude = parent.channelToData.get(channel).freqToTimeToAmplitude;
			for(double freq: freqToTimeToAmplitude.keySet()) {
				for(double time: freqToTimeToAmplitude.get(freq).keySet()) {
					g2.setColor(timeToColor(channel, time));
					int x = parent.freqToX(channel, freq);
					int y = parent.amplitudeToY(channel, freqToTimeToAmplitude.get(freq).get(time));
					g2.drawRect(x, y, 1, 1);
				}
			}
			drawGrid(channel, g2);
		}
	}
	
	private void drawGrid(Channel channel, Graphics g2) {
		g2.setColor(new Color(0.5f, 0.5f, 0.5f));
		for(double freq = Math.ceil(parent.channelToData.get(channel).minFreq); freq < parent.channelToData.get(channel).maxFreq; freq += 1.0) {
			g2.drawLine(parent.freqToX(channel, freq), getHeight() - parent.yPadding, parent.freqToX(channel, freq), 0);
			g2.drawString(Math.round(Math.pow(2.0, freq)) + "", parent.freqToX(channel, freq), getHeight() - 2);
		}
		for(double amplitude = Math.ceil(parent.channelToData.get(channel).minAmplitude); amplitude < parent.channelToData.get(channel).maxAmplitude; amplitude += 1.0) {
			g2.drawLine(parent.xPadding, parent.amplitudeToY(channel, amplitude), getWidth(),  parent.amplitudeToY(channel, amplitude));
			g2.drawString(Math.round(Math.pow(2.0, amplitude)) + "", 0, parent.amplitudeToY(channel, amplitude));
		}
	}

}

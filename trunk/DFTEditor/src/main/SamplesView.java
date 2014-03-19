
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

import main.SamplesEditor.Channel;
import main.modules.BasicWaveformController;
import main.modules.BasicWaveformView;
import main.modules.BasicWaveformEditor.ControlRect;

public class SamplesView extends JPanel {

	private static final long serialVersionUID = 9057228507254113149L;
	
	SamplesEditor parent;
	
	public SamplesView(SamplesEditor parent) {
		this.parent = parent;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		if(parent.maxTime == 0) return;
		double timePerPixel = parent.getTimePerPixel();
		for(Channel channel: parent.channels.keySet()) {
			if(channel == Channel.LEFT) g2.setColor(new Color(0.0f, 0.0f, 1.0f, 0.5f));
			if(channel == Channel.RIGHT) g2.setColor(new Color(1.0f, 0.0f, 0.0f, 0.5f));
			double[] samples = parent.channels.get(channel);
			for(double time = parent.minViewTime; time < parent.getMaxViewTime(); time += timePerPixel) {
				for(double pixelTime = time; pixelTime < time + timePerPixel; pixelTime += 1.0 / SynthTools.sampleRate) {
					int sampleIndex = (int) Math.round(pixelTime * SynthTools.sampleRate);
					if(sampleIndex >= samples.length) break;
					int y = parent.amplitudeToY(samples[sampleIndex]);
					int x = parent.timeToX(pixelTime);
					g2.drawRect(x, y, 1, 1);
				}
			}
		}
		drawGrid(g2);
	}
	
	private void drawGrid(Graphics g2) {
		g2.setColor(new Color(0.5f, 0.5f, 0.5f));
		double xGridStep = Math.pow(2.0, Math.ceil(Math.log(parent.getTimePerPixel() * parent.xPadding) / Math.log(2.0)));
		for(double time = parent.minViewTime; time < parent.getMaxViewTime(); time += xGridStep) {
			g2.drawLine(parent.timeToX(time), getHeight() - parent.yPadding, parent.timeToX(time), 0);
			g2.drawString(Math.round(time * 100000.0) / 100000.0 + "", parent.timeToX(time), getHeight() - 2);
		}
		for(double amplitude = -1.0 * parent.maxAmplitude; amplitude <= parent.maxAmplitude; amplitude += parent.maxAmplitude / 16.0) {
			g2.drawLine(parent.xPadding, parent.amplitudeToY(amplitude), getWidth(),  parent.amplitudeToY(amplitude));
			g2.drawString(amplitude + "", 0, parent.amplitudeToY(amplitude));
		}
	}

}
	
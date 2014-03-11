package main.modules;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.HashSet;

import javax.swing.JPanel;

import main.SynthTools;


public class EnvelopeView extends JPanel {

	private static final long serialVersionUID = -3242131901933358666L;
	private EnvelopeEditor parent;
	public static final int fontSize = 12;
	public static final int xPadding = fontSize * 4;
	public static final int yPadding = fontSize + 6;
	
	EnvelopeView(EnvelopeEditor parent) {
		this.parent = parent;
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.WHITE);
		int xSteps = 16;
		for(int fraction = 0; fraction <= xSteps; fraction++) {
			float colorVal = (float) Math.pow(1.0f / xSteps, 0.5f);
			for(int modVal = 2; modVal <= xSteps; modVal *= 2) {
				if(fraction % modVal == 0) {
					float testColorVal = (float) Math.pow((float) modVal / (float) xSteps, 0.3f);
					if(testColorVal > colorVal) colorVal = testColorVal;
				}
			}
			double time = (double) fraction / xSteps;
			g.setColor(new Color(colorVal, colorVal, colorVal));
			g.drawLine(parent.timeToX(time), 0, parent.timeToX(time), getHeight());
			g.drawString(new Double(time).toString(), parent.timeToX(time), getHeight() - 2);
		}
		int ySteps = 32;
		for(int fraction = 0; fraction <= ySteps; fraction++) {
			float colorVal = (float) Math.pow(1.0f / ySteps, 0.5f);
			for(int modVal = 2; modVal <= ySteps; modVal *= 2) {
				if(fraction % modVal == 0) {
					float testColorVal = (float) Math.pow((float) modVal / (float) ySteps, 0.3f);
					if(testColorVal > colorVal) colorVal = testColorVal;
				}
			}
			double amplitude = (double) fraction / ySteps;
			g.setColor(new Color(colorVal, colorVal, colorVal));
			g.drawLine(xPadding, parent.amplitudeToY(amplitude), getWidth(), parent.amplitudeToY(amplitude));
			g.drawString(new Double(amplitude).toString(), 0, parent.amplitudeToY(amplitude) - fontSize + yPadding - 1);
		}
		double[] control = new double[(int) Math.round(Envelope.maxEnvelopeDuration * SynthTools.sampleRate)];
		for(int index = 0; index < control.length; index++) {
			control[index] = 1.0;
		}
		g.setColor(Color.BLUE);
		double[] samples = parent.envelope.masterGetSamples(new HashSet<Integer>(), control);
		double step = parent.getMillisPerPixel() / 1000.0 * SynthTools.sampleRate;
		for(double index = 0; index < samples.length - 1; index += step) {
			int i0 = (int) Math.round(index);
			int i1 = (int) Math.round(index + step);
			if(i1 >= samples.length) break;
			int x0 = parent.timeToX(i0 / SynthTools.sampleRate);
			int x1 = parent.timeToX(i1 / SynthTools.sampleRate);
			int y0 = parent.amplitudeToY(samples[i0]);
			int y1 =  parent.amplitudeToY(samples[i1]);
			g.drawLine(x0, y0, x1, y1);
		}
		g.setColor(Color.RED);
		for(double time: parent.envelope.getEnvelopeTimes()) {
			int x = parent.timeToX(time);
			g.drawLine(x, 0, x, getHeight());
		}
		g.setColor(Color.GREEN);
		for(Rectangle rect: parent.getControlAreas()) {
			g.fillRect(rect.x, rect.y, rect.width, rect.height);
		}
	}

}

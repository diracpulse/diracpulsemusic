package main.modules;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.HashSet;

import javax.swing.JPanel;

import main.ModuleEditor;
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
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.WHITE);
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
			g2.setColor(new Color(colorVal, colorVal, colorVal));
			g2.drawLine(parent.timeToX(time), 0, parent.timeToX(time), getHeight());
			g2.drawString(new Double(time).toString(), parent.timeToX(time), getHeight() - 2);
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
			g2.setColor(new Color(colorVal, colorVal, colorVal));
			g2.drawLine(xPadding, parent.amplitudeToY(amplitude), getWidth(), parent.amplitudeToY(amplitude));
			g2.drawString(new Double(amplitude).toString(), 0, parent.amplitudeToY(amplitude) - fontSize + yPadding - 1);
		}
		double[] control = new double[(int) Math.round(parent.envelope.getEnvelopeTimes().last() * SynthTools.sampleRate)];
		for(int index = 0; index < control.length; index++) {
			control[index] = 1.0;
		}
		g2.setColor(new Color(0.5f, 0.5f, 1.0f));
		g2.setStroke(new BasicStroke(2));
		double[] samples = parent.envelope.masterGetSamples(new HashSet<Integer>(), control, false);
		double step = parent.getMillisPerPixel() / 1000.0 * SynthTools.sampleRate;
		for(double index = 0; index < samples.length - 1; index += step) {
			int i0 = (int) Math.round(index);
			int i1 = (int) Math.round(index + step);
			if(i1 >= samples.length) break;
			int x0 = parent.timeToX(i0 / SynthTools.sampleRate);
			int x1 = parent.timeToX(i1 / SynthTools.sampleRate);
			int y0 = parent.amplitudeToY(samples[i0]);
			int y1 =  parent.amplitudeToY(samples[i1]);
			g2.drawLine(x0, y0, x1, y1);
		}
		g2.setColor(new Color(0.5f, 0.0f, 0.5f));
		g2.setStroke(new BasicStroke(1));
		control = new double[(int) Math.round(ModuleEditor.defaultDuration * SynthTools.sampleRate)];
		for(int index = 0; index < control.length; index++) {
			control[index] = 1.0;
		}
		samples = parent.envelope.masterGetSamples(new HashSet<Integer>(), control);
		for(double index = 0; index < samples.length - 1; index += step) {
			int i0 = (int) Math.round(index);
			int i1 = (int) Math.round(index + step);
			if(i1 >= samples.length) break;
			int x0 = parent.timeToX(i0 / SynthTools.sampleRate);
			int x1 = parent.timeToX(i1 / SynthTools.sampleRate);
			int y0 = parent.amplitudeToY(samples[i0]);
			int y1 =  parent.amplitudeToY(samples[i1]);
			g2.drawLine(x0, y0, x1, y1);
		}		
		g2.setColor(Color.RED);
		for(double time: parent.envelope.getEnvelopeTimes()) {
			int x = parent.timeToX(time);
			g2.drawLine(x, 0, x, getHeight());
		}
		for(Rectangle rect: parent.getControlAreas()) {
			if(parent.envelope.getIndexFromEnvelopeTime(parent.getEnvelopePoint(rect.x, rect.y).seconds) == parent.envelope.sustainIndex) {
				g2.setColor(Color.RED);
			} else {
				g2.setColor(Color.GREEN);
			}
			g2.fillRect(rect.x, rect.y, rect.width, rect.height);
		}
	}

}

package main.modules;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;

import javax.swing.JPanel;

import main.Sequencer;
import main.SequencerUtils;
import main.SynthTools;


public class EnvelopeView extends JPanel {

	private EnvelopeEditor parent;
	
	EnvelopeView(EnvelopeEditor parent) {
		this.parent = parent;
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.GRAY);
		double[] samples = parent.envelope.masterGetSamples(new HashSet<Integer>(), null);
		double step = parent.getMillisPerPixel() / 1000.0 * SynthTools.sampleRate;
		for(double index = 0; index < samples.length; index += step) {
			int intIndex = (int) Math.round(index);
			double amplitude = samples[intIndex];
			g.drawLine((int) Math.round(index / step), (int) Math.round(this.getHeight() * (1.0 - amplitude)), (int) Math.round(index / step), this.getHeight());
		}
	}

}

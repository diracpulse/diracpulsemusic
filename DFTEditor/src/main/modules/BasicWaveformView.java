package main.modules;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashSet;

import javax.swing.JPanel;

import main.Sequencer;
import main.SequencerUtils;
import main.SynthTools;


public class BasicWaveformView extends JPanel {

	private BasicWaveformEditor parent;
	
	BasicWaveformView(BasicWaveformEditor parent) {
		this.parent = parent;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		int fontSize = 12;
		int yStep = fontSize + 6;
		g2.setColor(Color.WHITE);
		Font font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
		g2.setFont(font);
		int padding = 4;
		int currentX = padding;
		int currentY = 0;
		g2.setColor(Color.GREEN);
		g2.drawString("Freq: " + parent.basicWaveform.getFreqInHz(), currentX, currentY + fontSize + padding - 1);
		currentY += yStep;
		parent.freqControl = new Rectangle(0, currentY, getWidth(), yStep);
		g2.setColor(Color.DARK_GRAY);
		g2.fillRect(0, currentY + 2, getWidth(), yStep - 2);
		g2.setColor(Color.WHITE);
		g2.fillRect(0, currentY + 2, parent.freqInHzToX(parent.basicWaveform.getFreqInHz()), yStep - 2);
		currentY += yStep;
		g2.setColor(Color.GREEN);
		g2.drawString("Amp: " + Math.round(parent.basicWaveform.getAmplitude() * 100000.0) / 100000.0 + " (" + Math.round(Math.log(parent.basicWaveform.getAmplitude())/Math.log(10.0) * 2000.0) / 100.0 + "dB)", currentX, currentY + fontSize + padding - 1);
		currentY += yStep;
		parent.ampControl = new Rectangle(0, currentY, getWidth(), yStep);
		g2.setColor(Color.DARK_GRAY);
		g2.fillRect(0, currentY, getWidth(), yStep);
		g2.setColor(Color.WHITE);
		g2.fillRect(0, currentY, parent.amplitudeToX(parent.basicWaveform.getAmplitude()), yStep);
		currentY += yStep;
		g2.setColor(Color.GREEN);
		g2.drawString("FMMod: " + Math.round(parent.basicWaveform.getFMMod() * 100000.0) / 100000.0 + " (" + Math.round(Math.log(parent.basicWaveform.getFMMod())/Math.log(10.0) * 2000.0) / 100.0 + "dB)", currentX, currentY + fontSize + padding - 1);
		currentY += yStep;
		parent.fmModControl = new Rectangle(0, currentY, getWidth(), yStep);
		g2.setColor(Color.DARK_GRAY);
		g2.fillRect(0, currentY, getWidth(), yStep);
		g2.setColor(Color.WHITE);
		g2.fillRect(0, currentY, parent.fmModToX(parent.basicWaveform.getFMMod()), yStep);
		currentY += yStep;
	}

}

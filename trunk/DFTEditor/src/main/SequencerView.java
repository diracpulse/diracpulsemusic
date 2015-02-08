package main;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class SequencerView extends JPanel {

	private static final long serialVersionUID = 6342694782715867005L;
	private Sequencer parent;
	
	SequencerView(Sequencer parent) {
		this.parent = parent;
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawLeftFreqs(g);
		drawTimeGrid(g);
		drawSequencerData(g);
	}
	
	public void drawLeftFreqs(Graphics g) {
		int yOffset = (Sequencer.noteHeight - SequencerUtils.digitHeight) / 2;
		int y = yOffset;
		int x = parent.scrollPane.getVisibleRect().x;
		Color white = new Color(0.0f, 0.0f, 0.0f);
		Color black = new Color(1.0f, 1.0f, 1.0f);
		for (int note = (Sequencer.noteBase * Sequencer.octaves) - 1; note >= -Sequencer.numPercussion; note--) {
			if(note >= 0) {
				int freq = (int) Math.round(Math.pow(2.0, 1.0 * note / Sequencer.noteBase) * Sequencer.minFreq);
				SequencerUtils.DrawIntegerHorizontal(g, white, black, x, y, 4,
						freq);
				SequencerUtils.DrawIntegerHorizontal(g, white, black, x + 6 * SequencerUtils.digitWidth, y, 1,
						note);
				int lineY = y + SequencerUtils.digitHeight + yOffset;
				g.setColor(Color.WHITE);
				g.drawLine(0, lineY, Sequencer.scrollableWidth, lineY);
				y += Sequencer.noteHeight;
			} else {
				SequencerUtils.DrawIntegerHorizontal(g, white, black, x, y, 4,
						Math.abs(note));
				SequencerUtils.DrawIntegerHorizontal(g, white, black, x + 6 * SequencerUtils.digitWidth, y, 1,
						Math.abs(note));
				int lineY = y + SequencerUtils.digitHeight + yOffset;
				g.setColor(Color.WHITE);
				g.drawLine(0, lineY, Sequencer.scrollableWidth, lineY);
				y += Sequencer.noteHeight;
			}
			// System.out.println("drawUpperTimes " + intVal + " " +
			// decimalVal);
		}
	}
	
	public void drawTimeGrid(Graphics g) {
		int xOffset = SequencerUtils.digitWidth * Sequencer.leftDigits;
		g.setColor(Color.DARK_GRAY);
		for(int x = xOffset; x < Sequencer.scrollableWidth; x += Sequencer.pixelsPerDivision) {
			g.drawLine(x,  0, x, Sequencer.scrollableHeight);
		}
		g.setColor(Color.GRAY);
		for(int x = xOffset; x < Sequencer.scrollableWidth; x += Sequencer.pixelsPerBeat) {
			g.drawLine(x,  0, x, Sequencer.scrollableHeight);
		}
	}
	
	public void drawSequencerData(Graphics g) {
		int yOffset = (Sequencer.noteHeight - SequencerUtils.digitHeight) / 2;
		int index = 0;
		for(double[] freqRatio: parent.freqRatiosAtTimeInPixels) {
			g.setColor(parent.moduleInfo.get(index).getColor());
			for(int time = 0; time < Sequencer.totalPixels; time++) {
				if(freqRatio[time] >= 0.0) {
					int note = (int) Math.round(Math.log(freqRatio[time]) / Math.log(2.0) * Sequencer.noteBase);
					int y0 = (Sequencer.noteBase * Sequencer.octaves - note - 1) * Sequencer.noteHeight;
					int y1 = (Sequencer.noteBase * Sequencer.octaves - note) * Sequencer.noteHeight - yOffset;
					int x = time + Sequencer.leftDigits * SequencerUtils.digitWidth;
					g.drawLine(x, y0, x, y1);
				}
			}
			index++;
		}
	}
	
	public int getNote(int y) {
		int note = (Sequencer.noteBase * Sequencer.octaves) - 1 - y / Sequencer.noteHeight;
		return note;
	}

	public int getTimeInBeats(int x) {
		int time = (int)  Math.round(((x - SequencerUtils.digitWidth * Sequencer.leftDigits) / Sequencer.pixelsPerBeat));
		return time;
	}
	
	public int getTimeInDivisions(int x) {
		int time = (int)  Math.round(((x - SequencerUtils.digitWidth * Sequencer.leftDigits) / Sequencer.pixelsPerDivision));
		return time;
	}
	
}

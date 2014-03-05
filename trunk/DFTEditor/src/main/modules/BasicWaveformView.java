package main.modules;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JPanel;

import main.modules.BasicWaveformEditor.ControlRect;

public class BasicWaveformView extends JPanel {

	private static final long serialVersionUID = -3597657941937913124L;
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
		for(BasicWaveformEditor.ControlRect controlRect: parent.controlRects) {
			g2.setColor(Color.GREEN);
			g2.drawString("Envelope: " + controlRect.basicWaveform.getID() + ":  Freq: " + controlRect.basicWaveform.getFreqInHz(), currentX, currentY + fontSize + padding - 1);
			currentY += yStep;
			controlRect.coarseFreqControl = new Rectangle(0, currentY, getWidth(), yStep - 2);
			g2.setColor(Color.LIGHT_GRAY);
			g2.fillRect(0, currentY + 2, getWidth(), yStep - 2);
			g2.setColor(Color.WHITE);
			g2.fillRect(0, currentY + 2, parent.freqToXCoarse(controlRect.basicWaveform.getFreqInHz()), yStep - 2);
			currentY += yStep;
			controlRect.fineFreqControl = new Rectangle(0, currentY, getWidth(), yStep - 2);
			g2.setColor(Color.DARK_GRAY);
			g2.fillRect(0, currentY + 2, getWidth(), yStep - 2);
			g2.setColor(Color.WHITE);
			g2.fillRect(0, currentY + 2, parent.valueToXFine(controlRect.basicWaveform.getFreqInHz()), yStep - 2);
			currentY += yStep;
			g2.setColor(Color.GREEN);
			g2.drawString("Amp: " + Math.round(controlRect.basicWaveform.getAmplitude() * 100000.0) / 100000.0 + " (" + Math.round(Math.log(controlRect.basicWaveform.getAmplitude())/Math.log(10.0) * 2000.0) / 100.0 + "dB)", currentX, currentY + fontSize + padding - 1);
			currentY += yStep;
			controlRect.coarseAmpControl = new Rectangle(0, currentY, getWidth(), yStep - 2);
			g2.setColor(Color.LIGHT_GRAY);
			g2.fillRect(0, currentY, getWidth(), yStep - 2);
			g2.setColor(Color.WHITE);
			g2.fillRect(0, currentY, parent.amplitudeToXCoarse(controlRect.basicWaveform.getAmplitude()), yStep - 2);
			currentY += yStep;
			controlRect.fineAmpControl = new Rectangle(0, currentY, getWidth(), yStep - 2);
			g2.setColor(Color.DARK_GRAY);
			g2.fillRect(0, currentY, getWidth(), yStep - 2);
			g2.setColor(Color.WHITE);
			g2.fillRect(0, currentY, parent.valueToXFine(controlRect.basicWaveform.getAmplitude()), yStep - 2);
			currentY += yStep;
			g2.setColor(Color.GREEN);
			g2.drawString("FMMod: " + Math.round(controlRect.basicWaveform.getFMMod() * 100000.0) / 100000.0 + " (" + Math.round(Math.log(controlRect.basicWaveform.getFMMod())/Math.log(10.0) * 2000.0) / 100.0 + "dB)", currentX, currentY + fontSize + padding - 1);
			currentY += yStep;
			controlRect.coarseFMModControl = new Rectangle(0, currentY, getWidth(), yStep - 2);
			g2.setColor(Color.LIGHT_GRAY);
			g2.fillRect(0, currentY, getWidth(), yStep - 2);
			g2.setColor(Color.WHITE);
			g2.fillRect(0, currentY, parent.fmModToXCoarse(controlRect.basicWaveform.getFMMod()), yStep - 2);
			currentY += yStep;
			controlRect.fineFMModControl = new Rectangle(0, currentY, getWidth(), yStep - 2);
			g2.setColor(Color.DARK_GRAY);
			g2.fillRect(0, currentY, getWidth(), yStep - 2);
			g2.setColor(Color.WHITE);
			g2.fillRect(0, currentY, parent.valueToXFine(controlRect.basicWaveform.getFMMod()), yStep - 2);
			currentY += yStep;
		}
	}

}

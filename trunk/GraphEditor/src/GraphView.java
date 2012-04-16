import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class GraphView extends JComponent {

	public enum ColorView {
		AMPLITUDE,
		FREQUENCY,
		HARMONICS;
	}
	
	public enum YView {
		AMPLITUDE,
		FREQUENCY;
	}

	public static ColorView colorView = ColorView.FREQUENCY;
	public static YView yView = YView.AMPLITUDE;
	
	private static final long serialVersionUID = 2964004162144513754L;
	
	private static BufferedImage bi;
	private static boolean useImage = true;
	private static boolean drawPlaying = false;
	
	public void drawFileData(Graphics g) {
		double pixelsPerTime = (double) getWidth() / (double) (GraphEditor.maxViewTime - GraphEditor.minViewTime);
		double pixelsPerValue = 1;
		if(yView == YView.AMPLITUDE) pixelsPerValue = (double) getHeight() / (double) (GraphEditor.maxLogAmplitude - GraphEditor.minLogAmplitude);
		if(yView == YView.FREQUENCY) pixelsPerValue = (double) getHeight() / (double) (GraphEditor.maxViewNote - GraphEditor.minViewNote);
		for(Harmonic harmonic: GraphEditor.harmonicIDToHarmonic.values()) {
			if(harmonic.getLength() < GraphEditor.minHarmonicLength) continue;
			if(harmonic.getAverageNote() < GraphEditor.minViewNote || harmonic.getAverageNote() > GraphEditor.maxViewNote) continue;
			ArrayList<FDData> hData = new ArrayList<FDData>(harmonic.getAllData());
			if(hData.size() < 2) continue;
			FDData start = hData.get(0);
			for(int index = 1; index < hData.size(); index++) {
				FDData end = hData.get(index);
				if(GraphEditor.clipZero) {
					if(start.getLogAmplitude() == 0.0) {
						start = end;
						continue;
					}
					if(end.getLogAmplitude() == 0.0) {
						start = end;
						continue;
					}
				}
				int startTime = start.getTime() - GraphEditor.minViewTime;
				if(startTime > GraphEditor.maxViewTime) {
					start = end;
					continue;
				}
				int startX = (int) Math.round(pixelsPerTime * startTime);
				int startY = 0;
				if(yView == YView.AMPLITUDE) startY = (int) Math.round(pixelsPerValue * start.getLogAmplitude() - GraphEditor.minLogAmplitude);
				if(yView == YView.FREQUENCY) startY = (int) Math.round(pixelsPerValue * (start.getNote() - GraphEditor.minViewNote));
				startY = getHeight() - startY;
				int endTime = end.getTime() - GraphEditor.minViewTime;
				if(endTime < 0) {
					start = end;
					continue;					
				}			
				int endX = (int) Math.round(pixelsPerTime * end.getTime());
				int endY = 1;
				if(yView == YView.AMPLITUDE) endY = (int) Math.round(pixelsPerValue * end.getLogAmplitude() - GraphEditor.minLogAmplitude);
				if(yView == YView.FREQUENCY) endY = (int) Math.round(pixelsPerValue * (end.getNote() - GraphEditor.minViewNote));		
				endY = getHeight() - endY;
				if(colorView == ColorView.AMPLITUDE) {
					g.setColor(getAmplitudeColor((start.getLogAmplitude() + end.getLogAmplitude()) / 2));
				}
				if(colorView == ColorView.FREQUENCY) {
					g.setColor(getFrequencyColor((start.getNote() + end.getNote()) / 2));
				}
				if(colorView == ColorView.HARMONICS) {
					g.setColor(getHarmonicColor(harmonic.getHarmonicID()));
				}				
				g.drawLine(startX, startY, endX, endY);
				start = end;
			}
		}
	}
	
	private Color getAmplitudeColor(double amplitude) {
		return getAmplitudeColor(amplitude, 1.0f);
	}

	private Color getAmplitudeColor(double amplitude, float alpha) {
		float noteRange = (float) (GraphEditor.maxLogAmplitude - GraphEditor.minLogAmplitude);
		float currentVal = (float) amplitude;
		currentVal -= GraphEditor.minLogAmplitude;
		currentVal /= noteRange;
		if(currentVal < 0.0f) currentVal = 0.0f;
		if(currentVal > 1.0f) currentVal = 1.0f;
		float red = currentVal;
		float green = 0.0f;
		float blue = 1.0f - currentVal;
		if(red >= 0.5f) {
			green = (1.0f - red) * 2.0f;
		} else {
			green = red * 2.0f;
		}
		//return new Color(1.0f, 1.0f, 1.0f, 0.75f);
		return new Color(red, green, blue, alpha);
	}
	
	private Color getFrequencyColor(double note) {
		return getFrequencyColor(note, 1.0f);
	}

	private Color getFrequencyColor(double note, float alpha) {
		float noteRange = (float) (GraphEditor.maxNote - GraphEditor.minNote);
		float currentVal = (float) note;
		currentVal -= GraphEditor.minNote;
		currentVal /= noteRange;
		if(currentVal < 0.0f) currentVal = 0.0f;
		if(currentVal > 1.0f) currentVal = 1.0f;
		float red = currentVal;
		float green = 0.0f;
		float blue = 1.0f - currentVal;
		if(red >= 0.5f) {
			green = (1.0f - red) * 2.0f;
		} else {
			green = red * 2.0f;
		}
		//return new Color(1.0f, 1.0f, 1.0f, 0.75f);
		return new Color(blue, green, red, alpha);
	}
	
	private Color getHarmonicColor(long harmonicID) {
		int red = (int) harmonicID % 64;
		int green = (int) (harmonicID / 64) % 64;
		int blue = (int) (harmonicID / (64 * 64)) % 64;
		return new Color(red + 192, green + 192, blue + 192);
	}
	
    protected void paintComponent(Graphics g) {
    	//System.out.println("Paint Component");
    	if(drawPlaying) {
    		//double millisPerPixel = (double) FDData.timeStepInMillis / pixelsPerTime;
    		//int startX = (int) Math.round((double) HarmonicsView.offsetInMillis / millisPerPixel + HarmonicsEditor.leftOffset);
    		g.drawImage(bi, 0, 0, null);
       		g.setColor(new Color(0.5f, 0.5f, 0.5f, 0.75f));
    		//g.fillRect(startX, 0, 1, getHeight());    		
    		drawPlaying = false;
    		return;
    	}
    	if(useImage == true) {
    		bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    		Graphics2D g2 = bi.createGraphics();
    		super.paintComponent(g);
    		drawFileData(g2);
    		g.drawImage(bi, 0, 0, null);
    		return;
    	}
		super.paintComponent(g);
		drawFileData(g);
		return;
    	
    }
	
}

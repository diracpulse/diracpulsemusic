import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class GraphView extends JComponent {

	public enum DataView {
		HARMONICS,
		AMPLITUDES;
	}
	
	public static DataView dataView = DataView.AMPLITUDES;
	
	private static final long serialVersionUID = 2964004162144513754L;
	
	private static BufferedImage bi;
	private static boolean useImage = true;
	private static boolean drawPlaying = false;
	private static int offsetInMillis;
	
	public void drawFileData(Graphics g) {
		double pixelsPerTime = (double) getWidth() / (double) GraphEditor.maxTime; 
		double pixelsPerLogAmplitude = (double) getHeight() / (double) GraphEditor.maxLogAmplitude;
		for(Harmonic harmonic: GraphEditor.harmonicIDToHarmonic.values()) {
			ArrayList<FDData> hData = new ArrayList<FDData>(harmonic.getAllData());
			if(hData.size() < 2) continue;
			FDData start = hData.get(0);
			for(int index = 1; index < hData.size(); index++) {
				FDData end = hData.get(index);
				int startX = (int) Math.round(pixelsPerTime * start.getTime());
				int startY = (int) Math.round(pixelsPerLogAmplitude * start.getLogAmplitude());
				startY = getHeight() - startY;
				int endX = (int) Math.round(pixelsPerTime * end.getTime());
				int endY = (int) Math.round(pixelsPerLogAmplitude * end.getLogAmplitude());		
				endY = getHeight() - endY;
				g.setColor(getDataColor((start.getNote() + end.getNote()) / 2));
				g.drawLine(startX, startY, endX, endY);
				start = end;
			}
		}
	}
	
	private Color getDataColor(double note) {
		return getDataColor(note, 1.0f);
	}

	private Color getDataColor(double note, float alpha) {
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
		int red = (int) harmonicID % 128;
		int green = (int) (harmonicID / 128) % 128;
		int blue = (int) (harmonicID / (128 * 128)) % 128;
		return new Color(red + 128, green + 128, blue + 128);
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
